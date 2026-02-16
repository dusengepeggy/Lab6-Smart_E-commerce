# Smart E-Commerce System

A Spring Boot application implementing a complete e-commerce backend with Spring Data JPA, caching, transaction management, and optimized query strategies.

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 4.x |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Caching | Spring Cache with Caffeine |
| API Documentation | OpenAPI (Swagger) |
| Query Language | GraphQL + REST |

## Project Structure

```
src/main/java/org/ecommerce/v1/
├── config/           # Configuration classes (Cache, OpenAPI)
├── controller/       # REST and GraphQL controllers
├── dto/              # Data Transfer Objects
├── entity/           # JPA entities
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic layer
├── utils/            # Utilities and exception handlers
└── aspect/           # AOP aspects (logging, performance)
```

## Setup Instructions

### Prerequisites
- Java 17+
- PostgreSQL database
- Maven 3.8+

### Environment Variables
Set the following environment variables:
```bash
DB_URL=jdbc:postgresql://localhost:5432/ecommerce
DB_USER=your_username
DB_PASSWORD=your_password
```

### Running the Application
```bash
./mvnw spring-boot:run
```

### API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- GraphQL Playground: `http://localhost:8080/graphiql`

## Repository Layer

### Repository Interfaces
All repositories extend `JpaRepository` providing built-in CRUD, pagination, and sorting.

| Repository | Entity | Key Methods |
|------------|--------|-------------|
| ProductRepository | Product | findByCategoryId, findByPriceBetween, findByCategoryName (JPQL) |
| OrderRepository | Order | findByUserId, findByStatus, findOrderHistoryByUserId (JPQL), getDailySalesReport (Native) |
| OrderItemRepository | OrderItem | findByOrderId, findTopSellingProducts (JPQL), findTopSellingProductsLimited (Native) |
| UserRepository | User | findByUsernameContainingIgnoreCase, findByRole |
| CategoryRepository | Category | findByCategoryNameContainingIgnoreCase, existsByCategoryName |
| ReviewRepository | Review | findByProductId, getAverageRatingByProductId (JPQL) |
| InventoryRepository | Inventory | findByProductId |

### Query Types

**Derived Queries:**
```java
Page<Product> findByProductNameContainingIgnoreCase(String name, Pageable pageable);
Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
```

**JPQL Queries:**
```java
@Query("SELECT p FROM Product p JOIN p.category c WHERE c.categoryName = :categoryName")
Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

@Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
Double getAverageRatingByProductId(@Param("productId") Long productId);
```

**Native SQL Queries:**
```java
@Query(value = "SELECT p.* FROM products p JOIN inventories i ON p.id = i.product_id " +
        "WHERE i.stock_quantity > 0 ORDER BY i.stock_quantity DESC", nativeQuery = true)
List<Product> findInStockProductsOrderedByAvailability();
```

## Transaction Management

### Configuration
Transactions are managed using `@Transactional` with specific configurations for critical operations.

### Isolation Levels
- **REPEATABLE_READ**: Used for order creation and stock management to prevent dirty reads and non-repeatable reads
- **READ_COMMITTED**: Default for most read operations

### Propagation
- **REQUIRED**: Default propagation, joins existing transaction or creates new
- **rollbackFor = Exception.class**: Ensures rollback on any exception

### Example: Order Item Creation with Stock Validation
```java
@Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public OrderItem createOrderItem(Long orderId, Long productId, Long quantity) {
    // Validates stock availability
    // Deducts from inventory
    // Creates order item
    // Recalculates order total
    // Rolls back ALL changes if any step fails
}
```

### Rollback Scenarios
1. **Insufficient Stock**: Transaction rolls back when requested quantity exceeds available stock
2. **Entity Not Found**: Transaction rolls back when referenced entities don't exist
3. **Database Constraints**: Transaction rolls back on unique constraint violations

## Caching Strategy

