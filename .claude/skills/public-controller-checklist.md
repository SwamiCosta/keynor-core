# Skill: Public Controller Checklist

> Step-by-step procedure for adding a new `Public*Controller`. Read before creating any unauthenticated, read-only controller under `/api/public/v1/`.

---

## Adding a new public controller

1. Create `Public*Controller` in `infrastructure/web/<domain>/` (same package as `Internal*Controller`)
2. Inject `FindAll*UseCase` and `FindById*UseCase` only
3. Fix the `EntityFilter` to `List.of(EntityStatus.CANON)` — never expose other statuses
4. Map with `PagedResponse.from(result, *Response::from)`
5. No new DTOs needed if the existing `*Response` already covers the required fields
6. Ask Judis to add unit tests (see `.claude/skills/unit-testing-controllers.md`)

For the public API's invariants, available endpoints, and query parameters, see `CLAUDE.md` — "Public API".

---

*Maintained by Imaws. Update when the public controller pattern changes (e.g. new mandatory dependency, new response shape).*
