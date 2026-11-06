# Assignment 06 

- **Student:** Maksut Alibi
- **Group:** SE2523
- **Repository URL:** https://github.com/M1ska2021/Assignment6

---

## 1. Project Overview

This project implements an in-memory hierarchical file system based on the **Composite** and **Visitor** behavioral design patterns in Java 17. The solution decouples the tree structure (`Element`, `FileNode`, `DirectoryNode`) from operations applied to it, supporting three independent operations without runtime type checking:

1. **TotalSizeVisitor:** Computes the cumulative byte size of all files across arbitrary nesting levels.
2. **SearchVisitor:** Locates file paths matching configurable name filters and minimum size thresholds, returning deterministic root-relative paths.
3. **StatisticsVisitor:** Gathers aggregate metrics including total file count, directory count, and maximum tree depth.

---

## 2. Architecture & Role Map

| Role | Class / Interface | Location | Responsibility |
|---|---|---|---|
| **Element Interface** | `Element` | `model/Element.java` | Declares common hierarchy methods (`getName()`, `getParent()`, `setParent()`) and `accept(Visitor)` |
| **Concrete Element 1** | `FileNode` | `model/FileNode.java` | Represents a leaf file holding name and non-negative byte size; delegates `accept` |
| **Concrete Element 2** | `DirectoryNode` | `model/DirectoryNode.java` | Represents a composite directory holding named children; enforces tree integrity |
| **Visitor Interface** | `Visitor` | `visitor/Visitor.java` | Declares overloaded `visit(FileNode)` and `visit(DirectoryNode)` methods |
| **Concrete Visitor 1** | `TotalSizeVisitor` | `visitor/TotalSizeVisitor.java` | Accumulates file sizes across subtree; directories contribute zero bytes |
| **Concrete Visitor 2** | `SearchVisitor` | `visitor/SearchVisitor.java` | Combines name and size constraints; returns sorted root-relative paths |
| **Concrete Visitor 3 (Ext)** | `StatisticsVisitor` | `visitor/StatisticsVisitor.java` | Collects file count, directory count (including root), and maximum depth |
| **Harness / Client** | `Main` | `Main.java` | Executes deterministic demonstration on realistic multi-level file hierarchy |
| **Automated Tests** | `FileSystemVisitorTest` | `test/.../FileSystemVisitorTest.java` | Full JUnit 5 verification covering checkpoints T1 through T8 |

---

## 3. How to Build, Test, and Run

### Prerequisites
- Java 17 or higher
- Apache Maven 3.8+ (or use the included `./mvnw` script)

### Running Automated Tests
```bash
mvn clean test
```
Or using the included wrapper:
```bash
./mvnw clean test
```

### Running the Demonstration Application
```bash
mvn compile exec:java
```
Or directly via Java:
```bash
java -cp target/classes kz.aitu.se2523.visitor.Main
```

---

## 4. Key Design Decisions

### Double Dispatch Mechanism
- Every element implements `accept(Visitor visitor)` by delegating to `visitor.visit(this)`.
- At compile time, `this` binds to the concrete type (`FileNode` or `DirectoryNode`).
- At runtime, polymorphism selects the concrete visitor implementation.
- This eliminates the need for `instanceof`, reflection, or casting chains.

### Traversal Ownership (Visitor-Managed)
- Visitors control tree traversal by iterating children inside `visit(DirectoryNode)`.
- This keeps `DirectoryNode.accept()` strictly minimal and allows operations like `SearchVisitor` and `StatisticsVisitor` to maintain context-aware stacks (path segment tracking and tree depth) during navigation.

### State Control & Reusability
- Each visitor provides an explicit `reset()` method.
- Furthermore, visitor executions automatically re-initialize state when invoked on a new root (`activeDepth == 0`), preventing stale results or duplicated metrics on repeat executions.

### Encapsulation and Tree Invariants
- `DirectoryNode.getChildren()` returns an unmodifiable list via `Collections.unmodifiableList()`.
- Mutation is only possible through `DirectoryNode.add(Element child)`, which strictly guards against:
  - Negative file sizes (`IllegalArgumentException`)
  - Duplicate sibling names (`IllegalArgumentException`)
  - Multiple parent ownership (`IllegalStateException`)
  - Cyclic references (`IllegalStateException`)

---

## 5. Verification Results (T1–T8)

```text
[INFO] Running kz.aitu.se2523.visitor.FileSystemVisitorTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s
[INFO] BUILD SUCCESS
```

- **T1:** Both element types reach correct overloaded visit methods.
- **T2:** Size accumulation across multiple nesting levels is correct.
- **T3:** Search correctly combines both configurable filter conditions.
- **T4:** Search produces correct, deterministic, root-relative paths.
- **T5:** Statistics are correct for nesting, counting, and maximum depth.
- **T6:** Empty structures and no-match search cases are handled.
- **T7:** Repeat executions do not leak or multiply accumulated state.
- **T8:** Invalid tree mutations are rejected while valid nesting remains supported.