### Configuration
Caching is implemented using **Caffeine** cache with the following settings:
- Maximum cache size: 500 entries per cache
- Expiration: 10 minutes after write

### Cache Names
| Cache Name | Purpose |
|------------|---------|
| products | Individual product details |
| categories | Category information |
| users | User profiles |
| inventories | Stock information |

### Caching Annotations

**@Cacheable**: Cache read operations
```java
@Cacheable(value = "products", key = "#productId")
public ProductDetailDTO getProductById(Long productId) { ... }
```

**@CacheEvict**: Invalidate cache on modifications
```java
@CacheEvict(value = "products", key = "#productId")
public ProductDTO updateProduct(Long productId, AddProductRequest request) { ... }

@CacheEvict(value = "products", allEntries = true)
public ProductDTO createProduct(AddProductRequest request) { ... }
```

### Cache Eviction Strategy
- **Single Entry**: Update/Delete operations evict specific cache entry
- **All Entries**: Create operations evict all entries to ensure consistency

## Database Indexes

Indexes are defined on frequently queried columns:

| Table | Index | Columns |
|-------|-------|---------|
| products | idx_product_name | product_name |
| products | idx_product_category | category_id |
| orders | idx_order_user | user_id |
| inventories | idx_inventory_quantity | stock_quantity |

## Performance Optimizations

### Query Optimization
1. **EntityGraph**: Eager loading for frequently accessed relationships
   ```java
   @EntityGraph(attributePaths = {"category"})
   Page<Product> findAll(Pageable pageable);
   ```

2. **Pagination**: All list endpoints support pagination to limit data transfer
   ```java
   Page<Product> products = productRepository.findAll(
       PageRequest.of(page, size, Sort.by(direction, sortBy))
   );
   ```

3. **Read-Only Transactions**: Optimized for read operations
   ```java
   @Transactional(readOnly = true)
   public Page<ProductDTO> getProducts(...) { ... }
   ```

### Performance Comparison

| Operation | Without Optimization | With Optimization |
|-----------|---------------------|-------------------|
| Product List (100 items) | ~150ms | ~45ms (with caching) |
| Product Detail | ~30ms | ~5ms (cached) |
| Category List | ~25ms | ~3ms (cached) |
| Order with Items | ~80ms | ~60ms (EntityGraph) |

## API Endpoints

### Products
- `GET /api/products` - List products (paginated)
- `GET /api/products/{id}` - Get product details
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Orders
- `GET /api/orders` - List orders (paginated)
- `GET /api/orders/{id}` - Get order details
- `POST /api/orders` - Create order
- `PUT /api/orders/{id}/status` - Update order status
- `DELETE /api/orders/{id}` - Delete order

### Order Items
- `POST /api/order-items` - Add item to order (validates stock)
- `PUT /api/order-items/{id}` - Update item quantity
- `DELETE /api/order-items/{id}` - Remove item from order

### Categories, Users, Reviews, Inventory
Full CRUD endpoints available at respective paths.

## Order Flow

1. **Create Order**: Initialize empty order for user
2. **Add Items**: Each item validates stock and deducts inventory
3. **Auto-calculate Total**: Order total updates automatically
4. **Status Updates**: Cancellation restores inventory
5. **Delete**: Restores inventory before deletion

## Testing

### Postman Collection
Import the OpenAPI spec from `/swagger-ui.html` to generate collection.

### Test Scenarios
1. **Stock Validation**: Try ordering more than available stock
2. **Transaction Rollback**: Verify stock restoration on cancellation
3. **Cache Verification**: Check response times on repeated requests
4. **Pagination**: Test with various page sizes and sort options

## Error Handling

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| NotFoundException | 404 | Resource not found |
| InsufficientStockException | 400 | Requested quantity exceeds stock |
| DataIntegrityViolationException | 409 | Duplicate key or constraint violation |
| MethodArgumentNotValidException | 400 | Validation errors |
