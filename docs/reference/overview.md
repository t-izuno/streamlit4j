# Reference

> **Status**: placeholder. Full element catalog (40 widgets / layouts / charts)
> will land via TASK-117.

Until then, the authoritative sources are:

- [`docs/specification.md`](../specification) — wire-level protocol and DTO contracts
- [`docs/design.md`](../design) — runtime architecture
- `core/src/main/java/io/streamlit4j/core/St.java` — public API surface (Javadoc)

`mvn -P release -DskipTests package` produces `*-javadoc.jar` for each module.
