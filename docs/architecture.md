# Architecture

## System context

```mermaid
flowchart LR
    Recruiter([Recruiter / Reviewer])
    subgraph Docker["Docker Compose"]
        FE["Next.js Dashboard<br/>(:3000)"]
        BE["Spring Boot API<br/>(:8080)"]
        DB[("PostgreSQL 16")]
        PGA["pgAdmin<br/>(:5050, local only)"]
    end
    Recruiter --> FE
    Recruiter -->|Swagger UI| BE
    FE -->|"/api/backend/* (proxy)"| BE
    BE --> DB
    PGA --> DB
```

The browser never talks to the backend directly: the Next.js route handler
`app/api/backend/[...path]` proxies every call and injects the JWT stored in an
httpOnly cookie. This keeps tokens out of JavaScript and removes CORS from the
browser path.

## Hexagonal architecture (dependency rule)

```mermaid
flowchart TB
    subgraph Infrastructure
        WEB["in/web<br/>Controllers (generated OpenAPI APIs)"]
        PERSIST["out/persistence<br/>JPA adapters"]
        SEC["out/security<br/>JWT, hashing, current actor"]
        AUDIT["out/audit<br/>AOP JSONB snapshots"]
        PDF["out/report<br/>PDF + SQL reports"]
        DEMO["in/demo<br/>seeder + startup runner"]
    end
    subgraph Application
        SVC["services<br/>use cases, transactions, retries"]
    end
    subgraph Domain
        MODEL["model<br/>rich aggregates"]
        PORTS["ports/in · ports/out"]
        EX["exceptions (i18n codes)"]
    end
    WEB --> PORTS
    PERSIST --> PORTS
    SEC --> PORTS
    AUDIT --> PORTS
    PDF --> PORTS
    DEMO --> PORTS
    SVC -.implements.-> PORTS
    SVC --> MODEL
    MODEL --> EX
    PERSIST --> MODEL
```

ArchUnit (`HexagonalArchitectureTest`) fails the build if the domain starts
depending on Spring/Jakarta or if the application depends on infrastructure.

## Order-to-cash flow (sales + billing)

```mermaid
sequenceDiagram
    autonumber
    actor Sales
    participant API as SalesOrderController
    participant SVC as SalesOrderService
    participant INV as InventoryService
    participant BILL as InvoiceService
    participant DB as PostgreSQL

    Sales->>API: POST /sales-orders (customer, lines)
    API->>SVC: create(command)
    SVC->>DB: save DRAFT order
    Sales->>API: POST /sales-orders/{id}/confirm
    API->>SVC: confirm(id)
    SVC->>INV: reserve(product, warehouse, qty)  # @Version + @Retryable
    INV-->>SVC: reserved (409 if insufficient)
    SVC->>DB: status = CONFIRMED
    Sales->>API: POST /invoices {salesOrderId}
    API->>BILL: createFromOrder(id)
    BILL->>DB: insert invoice + lines
    BILL->>INV: ship(...)  # deducts stock
    BILL-->>API: invoice ISSUED
    Sales->>API: GET /invoices/{id}/pdf
    API-->>Sales: localized PDF
```

## Concurrency strategy (no overselling)

```mermaid
sequenceDiagram
    autonumber
    participant T1 as Thread A
    participant T2 as Thread B
    participant DB as inventory_items (quantity=1)

    T1->>DB: SELECT ... (version=7, qty=1)
    T2->>DB: SELECT ... (version=7, qty=1)
    T1->>DB: UPDATE qty=1, reserved=1 WHERE version=7
    DB-->>T1: 1 row (version -> 8) ✅
    T2->>DB: UPDATE ... WHERE version=7
    DB-->>T2: 0 rows -> OptimisticLockingFailure
    T2->>DB: retry: re-read (available=0) -> ConflictException 409
```

The retry lives *outside* the transaction (`@EnableRetry(order = HIGHEST_PRECEDENCE)`),
so each attempt gets a fresh persistence context.

## Data model (simplified)

```mermaid
erDiagram
    users ||--o{ user_roles : has
    roles ||--o{ user_roles : grants
    categories ||--o{ products : classifies
    products ||--o{ inventory_items : stocked
    warehouses ||--o{ inventory_items : stores
    products ||--o{ stock_movements : logs
    warehouses ||--o{ stock_movements : logs
    customers ||--o{ sales_orders : places
    sales_orders ||--o{ sales_order_lines : contains
    products ||--o{ sales_order_lines : references
    suppliers ||--o{ purchase_orders : fulfills
    purchase_orders ||--o{ purchase_order_lines : contains
    sales_orders ||--o| invoices : bills
    invoices ||--o{ invoice_lines : contains
    invoices ||--o{ invoice_payments : paid_by
    audit_log }o--|| users : acts
```

All monetary columns are `NUMERIC(12,2)`; `inventory_items`, `products`,
`sales_orders`, `purchase_orders` and `invoices` carry a `version` column driven
by JPA `@Version`.
