# Hotel Ticket Hub - Backend

Spring Boot backend application for the Hotel Ticket Hub system.

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 12+

### Installation

```bash
# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Testing

```bash
# Run all tests
mvn clean test

# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## CI/CD

The project includes a GitHub Actions workflow (`.github/workflows/ci.yml`) that runs:

- **Linting**: Checkstyle + SpotBugs
- **Testing**: JUnit + JaCoCo coverage
- **Build**: Maven package
- **SonarQube**: Code quality analysis

## Code Quality

- **Checkstyle**: Code style checking
- **SpotBugs**: Bug detection
- **JaCoCo**: Code coverage (minimum 50%)
- **SonarQube**: Quality gate

## SonarQube Local

To run SonarQube locally:

```bash
docker-compose -f docker-compose.sonarqube.yml up -d
```

Access SonarQube at `http://localhost:9000` (admin/admin)

## Features

- **Authentication & Authorization**: JWT-based with role-based access control
- **Ticket Management**: CRUD operations for tickets
- **Technician Management**: CRUD operations for technicians
- **Payment Management**: Stripe integration for subscriptions
- **Reporting**: PDF and CSV export
- **Comments**: Ticket comments system
- **Notifications**: Real-time notifications

## License

This project is part of an academic assignment.

## Authors

Hotel Ticket Hub Development Team

