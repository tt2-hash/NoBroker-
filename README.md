# Student accommodation finder (Spring Boot)

Searches an internal database of accommodation records in Andheri and Vile Parle and links out to
NoBroker. It does not scrape NoBroker and does not claim live synchronisation.

## Run
    mvn spring-boot:run                      # opens on http://localhost:8080 with DEMO records
    ADMIN_KEY=choose-a-long-secret mvn spring-boot:run   # enables /api/admin/*

## Data sources (in priority order)
1. Authorized NoBroker access (API / partner / data-sharing agreement): none found publicly. If you obtain
   written authorization, implement it as a separate provider that maps into `ListingInput` and calls
   `ListingService.create`, honouring whatever fields and storage terms the agreement allows.
2. Manual entry or CSV import (`POST /api/admin/import`, template in `import-template.csv`) of facts you are
   permitted to use. Put your own wording in `name`/`notes`; do not paste NoBroker descriptions or photos.
3. DEMO records (seeded only when the table is empty; set `app.seed-demo=false` to disable).

## Rules the code enforces
- `externalUrl` must be https on nobroker.in; `source` and `lastChecked` are mandatory.
- Missing audience / sharing / food / amenities / availability stay NULL ("Not specified") and never match a filter.
- Edits reset verification; `POST /api/admin/listings/{id}/verify` marks attributes admin-verified.
