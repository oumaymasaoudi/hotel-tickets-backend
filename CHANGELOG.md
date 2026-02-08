# [1.9.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.8.2...v1.9.0) (2026-02-08)


### Features

* add Loki datasource provisioning and test guide ([d2ae3c4](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/d2ae3c467bd876980beae9bf2f59212097e51c71))

## [1.8.2](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.8.1...v1.8.2) (2026-02-08)


### Bug Fixes

* add disk space cleanup script and Prometheus connection troubleshooting ([6523f55](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/6523f55ce11728e71218b4b749718a9943c065a9))

## [1.8.1](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.8.0...v1.8.1) (2026-02-08)


### Bug Fixes

* update Loki config to use tsdb instead of deprecated boltdb-shipper ([f897bf0](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/f897bf09e02676516233c3e91d8e88bbfc8fd072))

# [1.8.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.7.0...v1.8.0) (2026-02-08)


### Bug Fixes

* remove deprecated shared_store field from Loki config ([0909a38](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/0909a380cafa5cc0054bbb2c8c7240741a9fa64c))


### Features

* add script to activate all services and VM status documentation ([b868a9c](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/b868a9c5a5cf703c8f845adc1e295f9ffdc31771))
* add script to activate all services on all VMs ([fff690d](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/fff690d0047aa986ab1bcb7cebcb24881975af7a))

# [1.7.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.6.1...v1.7.0) (2026-02-08)


### Features

* add comprehensive functionality test script ([26b514d](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/26b514db0952fb26ef5dda009fb8edcfe36c48fd))
* add comprehensive functionality tests and verification document ([022ceff](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/022ceffa1f7888a9ad127ee049544da93c41991d))

## [1.6.1](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.6.0...v1.6.1) (2026-02-08)


### Bug Fixes

* improve test scripts and add complete VM test ([7258e50](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/7258e500b9acdad2f70b91f3ef54b4e3bd968c7f))


### Performance Improvements

* optimize CI/CD pipeline - remove unnecessary docker compose down ([83a5403](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/83a54037b168f7e39fbd849dc0c172c95500ffdd))
* optimize monitoring deployment - use force-recreate instead of down ([4dca953](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/4dca953ed54c53b29d8901f00ce4eab4d1d57cad))

# [1.6.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.5.1...v1.6.0) (2026-02-08)


### Features

* add comprehensive API testing script and connection troubleshooting guide ([cb2373e](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/cb2373e06fde9a4dbf39958efb7e5a71452d8ab4))

## [1.5.1](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.5.0...v1.5.1) (2026-02-08)


### Bug Fixes

* allow TECHNICIAN role to access tickets by hotel endpoint ([7872f87](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/7872f875aa3c09855547f0b2449319c69bd33a48))

# [1.5.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.4.0...v1.5.0) (2026-02-08)


### Features

* add scripts to create SuperAdmin and setup VM completely ([a278254](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/a2782542a8d435cf1f78a9082637b388a172fafc))

# [1.4.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.3.5...v1.4.0) (2026-02-08)


### Bug Fixes

* add comprehensive VM fixes for database and API issues ([05146dc](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/05146dcba78056604c3da36d87e8eee664aacb8a))
* add missing SubscriptionPlan import in HotelService ([2e45dd5](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/2e45dd575560321f4ebbfe64263236f322669d5f))
* comprehensive fixes for database and API issues ([e965401](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/e965401c38997e8cc0456a698f7f977c0410adbf))
* correct PowerShell script syntax ([905442a](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/905442a5b551f358950113de189c41497bd86818))


### Features

* improve Swagger/OpenAPI configuration with JWT support ([0dea11d](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/0dea11d115b01881eaa1ef0fb17e60f39b187ece))

## [1.3.5](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.3.4...v1.3.5) (2026-02-08)


### Bug Fixes

* improve error handling in hotels/public endpoint to prevent 500 errors ([5086f7e](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/5086f7e3fbedb1d98ea4fe5080f50a06c40154db))

## [1.3.4](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.3.3...v1.3.4) (2026-02-08)


### Bug Fixes

* add error handling and logging to hotels/public endpoint to debug 400 error ([5315a3c](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/5315a3cfcf529426f492d216ba4f185515b69d9c))

## [1.3.3](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.3.2...v1.3.3) (2026-02-07)


### Bug Fixes

* update backend IP to 13.63.15.86 in OpenAPI config and test script ([cd533a6](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/cd533a6229c7751f05c27faa151f8e880797e49c))

## [1.3.2](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.3.1...v1.3.2) (2026-02-07)


### Bug Fixes

* update CORS configuration to allow frontend staging URLs ([cd672e0](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/cd672e0ca5b9811b16acdfc2d7f3877ebc24eaa4))

## [1.3.1](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.3.0...v1.3.1) (2026-02-07)


### Bug Fixes

* correct typo 'ancible-controller' to 'ansible-controller' in Prometheus config ([c10eae1](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/c10eae1b4e94f013888f03e7a18322c0291a6ad8))

# [1.3.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.2.0...v1.3.0) (2026-02-07)


