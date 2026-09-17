# Omnichannel E-Commerce Microservices

The repository contains four database-per-service Spring Boot applications:

- Product Catalog (`8081`)
- Inventory (`8082`)
- Order (`8083`)
- Payment (`8084`)

Nginx exposes the public API at `http://localhost:8080`. PostgreSQL stores four
independent databases and RabbitMQ carries payment status events.

## Consistency model

1. Order Service retrieves authoritative product snapshots from Product Service.
2. Order Service reserves every line through Inventory Service. Partial failure
   releases successful reservations and marks the order failed.
3. Payment Service checks amount and currency against Order Service.
4. Payment mutations and payment outbox inserts commit atomically.
5. The outbox publisher emits `payment.status.*` events to RabbitMQ.
6. Order Service consumes those events. A successful payment confirms inventory;
   a failed payment releases inventory. Consumers and inventory transitions are
   idempotent.
7. Active inventory reservations expire automatically after fifteen minutes.

No service queries another service's database and no cross-service foreign key
exists.

## Run

```bash
cp .env.example .env
docker compose up --build
```

RabbitMQ management is available at `http://localhost:15672` using the
credentials configured in `.env`.
The API contract is in `docs/openapi.yaml`.
Prometheus is available at `http://localhost:9090` and Grafana at
`http://localhost:3000`.

## Build without Docker

Each project has an H2-backed test profile and can be built independently:

```bash
cd product_service && ./gradlew build
cd ../inventory_service && ./gradlew build
cd ../order_service && ./gradlew build
cd ../payment_service && ./gradlew build
```

Runtime schemas are managed by Flyway; Hibernate runs with `ddl-auto=validate`.
For production, provide database/RabbitMQ secrets through environment variables,
publish only the gateway port, terminate TLS at Nginx or a cloud load balancer,
and block database and broker ports at the firewall.

The Flash Sale scenario in `load-tests/inventory-flash-sale.js` drives 500
reservation requests per second by default. Seed the requested SKU first, then
run it with k6 and verify `reserved <= on_hand` in Inventory Service.
