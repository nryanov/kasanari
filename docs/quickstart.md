# Quickstart

## Prerequisites

- Java 21
- Docker (or Colima/Docker-compatible runtime)
- Shell with `curl`

If you use Colima on macOS, Docker socket compatibility may be needed:

```bash
sudo ln -sf "$HOME/.colima/default/docker.sock" /var/run/docker.sock
```

## 1) Build the project

From repository root:

```bash
./gradlew assemble --no-daemon
```

## 2) Start local infrastructure

Start MinIO and PostgreSQL:

```bash
docker compose -f development/docker-compose.yml up -d
```

Services:

- MinIO API: `http://localhost:9000`
- MinIO console: `http://localhost:9001`
- PostgreSQL: `localhost:5432`

## 3) Run Kasanari

Default auth mode is `none`, so this is enough:

```bash
./gradlew :modules:server:quarkusDev
```

For development profile:

```bash
./gradlew :modules:server:quarkusDev -Dquarkus.profile=dev
```

## 4) Verify

Health check:

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/q/health
```

Expected: `200`

Swagger UI:

- `http://localhost:9090/docs`

## Optional: run auth examples

- `examples/auth-none`
- `examples/auth-ldap`
- `examples/auth-oidc`

Each example contains a dedicated `README.md` and environment setup steps.
