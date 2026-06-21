# Task: task-004-home-ui-verification-pr Home UI Verification And PR

Status: Draft
Parent feature: `documentation/features/features/feature-001-home-screen-ui.md`

## Source References

| Source | Reference | Requirement / Design Detail |
| --- | --- | --- |
| Feature | `documentation/features/features/feature-001-home-screen-ui.md` | Build/visual verification, work review, draft PR, and PR babysitting setup. |
| Skill | `.codex/skills/feature-delivery-workflow/SKILL.md` | Implementation notes, work reviews, PR artifact, and PR monitoring artifact are required final workflow stages. |
| Skill | `/Users/cesar/.codex/plugins/cache/openai-curated-remote/test-android-apps/0.1.2/skills/android-emulator-qa/SKILL.md` | Android emulator QA evidence should be captured when practical. |
| Design | `screen-mockup-images/home-screen-no-sessions.png` | Empty-state visual verification target. |
| Design | `screen-mockup-images/home-screen-with-sessions.png` | Populated-state visual verification target. |

## Objective

Verify the UI work, record implementation/review/PR evidence, create a draft PR, and start PR monitoring without merging.

## Requirements

- Run the relevant Gradle build/compile/check command available in the repo.
- Capture emulator screenshot/UI-tree/logcat evidence for the home flow when practical, using `android-emulator-qa`.
- If emulator evidence is not possible, document the blocker and use build/static evidence instead.
- Create implementation notes for completed tasks under `documentation/features/implementation/`.
- Create work review artifacts under `documentation/features/reviews/work/`.
- Create a PR artifact under `documentation/features/prs/`.
- Commit intentionally, push `feature/home-screen-ui`, and open a draft PR.
- Create or update a PR monitor artifact as the PR babysitting final step.
- Do not merge the PR.

## Non-Goals

- No PR merge.
- No CI fix implementation unless PR monitoring finds a failure and the user approves follow-up scope.
- No backend work.

## Likely Files / Modules

- `documentation/features/implementation/*.md`: implementation notes.
- `documentation/features/reviews/work/*.md`: work reviews.
- `documentation/features/prs/*.md`: PR and monitor artifacts.
- Git branch `feature/home-screen-ui`: commit/push/draft PR.

## Dependencies And Ordering

- Depends on task-001, task-002, and task-003 implementation.
- Must run after code changes and work review.
- Draft PR creation depends on GitHub access and successful push.
- PR monitoring occurs after draft PR creation.

## Test Scenarios

- Build verification: Gradle command succeeds, or failure is documented with logs and scope.
- Visual verification: captured evidence shows empty and/or populated home state close to the mockups when environment allows.
- PR workflow: draft PR exists and PR artifact records link, summary, risks, and no-merge note.
- PR babysitting: monitor artifact records initial CI/comment status or documents why monitoring could not run.

## Android QA Plan

- Applies: yes
- Skill: `android-emulator-qa` when available
- Build variant: debug
- Emulator/device: available emulator at verification time
- Flow to validate: launch app home, capture screenshot, dump UI tree, collect logcat/crash evidence
- Evidence required: screenshots, UI-tree summary, logcat excerpt or documented reason not available

## Acceptance Criteria

- [ ] Relevant Gradle verification has been run and recorded.
- [ ] Visual/emulator verification evidence is captured or a clear blocker is documented.
- [ ] Implementation notes and work review artifacts exist for completed implementation tasks.
- [ ] Draft PR artifact exists with explicit `Do not merge by automation` note.
- [ ] Draft PR is created and pushed if GitHub access allows.
- [ ] PR monitor artifact records initial babysitting status.

## Review Checklist

- [ ] Aligns with parent feature
- [ ] Meets requirements and non-goals
- [ ] Handles relevant states and errors
- [ ] Tests or verification cover acceptance criteria
- [ ] Android emulator QA evidence is captured when applicable
- [ ] Avoids unrelated refactors

## Implementation Notes

Use connector GitHub tools for PR creation when possible. Use `gh` only as fallback where connector coverage is insufficient.

## Open Questions

- [ ] None blocking.
