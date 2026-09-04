# Nava Music Server

The Java backend for [Nava Music](https://github.com/HosseinBadr404/nava-music-app), an academic cross-platform music-player and marketplace project.

## Features

- Newline-delimited JSON protocol over TCP sockets
- Bounded worker pool for concurrent clients
- User registration, login, profile updates, and subscriptions
- PBKDF2 password hashing with per-user salts
- Music catalog, purchases, downloads, comments, likes, and dislikes
- Prepared SQL statements and transactional comment reactions
- Environment-based database and server configuration

## Requirements

- JDK 17+
- Maven 3.9+
- MySQL 8+

## Setup

1. Apply `src/main/resources/db/schema.sql` to MySQL.
2. Configure the environment variables shown in `.env.example`.
3. Build and start the server:

```bash
mvn clean test
mvn exec:java
```

The server listens on port `8081` by default. The app can override its host and port with Flutter `--dart-define` values.

## Configuration

| Variable | Purpose | Default |
| --- | --- | --- |
| `NAVA_DB_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/nava_music` |
| `NAVA_DB_USER` | Database user | `nava` |
| `NAVA_DB_PASSWORD` | Database password | required |
| `NAVA_PORT` | TCP server port | `8081` |
| `NAVA_USERS_FILE` | Local user-state file | `data/users.txt` |

## Security note

This remains a course project, not a production payment or identity service. Passwords are hashed and server/database credentials are no longer committed, but the custom TCP protocol does not provide TLS, authorization tokens, rate limiting, or production-grade transaction guarantees. Do not expose it directly to the public internet.

Created as part of an Advanced Programming final project by Hossein Badr and Mahan Varmazyar.
