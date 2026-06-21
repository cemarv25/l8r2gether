---
name: feature-delivery-workflow
description: "Use when Codex should run a repeatable multi-agent feature delivery workflow from project specification and UI designs: intake, feature decomposition, feature review, task decomposition, task review, implementation, task code review, draft GitHub PR creation, and PR monitoring for CI failures or comments without merging. Trigger when the user asks to split product work into features/tasks, coordinate multiple agents, create traceable markdown planning artifacts under documentation/features, implement approved tasks, open a PR, or watch a PR after creation."
---

# Feature Delivery Workflow

Run a gated feature delivery workflow that keeps product intent, UI designs, task scope, implementation, review evidence, and PR follow-up traceable through markdown artifacts.

## Artifact Root

Default to `documentation/features` unless the user provides another directory.

Create these directories as needed:

```text
documentation/features/
  intake/
  features/
  reviews/features/
  tasks/
  reviews/tasks/
  implementation/
  reviews/work/
  prs/
```

Use stable kebab-case IDs:

- Feature IDs: `feature-001-short-name`
- Task IDs: `task-001-short-name`
- Review IDs: reuse the reviewed artifact ID plus `-review`
- PR monitor IDs: `pr-<number>-monitor`

For task artifacts, use the path shape requested by the user:

```text
documentation/features/tasks/<taskId>.md
```

If a feature has many tasks, optionally group with a feature subdirectory only after asking the user.

## Reference Templates

Use the bundled templates when creating artifacts:

- `references/intake-template.md`
- `references/feature-template.md`
- `references/feature-review-template.md`
- `references/task-template.md`
- `references/task-review-template.md`
- `references/implementation-note-template.md`
- `references/work-review-template.md`
- `references/pr-template.md`
- `references/pr-monitor-template.md`

Load only the templates needed for the current stage.

## Operating Rules

- Keep every artifact traceable to source files, UI designs, feature IDs, task IDs, commits, or PR links.
- Treat each review stage as a gate. Do not proceed from feature planning to task planning, or from task planning to implementation, when major questions remain unresolved unless the user explicitly accepts the risk.
- Ask for human approval after feature review and task review before implementing unless the user has explicitly delegated approval.
- Default to multiple agents for this workflow when subagents are available: one supervisor, planners, reviewers, workers, an integration owner, and an optional PR monitor. If subagents are unavailable or the work is too small, keep the same roles as explicit passes in a single-agent workflow.
- The supervisor owns process quality at every stage: verify source coverage, enforce traceability, check gates, review agent outputs, prevent scope drift, coordinate file ownership, and decide whether to ask the user before proceeding.
- Assign disjoint responsibilities and write scopes to worker agents. Do not assign two agents to edit the same files unless the supervisor explicitly sequences the work.
- Never merge the GitHub PR. PR monitoring may inspect CI, inspect comments, propose fixes, implement approved fixes, and push follow-up commits, but must not merge.
- Prefer draft PRs unless the user asks for a ready PR.
- Maintain an open questions list. If an assumption affects scope, data model, UX, security, billing, or migration behavior, surface it before implementation.
- Include screenshots or visual verification evidence for UI changes when practical.
- For Android app UI flows, use the installed `android-emulator-qa` skill when available to validate the flow with adb-driven emulator launch, UI-tree inspection, screenshots, and logcat capture. Record emulator serial, build variant, screenshots, logs, and pass/fail evidence in task implementation and review artifacts.
- Respect existing repo conventions and avoid unrelated refactors.

## Workflow

### 1. Intake

Read the project specification, UI designs, relevant code, and any user constraints. Create an intake artifact in `documentation/features/intake/`.

The supervisor reviews the intake before feature decomposition and confirms that sources, assumptions, open questions, and risk areas are explicit.

Capture:

- source map with paths or URLs
- product goals
- UI surfaces
- hard requirements
- non-goals
- technical constraints
- open questions
- risk areas

Use `references/intake-template.md`.

### 2. Feature Decomposition

Split the work into user-meaningful features based on the spec and designs. Create one markdown file per feature in `documentation/features/features/`.

