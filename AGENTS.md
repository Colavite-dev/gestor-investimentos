## Autonomous SDD Workflow

When working autonomously, use the repository as the primary source of project context.

Before proposing the next meaningful feature:

1. inspect the current OpenSpec state;
2. read the relevant stable specs;
3. read PROFESSOR_REQUIREMENTS.md;
4. read PRD.md;
5. consult ARCHITECTURE.md and DECISIONS.md;
6. inspect the existing implementation and tests;
7. identify which required behavior is still missing.

Do not select a new feature merely because it seems useful.

Prioritize:
1. unfinished mandatory academic requirements;
2. required integration and validation work;
3. delivery/documentation requirements;
4. optional improvements and differentiators.

## OpenSpec Lifecycle

For meaningful features, follow:

Explore → Propose → Review → Apply → Validate → Review → Sync → Archive

### Explore / Propose

When asked to continue development and no specific change was provided:

1. inspect the repository;
2. identify the next logical unfinished requirement;
3. explain any important ambiguity;
4. create an OpenSpec change;
5. create proposal.md;
6. create design.md;
7. create tasks.md;
8. create only the necessary delta specs;
9. validate the change with OpenSpec strict.

Do not implement production code during PROPOSE.

After PROPOSE:
- report the proposed change;
- report architectural decisions;
- report task count;
- report affected specs;
- report expected tests;
- report migrations if any;
- report blockers.

STOP and wait for approval before APPLY.

### Apply

After explicit approval:

1. read the complete approved change;
2. inspect affected implementation;
3. implement all approved tasks;
4. keep tasks.md updated;
5. run focused tests during development;
6. avoid repeatedly running the full build after small edits;
7. run the full Maven verification near the end;
8. validate the OpenSpec change with strict validation;
9. compare implementation against the approved specs.

Fix failures directly caused by the change autonomously when safe.

Do not expand scope to unrelated features.

After APPLY:
- report completed tasks;
- report files changed;
- report tests executed;
- report build result;
- report OpenSpec validation;
- report deviations or blockers.

STOP before sync/archive and wait for approval.

### Sync / Archive

Only after explicit approval:

1. sync the delta specs into stable specs;
2. validate the resulting stable specs;
3. archive the completed change;
4. confirm that no completed change remains active.

Do not modify the approved implementation during archive unless required to correct a directly related specification problem.

## Validation Strategy

Use focused validation during implementation.

Run `mvn verify` once near the end of APPLY unless a failure requires another run.

Do not repeatedly start PostgreSQL/Docker merely as a routine check.

Real PostgreSQL validation is especially relevant when changing:
- Flyway migrations;
- JPA mappings;
- database constraints;
- datasource/database infrastructure.

If none of these changed, existing automated tests may be sufficient unless the feature specifically requires database verification.

## External Integration Tests

Normal automated tests must not depend on internet availability.

Use mocks/test doubles for normal provider tests.

Real external API tests must be opt-in.

Run real provider tests only when they provide meaningful validation of:
- authentication;
- endpoint contract;
- parser compatibility;
- provider-specific behavior.

Avoid unnecessary real API calls because providers may have rate limits.

Never print API keys or tokens.

## Repository Consistency

When determining expected behavior, consider:

1. mandatory academic requirements;
2. approved product requirements;
3. stable OpenSpec specifications;
4. architecture and recorded decisions;
5. current implementation and tests.

These sources serve different purposes and must not be silently overwritten by one another.

If they conflict in a way that changes required behavior, report the conflict before proceeding.

Stable OpenSpec specs describe behavior already accepted into the system.

Current code is implementation evidence, not permission to override an approved specification.

## Scope Control

Do not implement future features while completing the current change.

Do not add:
- speculative abstractions;
- unrelated refactors;
- optional libraries;
- new database structures;
- new external providers;

unless required by the approved change.

Prefer reusing existing abstractions and conventions.

