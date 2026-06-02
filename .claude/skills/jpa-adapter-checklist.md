# Skill: JPA Adapter Pre-PR Checklist

> Imperium must run this checklist before opening any PR that creates or modifies a `*JpaAdapter.java`.

---

## Import conflict check

keynor-core has domain types whose simple names collide with Spring types. Importing both causes a compile-time error. Check the import block of every adapter against this table before committing:

| Domain type (simple name) | Conflicts with |
|---------------------------|----------------|
| `com.keynor.core.domain.model.shared.PageRequest` | `org.springframework.data.domain.PageRequest` |

### Required pattern

```java
// CORRECT — import only Spring's PageRequest
import org.springframework.data.domain.PageRequest;

// CORRECT — use FQN for domain PageRequest in the method signature
public PageResult<Foo> findAll(FooFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest) {
    PageRequest springPage = PageRequest.of(pageRequest.page(), pageRequest.size());
    // ...
}
```

```java
// WRONG — importing both causes "type with same simple name already defined"
import com.keynor.core.domain.model.shared.PageRequest;
import org.springframework.data.domain.PageRequest;
```

### How to catch it before committing

1. After writing the adapter, scan the import block for duplicate simple names.
2. If a new domain type is added that might clash, add it to the table above and report to Imaws so `CLAUDE.md` can be updated.

---

## Layer boundary check

Verify that the adapter:

- [ ] Has `@Component` (or is registered in a `@Configuration`)
- [ ] Implements the output port interface from `domain/port/out/`
- [ ] Has zero business logic — only maps, delegates to `jpaRepository`, and converts results
- [ ] Never exposes `*Entity` objects outside the `infrastructure/persistence/<module>/` package
- [ ] Uses `*Specifications.fromFilter()` for filtered queries (no inline Predicates in the adapter)

---

## Consistency check (for paginated adapters)

- [ ] `findAll` signature: `(EntityFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest)`
- [ ] Returns `PageResult<DomainType>` — never `Page<EntityType>` or `List<EntityType>`
- [ ] Imports: only `org.springframework.data.domain.PageRequest`, `org.springframework.data.domain.Page`, and `org.springframework.data.jpa.domain.Specification`

---

*Maintained by Imaws. Update when new naming conflicts are identified.*
