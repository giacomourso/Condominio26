# Condominio26 Auth Service

Servizio Spring Boot configurato come OAuth2 Resource Server con multi-tenancy basata su `tenant_id`.

## Endpoints principali

- `GET /api/tenants` — elenco tenants.
- `GET/PUT /api/tenants/{tenantId}` — recupero e aggiornamento con controllo `@PreAuthorize`.
- `GET/POST /api/tenants/{tenantId}/roles` — gestione ruoli per tenant.
- `GET/POST /api/tenants/{tenantId}/users` — gestione utenti e mapping ruoli.

Tutte le mutazioni generano una voce di audit (`audit_log`) con l’utente autenticato e il tenant corrente.

## Sicurezza e multi-tenant

- Resource Server JWT configurato con `spring.security.oauth2.resourceserver.jwt.issuer-uri` per integrarsi con l’Authorization Server.
- Il `TenantContext` viene propagato dal filtro `TenantContextFilter` validando gli header del Gateway:
  - `X-Tenant-Id`
  - `X-Tenant-Signature` (HMAC-SHA256 del tenant id con il segreto `gateway.signature.secret`)
- Le API con `{tenantId}` usano `@PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")` per vincolare il tenant.

## Database

- PostgreSQL con repository Spring Data JPA.
- Migrazioni gestite da Flyway (`src/main/resources/db/migration`).

## Esecuzione locale

1. Impostare le variabili nel `application.yaml` (datasource, issuer-uri, `gateway.signature.secret`).
2. Avviare un database PostgreSQL accessibile con le credenziali configurate.
3. Eseguire l’applicazione:

```bash
mvn spring-boot:run
```

Per un build offline, assicurarsi che le dipendenze Spring Boot siano raggiungibili dal registry Maven configurato.
