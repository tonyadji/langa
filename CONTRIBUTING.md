# Contributing to Langa

Thanks for your interest! Langa is a work in progress: issues and pull requests are welcome.

## Workflow

1. Open an [issue](https://github.com/tonyadji/langa/issues) to discuss the change first (bug, feature, doc).
2. Branch from `develop`: `feature/<name>`, `fix/<name>` or `docs/<name>`.
3. Keep commits in [Conventional Commits](https://www.conventionalcommits.org/) style,
   e.g. `feat(backend): add live tail endpoint`, `fix(agent): …`, `docs: …`.
4. Open the pull request against `develop`. CI must be green.

## Checks before opening a PR

| Module | Command |
|---|---|
| Agent | `cd agent && mvn -B verify` |
| Backend | `cd backend && ./mvnw -B verify` |
| Dashboard | `cd frontend && npm run check:all` |

## Conventions

- **Backend** — the domain (`domain/`) must not depend on Spring or on `infra/`; new behaviour goes through a
  use case, persistence and external services through ports implemented in `infra/adapters`.
- **Tests** — every use case and adapter comes with unit tests; bugs are fixed with a failing test first.
- **Docs** — update the relevant README or `backend/documents` page in the same PR.
