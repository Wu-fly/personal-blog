# Database Configuration Guide

## Overview

This document describes the database and JPA configuration for the Personal Blog System backend.

## Technology Stack

- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA with Hibernate
- **Connection Pool**: HikariCP
- **Migration**: Hibernate DDL Auto (development) / Manual SQL scripts (production)

## Configuration Files

### 1. application.yml (Base Configuration)

Contains common configuration shared across all environments:
- Database connection settings
- HikariCP connection pool configuration
- JPA/Hibernate properties
- Logging configuration

### 2. application-dev.yml (Development)

Development-specific settings:
- Local database connection
- DDL auto mode: `update` (auto-creates/updates tables)
- SQL logging enabled
- Debug logging level

### 3. application-prod.yml (Production)

Production-specific settings:
- Environment variable-based database connection
- DDL auto mode: `validate` (prevents auto schema changes)
- SQL logging disabled
- Optimized connection pool settings

## HikariCP Connection Pool

HikariCP is configured with the following settings:

### Development Settings
- **maximum-pool-size**: 10
- **minimum-idle**: 2
- **connection-timeout**: 30000ms (30 seconds)
- **idle-timeout**: 600000ms (10 minutes)
- **max-lifetime**: 1800000ms (30 minutes)

### Production Settings
- **maximum-pool-size**: 30
- **minimum-idle**: 10
- **connection-timeout**: 20000ms (20 seconds)
- **idle-timeout**: 300000ms (5 minutes)
- **max-lifetime**: 1200000ms (20 minutes)
- **leak-detection-threshold**: 30000ms (30 seconds)

## JPA/Hibernate Configuration

### Key Settings

1. **DDL Auto Mode**
   - Development: `update` - Automatically creates/updates tables
   - Production: `validate` - Only validates schema, no auto changes

2. **SQL Logging**
   - Development: Enabled with formatting and comments
   - Production: Disabled for performance

3. **Batch Processing**
   - `batch_size`: 20 - Processes 20 statements in a batch
   - `fetch_size`: 50 - Fetches 50 rows at a time
   - `order_inserts`: true - Orders inserts for better performance
   - `order_updates`: true - Orders updates for better performance

4. **Naming Strategy**
   - Physical: Standard (uses exact column names as defined)
   - Implicit: Spring (converts camelCase to snake_case)

5. **Open Session In View**
   - Disabled to avoid lazy loading issues and N+1 query problems

## Database Setup

### 1. Create Database

Run the initialization script:

```bash
mysql -u root -p < src/main/resources/db/init-database.sql
```

Or manually:

```sql
CREATE DATABASE IF NOT EXISTS blog_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

### 2. Configure Connection

Update `application.yml` or set environment variables:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_system
    username: your_username
    password: your_password
```

### 3. Run Application

In development mode, Hibernate will automatically create/update tables based on entity definitions.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Environment Variables (Production)

For production deployment, use environment variables:

- `DB_HOST`: Database host (default: localhost)
- `DB_PORT`: Database port (default: 3306)
- `DB_NAME`: Database name (default: blog_system)
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

Example:

```bash
export DB_HOST=your-db-host.com
export DB_PORT=3306
export DB_NAME=blog_system
export DB_USERNAME=blog_user
export DB_PASSWORD=secure_password

java -jar blog-server.jar --spring.profiles.active=prod
```

## Performance Optimization

### 1. Indexes

Indexes are automatically created by JPA based on entity annotations:
- `@Index` on entity classes
- Foreign key indexes
- Unique constraint indexes

### 2. Connection Pool Tuning

Monitor connection pool metrics and adjust settings based on:
- Application load
- Database capacity
- Response time requirements

### 3. Query Optimization

- Use `@Query` with JPQL for complex queries
- Enable batch processing for bulk operations
- Use pagination for large result sets
- Avoid N+1 query problems with `@EntityGraph`

## Monitoring

### 1. Connection Pool Metrics

HikariCP provides metrics through JMX:
- Active connections
- Idle connections
- Pending threads
- Connection creation time

### 2. SQL Logging

In development, SQL queries are logged with:
- Formatted SQL
- Parameter bindings
- Execution time (with statistics enabled)

### 3. Hibernate Statistics

Enable in development for detailed metrics:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
```

## Troubleshooting

### Common Issues

1. **Connection Timeout**
   - Increase `connection-timeout` in HikariCP settings
   - Check database connectivity
   - Verify firewall rules

2. **Too Many Connections**
   - Reduce `maximum-pool-size`
   - Check for connection leaks
   - Enable `leak-detection-threshold`

3. **Slow Queries**
   - Enable SQL logging
   - Check for missing indexes
   - Optimize query logic

4. **Schema Validation Errors**
   - Ensure database schema matches entity definitions
   - Run migration scripts manually
   - Check DDL auto mode setting

## Best Practices

1. **Use Transactions**
   - Annotate service methods with `@Transactional`
   - Use `readOnly=true` for read-only operations

2. **Avoid Lazy Loading Issues**
   - Disable Open Session In View
   - Use `@EntityGraph` or JOIN FETCH
   - Load associations explicitly in service layer

3. **Batch Operations**
   - Use batch processing for bulk inserts/updates
   - Configure appropriate batch size

4. **Connection Pool**
   - Monitor pool metrics regularly
   - Tune settings based on load
   - Set appropriate timeouts

5. **Production Deployment**
   - Use `validate` DDL mode
   - Disable SQL logging
   - Use environment variables for credentials
   - Enable connection leak detection

## References

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [MySQL 8.0 Reference Manual](https://dev.mysql.com/doc/refman/8.0/en/)
