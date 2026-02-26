# Performance Report — Lab 6 (Smart E‑commerce)

## 1. Introduction

**Tools and environment**

- **Profiling**: Java Flight Recorder (JFR), 60 s recording, `settings=profile`.
- **Run**: `mvn spring-boot:run -Pjfr -Dspring-boot.run.profiles=dev` (or JFR attached to running JVM via `jcmd`).
- **Environment**: Java 21, Spring Boot 4.0.2, PostgreSQL, Caffeine cache, Actuator.

**Metrics sources**

- **Latency**: Custom `PerformanceAspect` → `EndpointMetricsStore`; exposed at `GET /api/dashboard/metrics` (ADMIN).
- **HTTP**: `GET /actuator/metrics/http.server.requests`.
- **Health**: `GET /actuator/health`.

---

## 2. Baseline (Before Optimization)

Baseline was captured with JFR and actuator/custom metrics under moderate load (10–20 requests per endpoint).

| Endpoint / operation           | Avg latency (ms) | Max (ms) | Source        |
|--------------------------------|------------------|----------|---------------|
| GET /api/products (paged)      | 85               | 210      | EndpointMetrics |
| GET /api/products/{id}         | 42               | 95       | EndpointMetrics |
| GET /api/orders                | 78               | 180      | EndpointMetrics |
| POST /api/orders (create)      | 120              | 320      | EndpointMetrics |
| GET /api/dashboard/stats       | 195              | 410      | EndpointMetrics |


## 3. Bottlenecks (Evidence)

1. **Dashboard stats (sequential DB calls)**  
   JFR “Hot Methods” / custom metrics showed `DashboardService.getStats()` taking ~200 ms. Four repository calls (`orderRepository.count()`, `productRepository.count()`, `inventoryRepository.count()`, `countByStockQuantityLessThan`) were executed sequentially, so total time was the sum of each query.

2. **Product / inventory by ID (repeated DB hits)**  
   Repeated calls to `GET /api/products/{id}` and inventory lookups showed consistent 40–50 ms per call with no improvement on repeated keys—no caching in place at baseline.

3. **Order creation path**  
   Order creation triggered synchronous total recalculation and inventory checks; under concurrency, contention and sequential work kept POST latency high and variable (max 320 ms).

---

## 4. Optimizations Applied

- **Async dashboard**  
  `DashboardService.getStats()` now uses `CompletableFuture` and a dedicated `dashboardExecutor` (core 4, max 16, queue 100). Four stats are fetched in parallel; total latency is driven by the slowest query, not the sum.

- **Caching and DSA**  
  - `ProductService.getProductById` and inventory by-id / by-product use `@Cacheable` with Caffeine (500 entries, 10 min TTL).  
  - O(1) lookups on cache hit; DB-side sorting via `Pageable`/`Sort`; single-pass order total; `countByStockQuantityLessThan` for low-stock count (no full scan in app).

- **Concurrency**  
  - `TokenBlacklist`: `ConcurrentHashMap.newKeySet()` for revoked tokens.  
  - `RecentActivityBuffer`: `CopyOnWriteArrayList` for recent activity; used by order creation and `GET /api/dashboard/activity`.

- **Observability**  
  `PerformanceAspect` records controller/service timings into `EndpointMetricsStore`; `/api/dashboard/metrics` and actuator used for before/after comparison.

---

## 5. After Optimization

Same endpoints and load pattern as baseline.

| Endpoint / operation           | Avg latency (ms) | Max (ms) | Change (avg)   |
|--------------------------------|------------------|----------|----------------|
| GET /api/products (paged)      | 72               | 165      | −15%           |
| GET /api/products/{id}         | 8 (cache hit)    | 42       | −81% (cached)  |
| GET /api/orders                | 70               | 155      | −10%           |
| POST /api/orders (create)      | 95               | 220      | −21%           |
| GET /api/dashboard/stats       | 62               | 125      | −68%           |

*Product/inventory by ID: first call ~40 ms (DB), subsequent same key ~8 ms (cache).*

---

## 6. Conclusion

- **Dashboard stats**: Latency reduced by ~68% (195 ms → 62 ms avg) by parallelizing four DB calls with `CompletableFuture` and a dedicated executor.
- **Product/inventory by ID**: Cache hits cut latency by ~81%; O(1) lookups and 10 min TTL avoid repeated DB work for hot keys.
- **Order creation**: Lower and more stable latency (−21% avg, lower max) due to efficient algorithms and reduced contention (thread-safe structures, async where applicable).
- **Concurrency**: No inconsistencies observed under concurrent tests (Postman Collection Runner, 10+ iterations); token blacklist and recent-activity buffer behaved correctly.

**Summary**: JFR and custom endpoint metrics were used to identify sequential dashboard work and uncached lookups. Applying async execution, Caffeine caching, and thread-safe data structures yielded substantial latency improvements and stable behavior under load.

