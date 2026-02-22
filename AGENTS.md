# AGENTS.md

This document defines mandatory rules for AI coding agents working in this repository.  
Follow these instructions exactly.

---

## Principles

- Always choose safe and reversible actions.
- Make the smallest, most focused change necessary to accomplish the task.
- Preserve the existing architecture and conventions.
- When unsure or if a task appears to require large, unrelated, or destructive changes, always stop and ask for clarification or instructions.

---

## Language Policy

- All commit messages must be written in Japanese.
- Code comments should be written in Japanese unless the existing codebase uses English-only comments.
 
---

## Guardrails (Must Follow)

- Never perform destructive operations.
- Never rewrite shared history (e.g., force push, unsafe rebase).
- Never modify repository internals (e.g., `.git`).

If a task appears to require any of the above, **stop and ask for instructions**.

---

## Required Workflow (Must Follow)

For any code change:

1. Create a working branch from `main`.
2. Make the required changes.
3. Run tests according to the Testing Policy before committing.
4. Commit the changes.
5. Provide a clear summary of the work.

Do not modify code without creating a branch and commit.

---

## Scope of Work

- Limit all changes to this repository.
- Do not edit generated files or directories such as:
  - `build/`
  - `coverage/`
  - `node_modules/`
  - `dist/`

---

## Test Commands

- JavaScript tests: `npm run test`
- Full test suite: `npm run test:full`

---

## Testing Policy

Never bypass failing tests. Prefer fixing the root cause over disabling tests.
Do not skip tests to save time.

Run tests based on the type of change:

- Run the JavaScript tests only when ALL modified files match `*.js`.
- If ANY file not matching `*.js` is modified, run the full test suite.
- When unsure, run the full test suite.

If tests cannot be executed, report:

- the reason (e.g., permissions, environment issues)
- that the tests were not run

### Test Exceptions

Tests are not required for the following changes:

- CSS-only changes that do not affect JavaScript behavior
- Developer-documentation-only changes (i.e., changes that qualify for the `docs` commit type, such as `README.md` or files under `docs/`).

If there is any possibility that runtime behavior is affected, run the tests.

---

## Coding Guidelines

- Follow existing implementation patterns.
- Write comments in Japanese.
- If existing comments are English-only, do not force translation — mixed language is acceptable.

---

## Change Policy

- For any new feature or bug fix, adding or updating tests is REQUIRED.
- Tests must demonstrate the failure before the change and pass after the change, when feasible.
- Tests may be omitted ONLY IF adding or updating tests is impractical.
- When tests are omitted, clearly explain the reason (e.g., technical constraints, environment limitations).

- After changes, check for duplication and refactor only if it is clearly safe.
- Perform refactoring in a SEPARATE commit from functional changes.

---

## Commit Policy

- Commit in meaningful, minimal units.
- Split commits when a task naturally involves multiple steps.

### Commit Messages

Use **Conventional Commits**:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Allowed `type` values:

- `feat` — New feature, or changes affecting user-facing functionality/content (e.g., UI text, new reports).
- `fix` — bug fix
- `refactor` — code improvement without behavior change
- `docs` — Developer-focused documentation (e.g., `README.md`, files under `docs/`). For user-facing content changes, use `feat`.
- `test` — test additions or updates
  - Use `test(red)` when adding a failing test
- `other` — if none apply

Footer:
- When adding a footer, please use the `--trailer` option like `git commit -m "..." --trailer "KEY: VALUE"`.
- `JIG-DOCUMENT: <documentName>` — when a modified JigDocument is identifiable.
- `AGENT: <agentName>` — when the commit is made by an automated agent. `<agentName>` should be the specific name of the agent (e.g., `Gemini`, `Codex`, `Copilot`).

### Commit Message Style

Keep commit messages concise and factual.

- Do not exaggerate impact or value.
- Do not include promotional language.
- Avoid unnecessary background or justification.
- Describe what changed, not why it is great.

Prefer short bodies. Omit the body if it is not necessary.

#### JIG-DOCUMENT Rule

If a change targets a specific JigDocument, add a footer:
`JIG-DOCUMENT: <documentName>`

Determine the document name from [`JigDocument.java`](jig-core/src/main/java/org/dddjava/jig/domain/model/documents/documentformat/JigDocument.java):

- If a modified file matches `jig-core/src/main/resources/templates/<documentFileName>.html`,
  the `JIG-DOCUMENT` footer should use the corresponding enum *name* from `JigDocument.java`.
- If a modified file matches `jig-core/src/main/resources/templates/assets/<documentFileName>.*`,
  and the change explicitly targets a document, the `JIG-DOCUMENT` footer should use the corresponding enum *name* from `JigDocument.java`.
- If multiple documents match, add all relevant `JIG-DOCUMENT` footers.
- For shared assets (e.g., `assets/jig.js`) that affect multiple documents, add a footer
  **only when the task explicitly targets a document**. Otherwise omit to avoid false attribution.
