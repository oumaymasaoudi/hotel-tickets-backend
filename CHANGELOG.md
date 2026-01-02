# Changelog

Tous les changements notables de ce projet seront documentés dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère à [Semantic Versioning](https://semver.org/lang/fr/).

## [1.0.0] - 2026-01-01

### Added
- Initial release of Hotel Ticket Hub Backend API
- Spring Boot 3.2.0 application with Java 17
- PostgreSQL database integration
- Complete REST API for ticket management
- JWT-based authentication and authorization
- Role-based access control (Client, Technician, Admin, SuperAdmin)
- User management endpoints
- Hotel management endpoints
- Ticket CRUD operations
- Category management
- Technician assignment system
- Payment integration with Stripe
- Subscription management
- Email notification service (Spring Mail)
- File upload service for ticket images
- Image deletion functionality
- Rate limiting (100 requests/minute per IP)
- Swagger/OpenAPI documentation
- Global exception handling
- Pagination support with PageResponse DTO
- Database optimization scripts
- Audit logging system
- Complete CI/CD pipeline with GitHub Actions
- SonarQube integration for code quality
- Docker containerization
- Automated testing with JUnit 5
- Code coverage with JaCoCo
- Checkstyle and SpotBugs for code quality
- Automated staging deployment

### Infrastructure
- Docker and Docker Compose support
- GitHub Container Registry (GHCR) integration
- AWS EC2 deployment configuration
- PostgreSQL database on separate VM
- Environment-based configuration (.env files)

### Security
- JWT token-based authentication
- Password encryption with BCrypt
- Role-based authorization
- Rate limiting protection
- File upload validation
- Path traversal protection
- Input sanitization

### API Features
- RESTful API design
- Comprehensive error handling
- HTTP status codes (400, 401, 403, 404, 409, 500)
- Structured error responses
- Pagination support
- Filtering and sorting
- Search functionality

### CI/CD Features
- Automated linting (Checkstyle, SpotBugs)
- Automated testing (JUnit 5)
- Code coverage analysis (JaCoCo)
- SonarQube quality gate
- Automated Docker build and push
- Automated staging deployment
- Health checks and monitoring

### Database
- PostgreSQL database schema
- JPA/Hibernate entities
- Spring Data JPA repositories
- Database migrations support
- Optimization indexes

