package com.example.chat.config;

import com.example.chat.repository.ConversationMemberRepository;
import com.example.chat.security.JwtService;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket.
 *  - client ket noi:   ws://localhost:8080/ws   (header CONNECT: Authorization: Bearer <jwt>)
 *  - client gui:       /app/chat.send
 *  - client subscribe: /topic/conversations.{id}   (chi thanh vien moi duoc subscribe)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Pattern TOPIC = Pattern.compile("^/topic/conversations\\.(\\d+)$");

    private final JwtService jwt;
    private final ConversationMemberRepository members;
    private final String corsOrigin;

    public WebSocketConfig(JwtService jwt, ConversationMemberRepository members,
                           @Value("${app.cors-origin}") String corsOrigin) {
        this.jwt = jwt;
        this.members = members;
        this.corsOrigin = corsOrigin;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(corsOrigin);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (acc == null) return message;

                if (StompCommand.CONNECT.equals(acc.getCommand())) {
                    String h = acc.getFirstNativeHeader("Authorization");
                    if (h == null || !h.startsWith("Bearer ")) throw new MessagingException("Missing token");
                    try {
                        Long userId = jwt.parseUserId(h.substring(7));
                        acc.setUser(new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of()));
                    } catch (Exception e) {
                        throw new MessagingException("Invalid token");
                    }
                } else if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
                    if (acc.getUser() == null) throw new MessagingException("Unauthenticated");
                    Matcher m = TOPIC.matcher(String.valueOf(acc.getDestination()));
                    if (!m.matches()) throw new MessagingException("Destination not allowed");
                    Long userId = Long.valueOf(acc.getUser().getName());
                    if (!members.existsByConversationIdAndUserId(Long.valueOf(m.group(1)), userId))
                        throw new MessagingException("Not a member");
                }
                return message;
            }
        });
    }
}
