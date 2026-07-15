# Personal Blog System - Backend

SpringBoot backend service for the Personal Blog System.

## Technology Stack

- **Framework**: Spring Boot 2.7.18
- **Language**: Java 11
- **Database**: MySQL 8.0
- **Cache**: Redis
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA
- **Testing**: JUnit 5, Mockito, jqwik (Property-Based Testing)

## Project Structure

```
blog-server/
├── src/
│   ├── main/
│   │   ├── java/com/blog/
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── service/         # Business logic services
│   │   │   ├── repository/      # Data access repositories
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── security/        # Security & JWT
│   │   │   └── exception/       # Exception handling
│   │   └── resources/
│   │       └── application.yml  # Application configuration
│   └── test/
│       └── java/com/blog/
│           ├── unit/            # Unit tests
│           ├── property/        # Property-based tests
│           └── integration/     # Integration tests
└── pom.xml                      # Maven dependencies
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

## Configuration

Before running the application, configure the following in `application.yml`:

1. **Database Connection**:
   - URL: `jdbc:mysql://localhost:3306/blog_system`
   - Username: `root`
   - Password: `root`

2. **Redis Connection**:
   - Host: `localhost`
   - Port: `6379`

3. **JWT Secret**: Update the JWT secret key for production

4. **Aliyun Services** (Optional):
   - SMS service credentials
   - OSS storage credentials

## Database Setup

Create the database:

```sql
CREATE DATABASE blog_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The application will automatically create tables on first run (ddl-auto: update).

## Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

### Build and Run

```bash
mvn clean package
java -jar target/blog-server-1.0.0.jar
```

The server will start on `http://localhost:8080/api`

## Testing

### Run All Tests

```bash
mvn test
```

### Run Unit Tests Only

```bash
mvn test -Dtest="com.blog.unit.**"
```

### Run Property-Based Tests Only

```bash
mvn test -Dtest="com.blog.property.**"
```

### Run Integration Tests Only

```bash
mvn test -Dtest="com.blog.integration.**"
```

## API Documentation

API endpoints will be documented as they are implemented. The base URL is:

```
http://localhost:8080/api
```

## Development Guidelines

1. **Code Style**: Follow Java naming conventions
2. **Testing**: Write both unit tests and property-based tests
3. **Logging**: Use SLF4J for logging
4. **Error Handling**: Use custom exceptions and global exception handler
5. **Security**: Always validate and sanitize user input

## Property-Based Testing

This project uses jqwik for property-based testing. Each correctness property from the design document should have a corresponding property test with at least 100 iterations.

Example:

```java
/**
 * Feature: personal-blog-system, Property 1: 用户注册唯一性
 * 验证需求: 15.2
 */
@Property(tries = 100)
void testUserRegistrationUniqueness(@ForAll String phone, @ForAll String email) {
    // Test implementation
}
```

## License

Copyright © 2025 Blog System
