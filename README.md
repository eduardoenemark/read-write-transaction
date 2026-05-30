# Read-Write Transaction

Example project demonstrating transactional separation of read and write operations using the Spring Framework. Routes read operations to a read-only HikariCP connection pool and write operations to a JTA-aware Atomikos connection pool via custom AOP-based routing.

## Architecture

The project implements a read-write split pattern where two parallel infrastructure stacks coexist:

- **Read Stack**: HikariCP (`HikariDataSource`) with `JpaTransactionManager` for local transactions
- **Write Stack**: Atomikos (`AtomikosNonXADataSourceBean`) with `JtaTransactionManager` for JTA transactions
- **Routing**: Custom `AbstractRoutingDataSource` with `@Aspect` interceptors inspect `@ReadOperation` / `@WriteOperation` annotations and set a `ThreadLocal<OperationType>` context at runtime

## Modules

| Module | Description |
|--------|-------------|
| `rwt-core-lib` | Reusable core library containing the routing infrastructure, annotations, AOP aspects, transaction managers, and configurations |
| `rwt-jdbc-app-server` | Spring Boot REST application that consumes the core library, exposing a CRUD API for products on PostgreSQL |

## Technologies

| Category | Libraries |
|----------|-----------|
| Java | JDK 17 |
| Framework | Spring Boot 3.5.14 |
| Connection Pools | HikariCP (read), Atomikos (write) |
| ORM / JPA | Spring Data JPA, Hibernate |
| JTA | Atomikos transactions-jdbc 6.0.0, Jakarta Transaction 2.0.1 |
| AOP | AspectJ Weaver 1.9.24 |
| Database | PostgreSQL 18 (via Testcontainers) |
| API Docs | SpringDoc OpenAPI 2.8.9 (Swagger) |
| Testing | JUnit 5, Mockito, Testcontainers |

## API Endpoints

| Method | Path | Operation | Description |
|--------|------|-----------|-------------|
| `POST` | `/product` | `@WriteOperation` | Save a product |
| `DELETE` | `/product/{id}` | `@WriteOperation` | Delete by ID |
| `DELETE` | `/products` | `@WriteOperation` | Delete all products |
| `GET` | `/product/{id}` | `@ReadOperation` | Get product by ID |
| `GET` | `/products` | `@ReadOperation` | List all products |
| `GET` | `/products/count` | `@ReadOperation` | Count products |
| `POST` | `/product/generate-and-save` | `@WriteOperation` | Generate and persist a fake product (JavaFaker) |
| `GET` | `/ping` | — | Health check |

## Prerequisites

- JDK 17+
- Maven 3.8+
- Podman or Docker

If using Podman:

```shell
systemctl --user enable --now podman.socket
```

## Build and Run

```shell
# Build all modules
mvn clean package

# Run the application
mvn -pl rwt-jdbc-app-server spring-boot:run
```

Or run the JAR directly:

```shell
java -jar rwt-jdbc-app-server/target/rwt-jdbc-app-server-1.0.0-SNAPSHOT.jar
```

The application starts on port **8080** with Swagger UI available at `/swagger-ui/html`.

## Configuration

Key configurations (in `application.properties`):

| Setting | Read Pool | Write Pool |
|---------|-----------|------------|
| Connection Pool | HikariCP | Atomikos |
| Pool Size | 5-20 | 10-50 |
| Read-Only | `true` | `false` |
| JTA | No | Yes |
| Hibernate Flush | `false` | `true` |

Database defaults: `jdbc:postgresql://localhost:5432/postgres` with username `postgres` and password `123456`.

## Testing

```shell
mvn test
```

Tests use Testcontainers with PostgreSQL and verify:
- Annotation metadata (`@ReadOperation`, `@WriteOperation`)
- Thread-local context binding and unbinding
- Aspect behavior and idempotent unbind
- Operation context correlation IDs and timing
- Multi-thread isolation
- Full integration through read/write operations on the REST API