## Git and Destructive Operations

Unless explicitly requested, do not:

- commit;
- push;
- pull;
- merge;
- rebase;
- force push;
- publish releases;
- deploy;
- delete database volumes;
- perform destructive database operations;
- change global Git configuration;
- install global system dependencies.

Do not use destructive commands such as database volume deletion merely to obtain a clean environment.

## Autonomous Decisions

Small, reversible implementation decisions that are already constrained by the approved design may be made autonomously.

Do not stop for confirmation for:
- naming local implementation details;
- adding focused tests;
- fixing compilation errors caused by the current change;
- correcting formatting;
- small refactors necessary to implement the approved design.

Stop and report before proceeding when:
- requirements materially conflict;
- a destructive operation appears necessary;
- a new migration was not anticipated;
- public API behavior would materially differ from the proposal;
- an external provider contract invalidates the approved design;
- secrets or credentials would need unsafe handling;
- scope would need to expand substantially.

## Git and GitHub Responsibility

Codex is also responsible for helping maintain the local Git repository and GitHub remote safely.

Git/GitHub work is separate from feature implementation.

Before any write operation involving Git or GitHub:

1. inspect the current repository state;
2. inspect local branches;
3. inspect configured remotes;
4. inspect differences between local and remote history;
5. inspect tracked, modified and untracked files;
6. inspect `.gitignore`;
7. verify that secrets and local-only files are not included;
8. explain the planned operation and its risks.

Read-only Git operations may be performed autonomously, including:

- git status
- git diff
- git diff --cached
- git log
- git branch
- git remote -v
- git fetch
- git ls-files
- git show
- git rev-list
- git merge-base

`git fetch` is allowed because it updates remote-tracking references without modifying the working tree.

Do NOT automatically perform:

- git add
- git commit
- git push
- git pull
- git merge
- git rebase
- git reset
- git clean
- git restore that discards changes
- git checkout that discards changes
- force push
- branch deletion
- tag deletion
- history rewriting

without explicit approval.

Never use `git push --force` or `--force-with-lease` unless explicitly authorized for a specific operation.

Never discard local work to make the repository clean.

Never delete untracked files unless their purpose has been reviewed and explicit approval was given.

## GitHub Safety

Before pushing:

1. fetch the remote;
2. compare local `main` with `origin/main`;
3. determine whether the branches are:
    - synchronized;
    - local ahead;
    - remote ahead;
    - diverged;
4. inspect the commits on each side;
5. verify that the commit does not include secrets;
6. verify that build artifacts, IDE files and local configuration are excluded.

If local and remote history diverge:

STOP.

Do not automatically pull, merge, rebase or force push.

Report:

- common ancestor;
- local-only commits;
- remote-only commits;
- modified files;
- untracked files;
- safest reconciliation options.

Wait for approval.

## Secrets Before Commit

Before staging or committing, inspect for likely secrets.

At minimum verify:

- `.env` is ignored;
- `.env.backup` is ignored;
- API keys are not tracked;
- database passwords are not tracked;
- tokens are not tracked;
- local IDE/runtime artifacts are not accidentally included.

Never display actual secret values in reports.

If a secret appears to be tracked:

STOP before commit or push.

Report the file and type of secret without reproducing the credential.

## Commit Discipline

When a commit is explicitly approved:

- include only reviewed project files;
- avoid unrelated local files;
- do not mix generated artifacts with source changes unless needed;
- use a concise descriptive commit message;
- show the staged diff summary before committing;
- report the resulting commit hash.

Do not commit automatically at the end of every OpenSpec change unless explicitly requested.

## Push Discipline

When a push is explicitly approved:

1. fetch first;
2. verify local/remote relationship again;
3. push only the intended branch;
4. never force by default;
5. report the remote and resulting branch state.

If the push is rejected:

STOP.

Do not automatically force, rebase, reset or merge.

Investigate and report the reason.