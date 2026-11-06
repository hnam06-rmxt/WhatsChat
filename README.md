# WhatsChat (Messenger-like web chat)

- `client/`  React + Vite (STOMP qua @stomp/stompjs, axios, react-router-dom)
- `server/`  Spring Boot 3.4 (Java 17+), PostgreSQL, JWT, WebSocket STOMP, Flyway

## Chay server
1. Tao user + database trong psql:
   ```sql
   CREATE USER chat WITH PASSWORD 'change_me';
   CREATE DATABASE chatdb OWNER chat ENCODING 'UTF8' TEMPLATE template0;
   ```
2. `cd server` roi `mvnw.cmd spring-boot:run` (Windows) hoac mo bang IntelliJ chay `ChatServerApplication`.
   Flyway tu tao bang tu `src/main/resources/db/migration/V1__init_schema.sql`.
3. Server chay o http://localhost:8080

## Chay client
`cd client` -> `npm run dev` -> http://localhost:5173

## API chinh
| Method | Path | Mo ta |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | tra ve `{token, user}` |
| GET | /api/users?q= | tim user |
| GET | /api/conversations | sidebar (tin cuoi + so tin chua doc) |
| POST | /api/conversations/direct `{userId}` | mo/tao chat 1-1 |
| POST | /api/conversations/group `{name, memberIds}` | tao nhom |
| GET | /api/conversations/{id}/messages?beforeId=&limit= | lich su, phan trang cursor |
| POST | /api/conversations/{id}/messages `{content}` | gui tin |
| POST | /api/conversations/{id}/read `{messageId}` | danh dau da doc |

## WebSocket (STOMP)
- Ket noi `ws://localhost:8080/ws`, header CONNECT `Authorization: Bearer <jwt>`
- Gui: `/app/chat.send` `{conversationId, content}`
- Nhan: subscribe `/topic/conversations.{id}`