Use planner agents where useful to split independent product areas. The supervisor reconciles overlapping features before review.

Each feature must include:

- linked source references
- user outcome
- in-scope and out-of-scope behavior
- UI/design references
- dependencies
- risks
- acceptance criteria at feature level
- candidate tasks, if obvious

Use `references/feature-template.md`.

### 3. Feature Review

Review the feature set against the project specification and UI designs. Use a separate reviewer agent by default, and have the supervisor review the reviewer output before deciding whether to continue.

Create review artifacts in `documentation/features/reviews/features/`.

Ask:

- Does each feature align with the spec and designs?
- Is anything missing?
- Is anything over-scoped?
- Are dependencies and sequencing clear?
- Are acceptance criteria testable?
- Are open questions blocking?

Use `references/feature-review-template.md`.

Stop for user approval if the review finds material issues.

### 4. Task Decomposition

Break approved features into small tasks. Create one task markdown file at:

```text
documentation/features/tasks/<taskId>.md
```

Each task must include:

- parent feature ID
- source references
- precise requirements
- non-goals
- likely files or modules
- dependencies and ordering
- test scenarios
- acceptance criteria
- review checklist
- implementation notes

The supervisor checks task boundaries, dependencies, file ownership, and sequencing before task review.

Use `references/task-template.md`.

### 5. Task Review

Review each task against its parent feature before implementation. Use a separate reviewer agent by default, and have the supervisor review the reviewer output before assigning work.

Create review artifacts in `documentation/features/reviews/tasks/`.

Ask:

- Does the task align with the feature?
- Is the task small enough to implement and review?
- Is scope clear?
- Are requirements testable?
- Are acceptance criteria objective?
- Are dependencies or file ownership conflicts visible?

Use `references/task-review-template.md`.

Stop for user approval if the review finds material issues.

### 6. Implementation

Assign task work to worker agents by default. Give each worker a task ID, artifact path, and disjoint file/module ownership when possible. Tell workers they are not alone in the codebase and must not revert unrelated edits.

The supervisor tracks active workers, checks for file ownership conflicts, and reviews each worker result before integration. For Android UI tasks, assign or run an Android QA pass with `android-emulator-qa` when available.

For each completed task, create or update an implementation note in `documentation/features/implementation/` with:

- task ID
- summary of changes
- files changed
- tests run
- screenshots or verification artifacts
- Android emulator screenshots, UI-tree summaries, and logcat excerpts when applicable
- known limitations
- commit or branch references when available

Use `references/implementation-note-template.md`.

### 7. Work Review

Review implemented task work against best practices, maintainability, repo conventions, and the task definition. Use a separate reviewer agent by default, and have the supervisor decide whether the work is ready for integration.

Create review artifacts in `documentation/features/reviews/work/`.

Ask:

- Does the implementation satisfy the task acceptance criteria?
- Is the code maintainable and idiomatic for this repo?
- Are tests meaningful?
- Are edge cases handled?
- For Android UI flows, does emulator evidence support the claimed behavior?
- Are there unrelated changes?
- Is there enough evidence to include in the PR?

Use `references/work-review-template.md`.

### 8. Integration And PR

One integration owner should reconcile the final branch, run relevant tests, check formatting, verify traceability, and prepare the PR. The supervisor performs a final process review before PR creation.

Create a PR artifact in `documentation/features/prs/` with:

- PR title and link
- related feature and task IDs
- implementation summary
- test evidence
- screenshots for UI changes
- risks and follow-ups
- explicit note: `Do not merge by automation`

Use `references/pr-template.md`.

### 9. PR Monitoring

Watch the PR for CI failures and comments when requested. Create or update a monitor artifact in `documentation/features/prs/`.

Use a PR monitor agent when available. The supervisor reviews monitor findings before any follow-up implementation and enforces the no-merge rule.

For each check or comment cycle, record:

- timestamp
- CI status
- failed jobs and log links
- actionable comments
- decisions
- fixes pushed
- remaining work

Do not merge the PR.

Use `references/pr-monitor-template.md`.