### Features

* replace HTTP requests panel with real-time status codes visualization ([49d6f2f](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/49d6f2fd5f87e134622228fc967fa3f5c6e8a835))

# [1.2.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.1.3...v1.2.0) (2026-02-07)


### Features

* improve Grafana dashboard for Spring Boot backend with robust queries ([7142f16](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/7142f1611e6a63b53b369fe27f635b15d4d7934a))

## [1.1.3](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.1.2...v1.1.3) (2026-02-07)


### Bug Fixes

* remove invalid metrics configuration causing Spring Boot startup failure ([b8f004e](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/b8f004e2f7d0cdc682a91a0e24d53715918f4d2b))

## [1.1.2](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.1.1...v1.1.2) (2026-02-06)


### Bug Fixes

* increase deployment timeout and improve health check retry logic ([5260fe5](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/5260fe5f8b508ccf541b61695c941f542a67cca5))

## [1.1.1](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.1.0...v1.1.1) (2026-02-06)


### Reverts

* Revert "feat: improve backend reliability with auto-restart and monitoring" ([5bd169e](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/5bd169e44584104a36b2824a3ddc65184518f996))

# [1.1.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.0.0...v1.1.0) (2026-02-06)


### Features

* improve backend reliability with auto-restart and monitoring ([8bb5542](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/8bb554284532cce5919911726f41cbdba6433b8f))

# 1.0.0 (2026-02-03)


### Bug Fixes

* add @semantic-release/exec dependency and update workflow ([7c80ba8](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/7c80ba82daec0a7214ff503c0fbf9f05b1f965b3))
* Add condition to SonarQube job for backend ([d173f59](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/d173f59111fe035fa95f7860d618c225a34b953c))
* Add disk space cleanup before Docker image pull in monitoring deployment ([5fa5de0](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/5fa5de018fac1f240942032b138651d3c1649573))
* add frontend staging URL to CORS allowed origins ([aa2b6bd](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/aa2b6bd74c09e39a6420f4a8c5e932d4900c437f))
* add Spring Boot Actuator and Micrometer Prometheus dependencies ([d1ddacd](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/d1ddacde59a92907cb078d44599c48a5e82ea17d))
* add Spring Boot Actuator and Micrometer Prometheus dependencies ([8be3968](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/8be3968a5a6185ad0e6d0d3fc2b3e7547ae21224))
* Aggressive Docker disk space cleanup to prevent 'no space left' errors ([f128754](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/f1287544530b9deb382363797f7b47bdb87cb124))
* Align SonarCloud configuration with frontend ([d94aa50](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/d94aa50d57323904018eb76177dd1363c7478d62))
* Align SonarQube workflow with frontend configuration ([af7a141](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/af7a141032d3efa8414b0299d93df4d3e03d017b))
* allow SonarCloud to run on schedule ([2d8d135](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/2d8d135a879ab89d78578a2f1ebb855a6a35bbb3))
* Configure SonarCloud to send emails on branch analysis ([cbd2d07](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/cbd2d07164003de2ea64d41fcc5636dc01fbc425))
* Configure SonarCloud with correct parameters for email notifications ([8247463](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/8247463398d619dcde3a6bd1b870a98c16acbd69))
* conflit port 9100 ([665ff54](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/665ff5452a7be608bee13955e4f3bb7a1ce04b8a))
* correct artifact download paths for SonarQube job ([d399e82](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/d399e8251f39c2d08aac40fc19e18dea806a8c10))
* correct JAR upload and SonarQube configuration ([a1c58f0](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/a1c58f06657fc255ab7b52c29d0c2a7c1e5352db))
* correct SonarQube action ([dfd55f7](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/dfd55f7839b3e20ee71c1817d9c13cc4eb01edd8))
* corriger problèmes SonarQube et nettoyer documentation ([e58150d](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/e58150d8bd10c0313927ecd53be71780a626390e))
* Disable mail health check to prevent DOWN ([91f2f67](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/91f2f67a154d1a60ec9be7f5df357594ef52e033))
* Disable mail health check to prevent DOWN status when mail is not configured ([ad0db0b](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/ad0db0b03187fec2bc04443e22250c03d1024fdd))
* Enable Prometheus endpoint explicitly ([0ea870b](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/0ea870b9664115b2a01bcaad230b86288ce71f64))
* erreur ([f4f4db0](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/f4f4db02765b5536fa0e43037ad4b1abf0791901))
* erreurs ([908ef56](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/908ef5626a17cfe78ee0f757471020591deee281))
* erreurs sonar ([c3bb741](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/c3bb74173935c9f1db0f42c84390f2fa93c551d1))
* Exclude Actuator endpoints from GlobalExceptionHandler ([86f0f44](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/86f0f4433ccf7d6ea8a8739e416698ede000cb66))
* fichier yml ([9032fa4](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/9032fa435e13b6ee0e0533548ce742559e4df7a8))
* I/O function calls should not be vulnerable to path injection attacks javasecurity:S2083 ([842202a](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/842202ab886fc7a967f1f0ffa66345bd8e669c1a))
* Improve deployment script ([34c63ab](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/34c63aba149e74225434076c8dd6292452aeee79))
* Improve deployment script and SonarQube verification ([c52b924](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/c52b924158cc56c339b064c77820fea44a4b1877))
* improve error handling for coverage check and SonarQube ([7164d1d](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/7164d1da071147bc1f6f2c1d0b86e0a76cea14cc))
* Improve monitoring deployment and Docker cleanup ([c6cc901](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/c6cc901f2e685417d0cce729b77da85a60f4d345))
* Improve monitoring stack health check verification ([11e54de](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/11e54de0d78568954dc0b337d0b954eda7205bd6))
* Improve SCP file copy with fallback method ([e7a8513](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/e7a8513a69f762ae0e3957da1f39f941af3461db))
* Improve SonarCloud and SCP error handling ([1f36dac](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/1f36dace72e5020efc02c81abab504f465da8366))
* include technicians with NULL hotel_id in getTechniciansByHotel ([030a9c8](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/030a9c84a24ba1f2517b01c02ec626c101c10cf8))
* prometherus yml ([5b5de97](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/5b5de975ff0d37a8d850a2df5118d6853092e441))
* remove duplicate Actuator and Micrometer Prometheus dependencies ([619fb31](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/619fb315c8161305d557a42fe93ea2aa4cd65e1d))
* remove jacoco ([26a6ee2](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/26a6ee2a6b7a14cea90f0a85fc1bccd7025867bf))
* remove jacoco:check step, let SonarCloud handle quality gate ([d3526ba](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/d3526ba4bf9aa9a85d2ecc0b93b7b5394be39754))
* Replace all SCP actions with direct scp commands and SSH fallback ([a601950](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/a60195052834ff62e71afc475a5e2863ca39546e))
* Resolve SonarQube issues - Replace System.out with logger, add constants for duplicated literals, replace generic exceptions ([decb2a5](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/decb2a5d21561d84150570ada1c2c1110c45119f))
* update all IPs to Elastic IPs and clean up Prometheus config ([77223bb](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/77223bb40655534e9daf0ff1b23f386ff2342389))
* update sonar-project.properties with correct keys ([f1f52df](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/f1f52df2b1b82f311fd7ff63a3b52931969bcd5c))
* update SonarQube plugin and fix CI/CD workflow issues ([0125ddf](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/0125ddf8720ac3b01d9a3a071518394bf8c71c0c))
* use direct JavaScript file for semantic-release to avoid permission issues ([1019ce6](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/1019ce69d0b689e7e21f12daae7c1832ec8abf86))
* use node to run semantic-release and fix permissions ([fb9deac](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/fb9deac32bc8ea86663e72538c13076bcbec5f7a))
* use npm exec instead of npx for semantic-release ([14d4551](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/14d4551e77fb02267809caad0edb035ba27c6660))
* use npm install ([cd569e5](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/cd569e51f0770fb876cf87061e40e12201e733c1))
* use npm install instead of npm ci and disable cache ([03f0971](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/03f0971b30f077f6fc91b91199ad2915d4f12139))
* use npm run for semantic-release instead of direct node execution ([08eb629](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/08eb6298b71333509fd60e54d98f80dcd2ac2714))
* use npx --yes for semantic-release and npm ci for reliable install ([c2c5e00](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/c2c5e00f53152546f5720e9e7def800c05818d55))
* use SonarCloud GitHub Action instead of Maven plugin to fix FeatureFlags error ([a8864fd](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/a8864fda3a23bee3a2299c0206e27a7284c38395))


