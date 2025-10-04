# Testing Guide

This Spring Boot project has been enhanced with comprehensive testing capabilities including unit tests, integration tests, and advanced testing tools.

## Test Summary

- **Total Tests**: 127 (all test types now auto-running)  
- **Surefire Plugin**: 89 repository tests (unit tests)
- **Failsafe Plugin**: 38 integration tests (service + web + e2e)
- **Test Classes**: 7  
- **Test Coverage**: Full coverage with JaCoCo reporting
- **All Tests Passing**: ✅ 100% success rate

## Test Structure

### Repository Tests (89 tests) - *Runs with Surefire*

- `StudentRepositoryTest` (14 tests) - Student data layer testing with custom queries
- `DepartmentRepositoryTest` (23 tests) - Department repository functionality  
- `CourseRepositoryTest` (21 tests) - Course repository operations
- `EnrollmentRepositoryTest` (31 tests) - Enrollment repository with complex relationships

### Integration Tests (38 tests) - *Runs with Failsafe*

**Service Integration Tests (13 tests):**
- `StudentServiceIntegrationTest` nested classes - Business logic validation
  - `BatchOperations` (2 tests)
  - `StudentGradeOperations` (1 test)
  - `StudentEnrollmentOperations` (3 tests)  
  - `StudentSearchOperations` (2 tests)
  - `StudentCRUDOperations` (5 tests)

**Web Integration Tests (12 tests):**
- `StudentControllerWebIntegrationTest` nested classes - Web layer integration with MockMvc
  - `StudentErrorHandlingApiTests` (2 tests)
  - `StudentBatchOperationsApiTests` (1 test)
  - `StudentEnrollmentApiTests` (1 test)
  - `StudentSearchApiTests` (2 tests)
  - `StudentCRUDApiTests` (6 tests)

**End-to-End Integration Tests (13 tests):**
- `EndToEndIntegrationTest` nested classes - Complete workflow testing from HTTP to database
  - `CompleteStudentJourneyTests` (2 tests)
  - `ErrorHandlingE2ETests` (2 tests)
  - `PerformanceE2ETests` (3 tests)
  - `DataIntegrityE2ETests` (2 tests)
  - `EdgeCasesE2ETests` (2 tests)
  - `SecurityE2ETests` (2 tests)

**All integration tests now auto-run with `./mvnw clean verify` thanks to updated Maven configuration.**

## Running Tests

### Basic Test Execution

```bash
# Run all 118 tests (unit + integration) - RECOMMENDED
./mvnw clean verify

# Run all tests with coverage report
./mvnw clean verify jacoco:report

# Run tests quietly (less output)
./mvnw clean verify -q
```

### Maven Profiles for Different Test Types

#### 1. All Tests (Default - 127 tests)
```bash
# Profile is active by default
./mvnw clean verify

# Or explicitly specify the profile
./mvnw clean verify -P all-tests
```
**Results:** 89 repository tests (Surefire) + 38 integration tests (Failsafe) = **127 total tests**

#### 2. Unit Tests Only (89 tests)
```bash
./mvnw clean test -P unit-tests
```
Runs only repository unit tests with Surefire plugin.

#### 3. Integration Tests Only (38 tests)
```bash
./mvnw clean verify -P integration-tests -DskipTests=true
```
Runs only integration tests (service + web + e2e) with Failsafe plugin.

## Advanced Testing Tools Available

### 1. Testcontainers
For real database testing instead of H2 in-memory database:
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

Example usage:
```java
@SpringBootTest
@Testcontainers
class StudentRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### 2. REST Assured
For API testing with fluent syntax:
```java
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Test
void shouldGetStudentById() {
    given()
        .port(port)
    .when()
        .get("/api/students/1")
    .then()
        .statusCode(200)
        .body("firstName", equalTo("John"))
        .body("lastName", equalTo("Doe"));
}
```

### 3. WireMock
For mocking external services:
```java
@Test
void shouldCallExternalService() {
    WireMockServer wireMockServer = new WireMockServer(8089);
    wireMockServer.start();
    
    wireMockServer.stubFor(get(urlEqualTo("/external-api/data"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\":\"success\"}")));
    
    // Your test code here
    
    wireMockServer.stop();
}
```

### 4. JaCoCo Code Coverage
Generate coverage reports:
```bash
./mvnw test jacoco:report
```

View coverage report at: `target/site/jacoco/index.html`

## Test Configuration Features

### Maven Surefire Plugin
- Runs unit tests during `test` phase
- Configured to exclude integration tests by default in unit-tests profile
- Parallel execution support available

### Maven Failsafe Plugin
- Runs integration tests during `integration-test` phase
- Handles test lifecycle properly for integration scenarios
- Configured for proper resource management

### Test Profiles Configuration

The project includes three Maven profiles:

1. **unit-tests**: Excludes `*IntegrationTest.java` files
2. **integration-tests**: Includes only `*IntegrationTest.java` files  
3. **all-tests**: Includes all test files (default)

## Current Test Features

### Repository Layer Testing
- Custom query testing
- Pagination and sorting
- Complex criteria queries
- Data validation

### Web Layer Testing
- MockMvc integration
- JSON response validation
- HTTP status code verification
- Error handling testing

### End-to-End Testing
- Full application context
- Database transactions
- Complete workflow testing
- Real HTTP requests

### Test Data Management
- Unique UUID-based test data generation
- Proper transaction isolation with `@DirtiesContext`
- Automatic cleanup between tests

## Best Practices Implemented

1. **Test Isolation**: Each test is independent and doesn't affect others
2. **Unique Test Data**: UUID-based generation prevents data conflicts
3. **Proper Annotations**: Correct use of Spring Boot test annotations
4. **Transaction Management**: Proper handling of database transactions
5. **Content Type Handling**: Correct JSON serialization/deserialization
6. **Error Testing**: Comprehensive error scenario coverage

## Adding New Tests

### Repository Test Example
```java
@DataJpaTest
class NewRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private NewRepository repository;
    
    @Test
    void shouldFindByCustomCriteria() {
        // Test implementation
    }
}
```

### Integration Test Example
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NewControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldHandleNewEndpoint() throws Exception {
        mockMvc.perform(get("/api/new-endpoint"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
```

## Continuous Integration

The enhanced configuration supports CI/CD pipelines with:
- Fast unit test execution
- Separate integration test phase
- Code coverage reporting
- Multiple database support via Testcontainers

## Troubleshooting

### Common Issues and Solutions

1. **Test Data Conflicts**: Use UUID-based unique identifiers
2. **Transaction Issues**: Add `@DirtiesContext` for test isolation
3. **MockMvc Not Found**: Ensure `@AutoConfigureMockMvc` is present
4. **JSON Serialization**: Check `@JsonManagedReference`/`@JsonBackReference` usage
5. **Port Conflicts**: Use `@SpringBootTest(webEnvironment = RANDOM_PORT)`

### Debugging Tests
```bash
# Run tests with debug output
./mvnw test -X

# Run specific test class
./mvnw test -Dtest=StudentRepositoryTest

# Run specific test method
./mvnw test -Dtest=StudentRepositoryTest#shouldFindByEmail
```

This comprehensive testing setup provides a robust foundation for maintaining code quality and ensuring application reliability.
