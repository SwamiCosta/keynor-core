# Skill: Unit Testing Controllers (Judis)

> Applies to all REST controllers in `infrastructure/web/`.
> Read before writing or reviewing any `*Test.java` for a controller class.

---

## Scope and purpose

Controller unit tests verify that a controller:

1. Passes the **correct filter invariants** to use cases (e.g. CANON-only on public endpoints)
2. Forwards **pagination parameters** without distortion
3. **Maps the response** correctly from domain types to DTOs
4. **Delegates** to the right use case with the right arguments

They do **not** test Spring MVC routing, HTTP serialization, or security filters — those belong to integration tests.

---

## Framework decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Test runner | JUnit 5 (`@ExtendWith(MockitoExtension.class)`) | No Spring context needed — controllers are plain Java objects |
| Mocking | Mockito (`@Mock` + `when/verify`) | Use cases are interfaces; no real beans required |
| Assertions | AssertJ (`assertThat`) | Fluent, readable, consistent with the rest of the test suite |
| Controller instantiation | Direct constructor (`new Controller(mock1, mock2)`) | Avoids Spring context startup overhead |

**Never use `@SpringBootTest` or `@WebMvcTest` for controller unit tests.** Those load the application context and are reserved for integration tests.

---

## Standard test structure

```java
@ExtendWith(MockitoExtension.class)
class PublicFooControllerTest {

    @Mock
    private FindAllFoosUseCase findAllFoosUseCase;

    @Mock
    private FindFooByIdUseCase findFooByIdUseCase;

    private PublicFooController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicFooController(findAllFoosUseCase, findFooByIdUseCase);
    }

    // tests...
}
```

---

## Required test cases per public controller

Every `Public*Controller` must have these four tests:

### 1. Filter is always CANON

Public endpoints must never leak DRAFT or DEPRECATED entities. Use `ArgumentCaptor` to verify the filter passed to the use case.

```java
@Test
void findAll_shouldAlwaysFilterByCanonStatus() {
    when(findAllFoosUseCase.findAll(any(), any()))
            .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

    controller.findAll(null, null, 0, 20);

    ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
    verify(findAllFoosUseCase).findAll(filterCaptor.capture(), any());
    assertThat(filterCaptor.getValue().statuses()).containsExactly(EntityStatus.CANON);
}
```

> This is the highest-priority test. If the CANON invariant breaks, the frontend displays unfinished content to users.

### 2. Pagination params are forwarded correctly

```java
@Test
void findAll_shouldPassPaginationParamsToUseCase() {
    when(findAllFoosUseCase.findAll(any(), any()))
            .thenReturn(new PageResult<>(List.of(), 2, 50, 0));

    controller.findAll(null, null, 2, 50);

    ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(findAllFoosUseCase).findAll(any(), pageCaptor.capture());
    assertThat(pageCaptor.getValue().page()).isEqualTo(2);
    assertThat(pageCaptor.getValue().size()).isEqualTo(50);
}
```

### 3. PageResult is mapped to PagedResponse correctly

Build a real domain object (not a mock) so the `*Response::from` factory runs end-to-end.

```java
@Test
void findAll_shouldReturnMappedPagedResponse() {
    Instant now = Instant.now();
    UUID id = UUID.randomUUID();
    Foo foo = new Foo(id, "Name", "Summary", "Body", List.of("tag"),
            List.of(FooCategory.SOME_CATEGORY), EntityStatus.CANON, null, now, now);
    when(findAllFoosUseCase.findAll(any(), any()))
            .thenReturn(new PageResult<>(List.of(foo), 0, 20, 1));

    var response = controller.findAll(null, null, 0, 20);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    PagedResponse<FooResponse> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.totalElements()).isEqualTo(1);
    assertThat(body.content().get(0).id()).isEqualTo(id);
}
```

> Use a real domain object here — mocking the domain entity would bypass the `::from` factory and miss mapping bugs.

### 4. findById delegates and maps correctly

```java
@Test
void findById_shouldDelegateToUseCaseAndMapResult() {
    UUID id = UUID.randomUUID();
    Foo foo = new Foo(id, "Name", ...);
    when(findFooByIdUseCase.findById(id)).thenReturn(foo);

    var response = controller.findById(id);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id()).isEqualTo(id);
    verify(findFooByIdUseCase).findById(id);
}
```

---

## What NOT to test here

- HTTP status codes for exceptions (e.g. 404 on not found) — that is the `GlobalExceptionHandler`'s responsibility, tested separately
- JWT validation or CORS headers — those are Spring Security concerns, tested at the integration level
- Sorting or ordering of results — the controller does not sort; that is the repository's responsibility

---

## Naming conventions

| Test method name pattern | Example |
|--------------------------|---------|
| `findAll_should<Outcome>_when<Condition>` | `findAll_shouldAlwaysFilterByCanonStatus` |
| `findById_should<Outcome>_when<Condition>` | `findById_shouldDelegateToUseCaseAndMapResult` |

Test class name: `<ControllerClass>Test` (e.g. `PublicPlaceControllerTest`).
Location: mirrors the production package under `src/test/`.

---

*Maintained by Imaws. Update when new controller patterns are introduced.*
