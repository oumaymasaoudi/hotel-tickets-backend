## [1.12.1](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.12.0...v1.12.1) (2026-02-19)


### Bug Fixes

* utiliser [@main](https://github.com/main) temporairement pour OWASP Dependency Check (Dependabot dÃ©couvrira la version taguÃ©e) ([6da30ef](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/6da30ef231a3920fa86f3972eec8bb2bbf865a3f))

# [1.12.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.11.2...v1.12.0) (2026-02-19)


### Features

* dependaboty.yml ([9da2418](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/9da2418cdb4e7d73510aa590775ff41dd0d88fe1))

## [1.11.2](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.11.1...v1.11.2) (2026-02-19)


### Bug Fixes

* pipline ([f10e28c](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/f10e28c16378c03a24a46f7187121a958456dfee))

## [1.11.1](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.11.0...v1.11.1) (2026-02-19)


### Bug Fixes

* yml ([7b9ad2e](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/7b9ad2e2ce1f65b7a0605c1159c30fa1b72e6cbd))

# [1.11.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.10.7...v1.11.0) (2026-02-18)


### Features

* ajouter script PowerShell pour dÃ©sactiver analyse auto SonarCloud ([55e3d1a](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/55e3d1aedd5ab5ae9bc2d2ab790a210d59d7f056))

## [1.10.7](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.10.6...v1.10.7) (2026-02-18)


### Bug Fixes

* exclure node_modules du scan OWASP (backend Java uniquement) ([386a0c7](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/386a0c7a8f5b076893f5070ada0e798da57de6ab))

## [1.10.6](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.10.5...v1.10.6) (2026-02-18)


### Bug Fixes

* corriger SonarCloud (ajouter args pour dÃ©sactiver auto-analysis) et simplifier OWASP args ([7d5ef32](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/7d5ef32edc23174b7583298f01433cd639e3e47d))
* utiliser SONAR_SCANNER_OPTS pour SonarCloud au lieu de args ([32005db](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/32005dbe3819442d5ea49ce74d95191d63c61fbb))

## [1.10.5](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.10.4...v1.10.5) (2026-02-18)


### Bug Fixes

* retirer setup Java pour OWASP (action utilise Docker avec Java intÃ©grÃ©) ([b4c7ddc](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/b4c7ddc8bf5075103bd7a9a4fd4388b002c1fdd7))

## [1.10.4](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.10.3...v1.10.4) (2026-02-18)


### Bug Fixes

* corriger format args OWASP (utiliser |) et ajouter sonar.ci.skip=false ([851837b](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/851837b76eccc396bc39529708c109b1c2ef6eed))
* corriger SonarCloud (dÃ©sactiver analyse auto) et format args OWASP (utiliser | au lieu de >) ([18ff2f7](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/18ff2f754ee1c6caad692c730476d50dfc3560ef))

## [1.10.3](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.10.2...v1.10.3) (2026-02-18)


### Bug Fixes

* corriger format arguments OWASP dependency-check (utiliser = pour les valeurs) ([37df53c](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/37df53c45116184127638e9b8a5d7af5a7dd5f69))

## [1.10.2](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.10.1...v1.10.2) (2026-02-18)


### Bug Fixes

* corriger version dependency-check action de v3 Ã  v5 ([9f326a6](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/9f326a6dcbbe64fbee0c7bc9fcc6d6d952a6ed39))
* utiliser [@main](https://github.com/main) pour dependency-check action (version stable) ([873f828](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/873f8288ca256cf2ff8e6b759640edbb9ef61f0a))

## [1.10.1](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.10.0...v1.10.1) (2026-02-18)


### Bug Fixes

* permettre SonarCloud, OWASP et Trivy sur les pull requests ([03545e1](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/03545e18e1e1287b98f615137a90df778c4e7fe4))

# [1.10.0](https://github.com/oumaymasaoudi/hotel-tickets-backend/compare/v1.9.0...v1.10.0) (2026-02-18)


### Bug Fixes

* ajouter constante DEV_TOKEN manquante dans AuthService ([aec3e24](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/aec3e2402b93579309cd88360651c131a8ab484f))
* ajouter contentType et content expectations pour TicketControllerTest ([47f670a](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/47f670a9c6a13dde7a811d7bd97f1567ffeaeeb1))
* ajouter hotelId et categoryId requis dans TicketControllerTest ([31a8bab](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/31a8bab23cb2c186fc9179902b4568f9a17c5243))
* convertir TicketControllerTest pour utiliser MockMvc avec @RequestPart ([8d3f7da](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/8d3f7daf5e38272ddae6c673ef6973cb37427c63))
* corriger formatage email - valeurs null et %n dans sendReport ([321e838](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/321e838f6601391d00315fbaa18c4517ff735cd7))
* corriger imports MockMultipartFile et ResponseEntity dans TicketControllerTest ([aaa5339](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/aaa5339e60b213afdf1938d3f84c1111b93a12b7))
* corriger problemes critiques SonarCloud - PasswordHashGenerator, EmailService, AuthService, HotelRestController ([720432f](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/720432f4645306dbd74e79c68969c622e39eaad0))
* corriger testCreateTicket_Error - MockMvc multipart ne propage pas correctement les exceptions ([7fa7a5e](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/7fa7a5e9be86768419a41ce469010ff14418f012))
* corriger TicketController et tests - laisser GlobalExceptionHandler gÃ©rer exceptions, dÃ©finir content-type ([2c03793](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/2c03793c20e4031cacc5b720f2bff77068cf80ba))
* dÃ©sactiver temporairement la vÃ©rification JaCoCo stricte - SonarCloud gÃ¨re la couverture ([da7047a](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/da7047a2a35c0903987c6d3acecb699ff7dc21f5))
* simplifier tests TicketController - MockMvc multipart a des limitations avec body JSON ([07f3149](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/07f31497b537196a0fa15cb7eb6a9199db82f800))
* simplifier tests TicketController avec contentTypeCompatibleWith ([4c6c315](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/4c6c315783830d6a0d9f825f63a455475452ef33))
* simplifier TicketController et amÃ©liorer test avec assertions explicites ([0dafc35](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/0dafc353198e666a304bd9ce3baf05a57621eb44))
* tester directement TicketController sans MockMvc pour Ã©viter problÃ¨mes multipart ([3de7bcc](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/3de7bccbc0aa56ecda201530637b91ce2d18a585))
* tests unitaires ([8ae31e5](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/8ae31e573925be7151c16685203992a83d89280d))
* TicketController retourne 201 CREATED avec content-type explicite + ajout test validation ([af54365](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/af54365d28c7d53f81eb27072c06b7fe9ee7e401))
* utiliser constante DATE_FORMAT_PATTERN dans sendOverdueNotification ([2085991](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/2085991e2d294aefca9579f09c4d20fc99105ead))
* utiliser SpringBootTest au lieu de WebMvcTest pour TicketControllerTest ([63739cd](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/63739cd737090a333b57bee9e586a4d0f936646c))


### Features

* ajouter dÃ©pendance docker-build sur security checks + pinner Trivy ([e8a5e37](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/e8a5e37e8d980f0af6c4f2fad5d6c21407ad6972))
* amÃ©liorer pipeline CI/CD - Sonar rÃ©utilise coverage, nettoyage Docker sÃ©curisÃ©, gating sur main ([27f9697](https://github.com/oumaymasaoudi/hotel-tickets-backend/commit/27f9697316bd9a9ea7493ad85a3389b0abff6ed9))