### Features

* add workflow ([113762b](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/113762bd3430fa6efd459c693640cafe0f47e72b))
* add workflow to add oumayma key ([9474a75](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/9474a756ff214f893de56030451cfc5cc5c01680))
* add workflow to add oumayma key to backend ([a4ff61c](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/a4ff61c286793c073b7707aa424c754ae4f5060c))
* add workflow to add SSH key to backend ([b046e00](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/b046e005f25030e376704974a00e05e61a85abab))
* ajouter deploiement automatique staging ([a2ad1bc](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/a2ad1bcb09917c6645f73b3d4b8a32f0358c6966))
* Code cleanup ([6ac1d11](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/6ac1d11f0554ba67799c533f0ae8cdbd0aaf50be))
* Code fixed ([6a4820d](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/6a4820d6921f36da7f93eb5fb7857b21e24f7d50))
* complete all improvements - email, security, performance, documentation ([1aca18a](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/1aca18a1bd44947ff618038a8e90ee75ccb41b69))
* configure CI/CD pipeline with SonarQube, improve tests and coverage ([14d8978](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/14d8978afdfa4abcb06ab3a3bdceb20e478c3758))
* description du changement ([2e7cfb2](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/2e7cfb2473a133472288e023f404d7352a1eaa1a))


### Performance Improvements

* optimize CI workflow to avoid duplicate test execution ([9f1ed71](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/9f1ed71c224d22edf0b8d2b3bee1caaf9ed86e86))

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
