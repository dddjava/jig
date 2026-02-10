# AGENTS.md

This document defines mandatory rules for AI coding agents working in this repository.  
Follow these instructions exactly.

---

## Principles

- Choose safe and reversible actions.
- Make the smallest change necessary to accomplish the task.
- Preserve the existing architecture and conventions.
- When unsure, stop and ask for clarification.

---

## Language Policy

- All commit messages must be written in Japanese.
- All agent responses must be written in Japanese.
- Code comments should be written in Japanese unless the existing codebase uses English-only comments.
 
---

## Guardrails (Must Follow)

- Never perform destructive operations.
- Never rewrite shared history (e.g., force push, unsafe rebase).
- Never modify repository internals (e.g., `.git`).
- Do not make large or unrelated changes in a single task.

If a task appears to require any of the above, **stop and ask for instructions**.

---

## Required Workflow (Must Follow)

For any code change:

1. Create a working branch from `main`.
2. Make the required changes.
3. Run tests according to the Testing Policy.
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

If modification seems necessary, **stop and ask**.

---

## Test Commands

- JavaScript tests: `npm run test`
- Full test suite: `./gradlew clean test`

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
- Documentation-only changes (e.g., `README.md`, files under `docs/`)

If there is any possibility that runtime behavior is affected, run the tests.

When unsure, run the tests for the most specific applicable category.

---

## Coding Guidelines

- Follow existing implementation patterns.
- Keep changes minimal and focused on the request.
- Write comments in Japanese.
- If existing comments are English-only, do not force translation — mixed language is acceptable.

---

## Change Policy

- For any new feature or bug fix, adding or updating tests is REQUIRED.
- Tests must demonstrate the failure before the change and pass after the change, when feasible.
- Tests may be omitted ONLY IF adding or updating tests is impractical.
- When tests are omitted, clearly explain the reason (e.g., technical constraints, environment limitations).

- Avoid refactoring unrelated to the request.
- After changes, check for duplication and refactor only if it is clearly safe.
- Perform refactoring in a SEPARATE commit from functional changes.
- If uncertain whether refactoring is appropriate, report instead of modifying.

---

## Commit Policy

- Commit in meaningful, minimal units.
- Split commits when a task naturally involves multiple steps.
- Always run tests before committing.

### Commit Messages

Use **Conventional Commits**:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

Allowed `type` values:

- `feat` — new feature
- `fix` — bug fix
- `refactor` — code improvement without behavior change
- `docs` — documentation only
- `test` — test additions or updates
  - Use `test(red)` when adding a failing test
- `other` — if none apply

Footer:

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

Determine the document name from `JigDocument.java`:

- If a modified file matches `jig-core/src/main/resources/templates/<documentFileName>.html`,
  add the corresponding `JigDocument`.
- If a modified file matches `jig-core/src/main/resources/templates/assets/<documentFileName>.*`,
  add the corresponding `JigDocument`.
- If multiple documents match, add all relevant `JIG-DOCUMENT` footers.
- For shared assets (e.g., `assets/jig.js`) that affect multiple documents, add a footer
  **only when the task explicitly targets a document**. Otherwise omit to avoid false attribution.

---

## Branch Strategy

- Create a branch per request using: `agent/<topic>`
- Branch from `main`.
- Use short, lowercase, hyphenated names describing the work.

Example: `agent/package-glossary-link`

---

## Decision Rule

When multiple approaches are possible:

👉 **Choose the least invasive option.**

Do exactly what was requested — nothing more.
Do not proactively "improve" unrelated areas.