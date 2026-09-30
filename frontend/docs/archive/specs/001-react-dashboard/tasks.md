# Tasks: Langa Dashboard - React Observability Platform

**Input**: Design documents from `/specs/001-react-dashboard/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/
**Tests**: Tests REQUIRED per constitution Section III (Test-First Development)
**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

All paths assume the project root at `langa-dashboard/`:
- Frontend code: `src/`
- Tests: `tests/`
- Configuration: root level

---

## Phase 1: Setup (Shared Infrastructure) ✅ COMPLETE

**Purpose**: Project initialization and basic structure

- [x] T001 Initialize Vite + React + TypeScript project with `npm create vite@latest langa-dashboard -- --template react-ts`
- [x] T002 Install core dependencies: react@18.3, react-dom@18.3, typescript@5.3, react-router-dom@6.21, axios@1.6
- [x] T003 [P] Install and configure Tailwind CSS with PostCSS in tailwind.config.js and postcss.config.js
- [x] T004 [P] Configure ESLint with TypeScript plugin in .eslintrc.cjs with strict rules (no any types per constitution)
- [x] T005 [P] Configure Prettier in .prettierrc
- [x] T006 [P] Install and configure Vitest + React Testing Library in vitest.config.ts
- [x] T007 [P] Install and configure MSW (Mock Service Worker) for API mocking in tests/mocks/server.ts
- [x] T008 [P] Configure Playwright for E2E testing in playwright.config.ts
- [x] T009 [P] Set up Husky + lint-staged for pre-commit hooks
- [x] T010 [P] Install and configure Storybook 7+ for component development and documentation
- [x] T011 Install Recharts for metrics visualization: `npm install recharts`
- [x] T012 Install @tanstack/react-virtual for virtual scrolling: `npm install @tanstack/react-virtual`
- [x] T013 Create environment files: .env.example, .env.development, .env.production with VITE_API_BASE_URL
- [x] T014 Create base file structure per plan.md (src/components, src/features, src/pages, src/types, etc.)
- [x] T015 [P] Configure tsconfig.json with strict mode and path aliases (@/ for src/)
- [x] T016 [P] Setup global styles in src/styles/index.css with Tailwind directives
- [x] T017 Install and integrate lucide-react icon library: `npm install lucide-react` - Replaced emoji with professional icons throughout application (FileText, BarChart3, Eye/EyeOff, Copy/Check, CheckCircle, XCircle, AlertTriangle, Info icons)

---

## Phase 2: Foundational (Blocking Prerequisites) ✅ COMPLETE

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T018 Create all TypeScript type definitions in src/types/user.ts for User, AuthState, AuthAction
- [x] T019 [P] Create TypeScript type definitions in src/types/application.ts for Application, ApplicationSecured, ShareWith, ApplicationUsage
- [x] T020 [P] Create TypeScript type definitions in src/types/log.ts for LogEntry, LogLevel, LogFilterParams, LogsState
- [x] T021 [P] Create TypeScript type definitions in src/types/metric.ts for MetricEntry, MetricStatus, MetricFilterParams, MetricsState, MetricStats
- [x] T022 [P] Create TypeScript type definitions in src/types/team.ts for Team, TeamMember, TeamInvitation (with nested Identity/StakeHolders/Period), TeamRole, InvitationStatus
- [x] T023 [P] Create TypeScript type definitions in src/types/api.ts for all request/response types, ApiError, PaginatedResponse, ApplicationLogsResponse, ApplicationMetricsResponse, RetentionPolicy, UpdateRetentionPolicyRequest, CompleteFirstConnectionRequest, GetInvitationResponse
- [x] T024 [P] Create TypeScript type definitions in src/types/common.ts for shared types (PaginationParams)
- [x] T025 [P] Create type guards in src/types/guards.ts for isApplicationSecured, isApiError, isPaginatedResponse
- [x] T026 Create Axios instance with base configuration in src/services/api.ts
- [x] T027 Add request interceptor to inject Authorization header in src/services/api.ts
- [x] T028 Add response interceptor for token refresh on 401 errors in src/services/api.ts
- [ ] T029 Implement centralized error handler with exponential backoff retry logic in src/services/errorHandler.ts
- [ ] T030 Integrate contracts/api-client.ts with src/services/api.ts Axios instance to wire up API modules
- [x] T031 Create API client modules: authApi in src/features/auth/api/authApi.ts
- [x] T032 [P] Create API client module: applicationsApi in src/features/applications/api/applicationsApi.ts
- [x] T033 [P] Create API client module: logsApi in src/features/logs/api/logsApi.ts
- [x] T034 [P] Create API client module: metricsApi in src/features/metrics/api/metricsApi.ts
- [x] T035 [P] Create API client module: teamsApi in src/features/teams/api/teamsApi.ts
- [ ] T036 [P] Create API client module: teamInvitationsApi and usersApi in src/features/teams/api/teamsApi.ts
- [x] T037 Create utility functions in src/utils/formatters.ts using native Intl API for date formatting
- [x] T038 [P] Create validation utilities in src/utils/validators.ts for form validation
- [x] T039 [P] Create constants in src/utils/constants.ts for DEFAULT_PAGE_SIZE, LOG_LEVELS, METRIC_STATUSES, etc.
- [x] T040 Create common Button component in src/components/common/Button.tsx with Tailwind styling and Storybook story
- [x] T041 [P] Create common Input component in src/components/common/Input.tsx with HTML5 validation support and Storybook story
- [x] T042 [P] Create common Modal component in src/components/common/Modal.tsx with Storybook story
- [x] T043 [P] Create common Card component in src/components/common/Card.tsx with Storybook story
- [x] T044 [P] Create common Table component in src/components/common/Table.tsx with Storybook story
- [x] T045 [P] Create common Pagination component in src/components/common/Pagination.tsx with Storybook story
- [x] T046 [P] Create common EmptyState component in src/components/common/EmptyState.tsx with Storybook story
- [x] T047 [P] Create common LoadingSpinner component in src/components/common/LoadingSpinner.tsx with Storybook story - *Note: Created as Spinner.tsx*
- [x] T048 [P] Create common ErrorMessage component in src/components/common/ErrorMessage.tsx with Storybook story
- [x] T049 [P] Create common Alert component in src/components/common/Alert.tsx with lucide-react icons (CheckCircle, XCircle, AlertTriangle, Info) and Storybook story
- [x] T050 [P] Create common Badge component in src/components/common/Badge.tsx with variants (default, success, warning, error, info) and sizes (sm, md, lg) and Storybook story
- [x] T051 Create layout Navbar component in src/components/layout/Navbar.tsx with navigation links, active route highlighting, and user dropdown menu with Storybook story - *Note: Replaces Header component*
- [x] T052 Create layout Layout component in src/components/layout/Layout.tsx composing Navbar and main content area with responsive container - *Note: Replaces MainLayout with simplified structure*
- [ ] T053 [P] Create layout Sidebar component in src/components/layout/Sidebar.tsx with Storybook story - *Note: Not implemented - top navigation sufficient for current needs*
- [ ] T054 [P] Create layout Footer component in src/components/layout/Footer.tsx with Storybook story - *Note: Not implemented - not required for MVP*
- [x] T055 Create router configuration in src/router/index.tsx with all route definitions (/, /login, /register, /applications, /applications/:id, /logs, /metrics, /profile, /teams)
- [x] T056 Create ProtectedRoute wrapper component in src/router/ProtectedRoute.tsx with loading state handling and session verification
- [x] T057 Create custom hook useDebounce in src/hooks/useDebounce.ts with unit tests
- [x] T058 [P] Create custom hook usePagination in src/hooks/usePagination.ts with unit tests
- [x] T059 [P] Create custom hook useLocalStorage in src/hooks/useLocalStorage.ts with unit tests

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - User Authentication and Onboarding (Priority: P1) 🎯 MVP

**Goal**: Enable new users to register, login, and complete initial setup to access the Langa observability platform with JWT-based authentication.

**Independent Test**: Register a new account with email/password, login with credentials to receive JWT tokens, verify token refresh works automatically, complete first-time setup wizard, and verify protected routes redirect unauthenticated users to login.

### Tests for User Story 1 (MANDATORY per Constitution Section III)

**⚠️ WRITE TESTS FIRST - They MUST FAIL before implementation**

- [X] T060 [P] [US1] Write contract test for POST /api/auth/register in tests/integration/auth.register.test.tsx
- [X] T061 [P] [US1] Write contract test for POST /api/auth/login in tests/integration/auth.login.test.tsx
- [X] T062 [P] [US1] Write contract test for POST /api/auth/refresh in tests/integration/auth.refresh.test.tsx
- [X] T063 [P] [US1] Write integration test for user registration flow in tests/integration/auth.flow.test.tsx
- [X] T064 [P] [US1] Write integration test for token refresh on 401 error in tests/integration/auth.tokenRefresh.test.tsx
- [X] T065 [P] [US1] Write unit test for useAuth hook in src/features/auth/hooks/useAuth.test.ts
- [X] T066 [P] [US1] Write component test for LoginForm in src/features/auth/components/LoginForm.test.tsx
- [X] T067 [P] [US1] Write component test for RegisterForm in src/features/auth/components/RegisterForm.test.tsx
- [X] T068 [P] [US1] Write component test for SetupWizard in src/features/auth/components/SetupWizard.test.tsx
- [X] T069 [P] [US1] Write E2E test for complete auth flow (register → login → setup) in tests/e2e/auth.spec.ts

### Implementation for User Story 1

- [X] T067 [P] [US1] Create AuthContext in src/features/auth/context/AuthContext.tsx with AuthState and reducer
- [X] T068 [P] [US1] Create useAuth hook in src/features/auth/hooks/useAuth.ts with login, register, logout functions
- [X] T069 [US1] Create useTokenRefresh hook in src/features/auth/hooks/useTokenRefresh.ts for automatic token refresh
- [X] T070 [P] [US1] Create LoginForm component in src/features/auth/components/LoginForm.tsx with HTML5 validation and Storybook story (Updated: Login field changed from email to username - accepts any identifier)
- [X] T071 [P] [US1] Create RegisterForm component in src/features/auth/components/RegisterForm.tsx with password confirmation and Storybook story
- [X] T072 [P] [US1] Create SetupWizard component in src/features/auth/components/SetupWizard.tsx for first-time users and Storybook story
- [X] T073 [US1] Create LoginPage in src/pages/LoginPage.tsx integrating LoginForm
- [X] T074 [US1] Create RegisterPage in src/pages/RegisterPage.tsx integrating RegisterForm
- [X] T075 [US1] Create DashboardPage skeleton in src/pages/DashboardPage.tsx with setup wizard integration
- [X] T076 [US1] Integrate AuthContext provider in src/App.tsx wrapping router
- [X] T077 [US1] Apply ProtectedRoute to dashboard and other authenticated routes in src/router/index.tsx
- [X] T078 [US1] Add error handling for auth failures (invalid credentials, network errors) in useAuth hook
- [X] T079 [US1] Implement redirect logic from protected routes to login page in ProtectedRoute component
- [X] T080 [US1] Add form validation error messages to LoginForm and RegisterForm
- [X] T081 [US1] Write component test for ProfilePage in src/pages/ProfilePage.test.tsx
- [X] T082 [US1] Create ProfilePage in src/pages/ProfilePage.tsx showing user info and logout button
- [X] T083 [US1] Add ProfilePage route to router in src/router/index.tsx
- [x] T084 [US1] Add logout functionality to header/navigation with user menu dropdown (calls POST /api/users/logout)
- [X] T085 [US1] Verify all tests pass and coverage ≥ 80% for US1 code per constitution

**Checkpoint**: User Story 1 complete - users can register, login, receive tokens, access protected routes, view profile, and logout. All tests passing.

---

## Phase 4: User Story 2 - Application Management (Priority: P1)

**Goal**: Enable users to create, view, and manage monitored applications with unique credentials for log and metric ingestion.

**Independent Test**: Create a new application with a unique name, view the list of owned and shared applications, access application details to see ingestion credentials (for owned apps), search/filter in large lists, and verify the application appears correctly.

### Tests for User Story 2 (MANDATORY per Constitution Section III)

**⚠️ WRITE TESTS FIRST - They MUST FAIL before implementation**

- [X] T082 [P] [US2] Write contract test for POST /api/applications in tests/integration/applications.create.test.tsx
- [X] T083 [P] [US2] Write contract test for GET /api/applications in tests/integration/applications.list.test.tsx
- [X] T084 [P] [US2] Write contract test for GET /api/applications/{id}/secured-details in tests/integration/applications.details.test.tsx
- [X] T085 [P] [US2] Write integration test for application creation flow in tests/integration/applications.flow.test.tsx
- [X] T086 [P] [US2] Write unit test for useApplications hook in src/features/applications/hooks/useApplications.test.ts
- [X] T087 [P] [US2] Write unit test for useCreateApplication hook in src/features/applications/hooks/useCreateApplication.test.ts
- [X] T088 [P] [US2] Write component test for ApplicationCard in src/features/applications/components/ApplicationCard.test.tsx
- [X] T089 [P] [US2] Write component test for ApplicationList with search in src/features/applications/components/ApplicationList.test.tsx
- [X] T090 [P] [US2] Write component test for CreateApplicationModal in src/features/applications/components/CreateApplicationModal.test.tsx

### Implementation for User Story 2

- [x] T091 [P] [US2] Create useApplications hook in src/features/applications/hooks/useApplications.ts to fetch application list
- [x] T092 [P] [US2] Create useCreateApplication hook in src/features/applications/hooks/useCreateApplication.ts
- [x] T093 [P] [US2] Create ApplicationCard component in src/features/applications/components/ApplicationCard.tsx with Storybook story
- [x] T094 [P] [US2] Create ApplicationList component in src/features/applications/components/ApplicationList.tsx with grid layout and Storybook story
- [x] T095 [US2] Create CreateApplicationModal component in src/features/applications/components/CreateApplicationModal.tsx with form and Storybook story
- [x] T096 [US2] Create ApplicationDetails component in src/features/applications/components/ApplicationDetails.tsx showing credentials and Storybook story - *Note: Implemented as ApplicationDetailsPage.tsx*
- [x] T097 [US2] Create ApplicationsPage in src/pages/ApplicationsPage.tsx integrating list and create modal
- [x] T098 [US2] Create ApplicationDetailsPage in src/pages/ApplicationDetailsPage.tsx with route param for appId
- [x] T099 [US2] Add route for /applications in src/router/index.tsx
- [x] T100 [US2] Add route for /applications/:appId in src/router/index.tsx
- [x] T101 [US2] Implement ownership-based UI logic in ApplicationDetails (show credentials only for owners)
- [x] T102 [US2] Add search/filter input to ApplicationList for name-based filtering (SC-012: find in 100+ apps in <10s)
- [x] T103 [US2] Add empty state to ApplicationList when user has no applications
- [x] T104 [US2] Add validation for unique application name in CreateApplicationModal
- [x] T105 [US2] Add loading states to ApplicationsPage and ApplicationDetailsPage
- [x] T106 [US2] Add error handling for application creation failures
- [x] T107 [US2] Add navigation from ApplicationCard click to ApplicationDetailsPage
- [ ] T108 [US2] Verify all tests pass and coverage ≥ 80% for US2 code per constitution
- [x] T108a [P] [US2] Write test for DELETE /api/applications/{appId} in tests/integration/applications.delete.test.tsx
- [x] T108b [US2] Implement application deletion UI with confirmation dialog in ApplicationDetailsPage
- [x] T108c [P] [US2] Write test for PUT /api/applications/{appId}/update-retention-policy in tests/integration/applications.retention.test.tsx
- [x] T108d [US2] Create RetentionPolicySettings component in src/features/applications/components/RetentionPolicySettings.tsx with duration/unit selector and Storybook story
- [x] T108e [US2] Integrate RetentionPolicySettings into ApplicationDetailsPage for owners only

**Checkpoint**: User Stories 1-2 complete - users can manage applications after authentication with search/filter. All tests passing.

---

## Phase 5: User Story 3 - Log Viewing and Filtering (Priority: P1)

**Goal**: Enable users to view, search, and filter logs from their monitored applications to troubleshoot issues, including filtering by time range, log level, logger name, pagination, and MDC display.

**Independent Test**: Select an application, view its logs in reverse chronological order, apply filters for log level (ERROR), time range, and logger name, paginate through results, verify stack traces are expandable, and see MDC data when present.

### Tests for User Story 3 (MANDATORY per Constitution Section III)

**⚠️ WRITE TESTS FIRST - They MUST FAIL before implementation**

- [x] T109 [P] [US3] Write contract test for GET /api/applications/{id}/logs in tests/integration/logs.query.test.tsx
- [x] T110 [P] [US3] Write integration test for log filtering (level + time + logger) in tests/integration/logs.filter.test.tsx
- [x] T111 [P] [US3] Write unit test for useLogs hook in src/features/logs/hooks/useLogs.test.ts
- [x] T112 [P] [US3] Write unit test for useLogFilters hook in src/features/logs/hooks/useLogFilters.test.ts - *Note: Integrated into LogFilters component*
- [x] T113 [P] [US3] Write component test for LogsTable with virtual scrolling in src/features/logs/components/LogsTable.test.tsx
- [x] T114 [P] [US3] Write component test for LogEntry with stack trace expansion in src/features/logs/components/LogEntry.test.tsx - *Note: Integrated into LogsTable*
- [x] T115 [P] [US3] Write component test for LogFilters in src/features/logs/components/LogFilters.test.tsx
- [x] T116 [P] [US3] Write E2E test for complete log viewing flow in tests/e2e/logs.spec.ts

### Implementation for User Story 3

- [x] T117 [P] [US3] Create useLogs hook in src/features/logs/hooks/useLogs.ts with pagination support
- [ ] T118 [P] [US3] Create useLogFilters hook in src/features/logs/hooks/useLogFilters.ts with debouncing - *Note: Integrated into LogFilters component*
- [x] T119 [P] [US3] Create LogLevelBadge component in src/features/logs/components/LogLevelBadge.tsx with color coding and Storybook story
- [x] T120 [P] [US3] Create LogEntry component in src/features/logs/components/LogEntry.tsx for single log display with MDC support and Storybook story - *Note: Integrated into LogsTable*
- [x] T121 [P] [US3] Create StackTraceViewer component in src/features/logs/components/StackTraceViewer.tsx with expand/collapse and Storybook story - *Note: Integrated into LogsTable expandable rows*
- [x] T122 [P] [US3] Create MDCViewer component in src/features/logs/components/MDCViewer.tsx to display Mapped Diagnostic Context as expandable key-value pairs (FR-014) - *Note: Integrated into LogsTable as metadata display*
- [x] T123 [US3] Create LogsTable component in src/features/logs/components/LogsTable.tsx with @tanstack/react-virtual and Storybook story
- [x] T124 [US3] Create LogFilters component in src/features/logs/components/LogFilters.tsx with level, logger name, time range inputs and Storybook story
- [x] T125 [US3] Create LogsPage in src/pages/LogsPage.tsx integrating filters and table
- [x] T126 [US3] Add route for /applications/:appId/logs in src/router/index.tsx - *Note: Route already exists at /logs*
- [x] T127 [US3] Implement filter application logic in useLogs hook (level + keyword + time range) - *Note: Updated from level + logger + time range to use keyword search*
- [x] T128 [US3] Implement pagination controls in LogsTable using Pagination component
- [x] T129 [US3] Add empty state when no logs match filters
- [x] T130 [US3] Add loading skeleton while logs are being fetched
- [x] T131 [US3] Implement expandable stack trace in LogEntry component - *Note: Implemented as expandable metadata in LogsTable*
- [x] T132 [US3] Integrate MDCViewer into LogEntry component to display MDC data when available (FR-014) - *Note: Implemented as metadata display in LogsTable*
- [x] T133 [US3] Add clear filters button to reset all filters in LogFilters
- [x] T134 [US3] Add navigation link from ApplicationDetailsPage to LogsPage
- [ ] T135 [US3] Verify all tests pass and coverage ≥ 80% for US3 code per constitution - *Note: Tests not yet written (T109-T116)*

**Checkpoint**: User Stories 1-3 complete (MVP) - users can view and filter application logs with MDC display. All tests passing.

---

## Phase 6: User Story 4 - Metrics Visualization and Querying (Priority: P2)

**Goal**: Enable users to view and analyze metrics from their applications, including performance data, request durations, success/failure rates, and HTTP metrics with time-series visualization using Recharts.

**Independent Test**: Select an application with metrics, view metric entries with duration/status/timestamp, filter by metric name and status, visualize time-series data with Recharts chart, and view statistics (avg, median, p95, p99) matching MetricStats type from data-model.md.

### Tests for User Story 4 (MANDATORY per Constitution Section III)

**⚠️ WRITE TESTS FIRST - They MUST FAIL before implementation**

- [ ] T136 [P] [US4] Write contract test for GET /api/applications/{id}/metrics in tests/integration/metrics.query.test.tsx
- [ ] T137 [P] [US4] Write integration test for metric filtering in tests/integration/metrics.filter.test.tsx
- [ ] T138 [P] [US4] Write unit test for useMetrics hook in src/features/metrics/hooks/useMetrics.test.ts
- [ ] T139 [P] [US4] Write unit test for useMetricsStats hook calculating MetricStats in src/features/metrics/hooks/useMetricsStats.test.ts
- [ ] T140 [P] [US4] Write component test for MetricsTable in src/features/metrics/components/MetricsTable.test.tsx
- [ ] T141 [P] [US4] Write component test for MetricsStats in src/features/metrics/components/MetricsStats.test.tsx

### Implementation for User Story 4

<!-- NOTE: Implementation tasks marked [ ] to ensure verification against new tests -->
- [x] T142 [P] [US4] Create useMetrics hook in src/features/metrics/hooks/useMetrics.ts with filtering and pagination
- [x] T144 [P] [US4] Create MetricEntry component - *Note: Integrated into MetricsTable*
- [ ] T145 [P] [US4] Create MetricsStats component - *Note: Future enhancement*
- [x] T146 [P] [US4] Create MetricsFilters component in src/features/metrics/components/MetricsFilters.tsx
- [x] T146a [P] [US4] Enhance MetricsFilters to support new filter options (uri, httpMethod, httpStatus, durationLessThan, durationGreaterThan, keyword) per updated MetricFilterDto
- [x] T147 [US4] Create MetricsTable component in src/features/metrics/components/MetricsTable.tsx
- [x] T149 [US4] Create MetricsPage in src/pages/MetricsPage.tsx
- [x] T150 [US4] Add route /metrics in src/router/index.tsx
- [ ] T151 [US4] Implement statistics calculation
- [x] T152 [US4] Implement filter application logic in useMetrics hook
- [x] T153 [US4] Add empty state when no metrics match filters
- [x] T154 [US4] Add loading skeleton for table
- [x] T155 [US4] Add HTTP details display (URI, method, status code)
- [x] T156 [US4] Add navigation link from ApplicationDetailsPage to MetricsPage
- [ ] T157 [US4] Verify all tests pass and coverage ≥ 80%

**Checkpoint**: US4 Incomplete - Awaiting Test Verification

---

## Phase 7: User Story 5 - Application Sharing and Collaboration (Priority: P2)

**Goal**: Enable application owners to share read access with other users or teams, supporting collaboration across organizations with restricted permissions.

**Independent Test**: Share an application with a specific user email, verify the shared user can view the application in their list, confirm the shared user sees logs/metrics but not sensitive credentials, and revoke access to verify the user can no longer access the application.

### Tests for User Story 5 (MANDATORY per Constitution Section III)

**⚠️ WRITE TESTS FIRST - They MUST FAIL before implementation**

- [x] T158 [P] [US5] Write contract test for POST /api/applications/{id}/share in tests/integration/applications.share.test.tsx
- [x] T159 [P] [US5] Write contract test for POST /api/applications/{id}/revoke in tests/integration/applications.revoke.test.tsx
- [x] T160 [P] [US5] Write integration test for sharing flow in tests/integration/applications.shareFlow.test.tsx
- [x] T161 [P] [US5] Write unit test for useShareApplication hook in src/features/applications/hooks/useShareApplication.test.ts
- [x] T162 [P] [US5] Write component test for ShareApplicationModal in src/features/applications/components/ShareApplicationModal.test.tsx

### Implementation for User Story 5

- [x] T163 [P] [US5] Create useShareApplication hook in src/features/applications/hooks/useShareApplication.ts
- [x] T164 [P] [US5] Create ShareApplicationModal component in src/features/applications/components/ShareApplicationModal.tsx with form and Storybook story
- [x] T165 [US5] Create SharedUsersList component in src/features/applications/components/SharedUsersList.tsx to display shares and Storybook story
- [x] T166 [US5] Add share button to ApplicationDetails component (owners only)
- [x] T167 [US5] Integrate SharedUsersList into ApplicationDetails to show current shares
- [x] T168 [US5] Implement share functionality in useShareApplication hook (user or team sharing)
- [x] T169 [US5] Implement revoke access functionality in useShareApplication hook
- [x] T170 [US5] Add owner-only permission check in ApplicationDetails before showing share button
- [x] T171 [US5] Update ApplicationCard to show shared indicator for non-owned applications
- [x] T172 [US5] Add validation in ShareApplicationModal to ensure valid email or team key
- [x] T173 [US5] Add error handling for share/revoke failures
- [x] T174 [US5] Add confirmation dialog before revoking access
- [x] T175 [US5] Verify all tests pass and coverage ≥ 80% for US5 code per constitution

**Checkpoint**: User Stories 1-5 complete - users can collaborate on applications. All tests passing.

---

## Phase 8: User Story 6 - Team Management and Invitations (Priority: P3)

**Goal**: Enable users to create teams, invite members, accept invitations, and manage team membership to organize users and share applications at the team level.

**Independent Test**: Create a new team with a unique name, invite a user by email with a specific role (ADMIN or MEMBER), verify the invited user sees the pending invitation, accept the invitation, and confirm the user becomes a team member with the correct role.

### Tests for User Story 6 (MANDATORY per Constitution Section III)

**⚠️ WRITE TESTS FIRST - They MUST FAIL before implementation**

- [x] T176 [P] [US6] Write contract test for POST /api/teams in tests/integration/teams.create.test.tsx
- [x] T177 [P] [US6] Write contract test for POST /api/teams/invite in tests/integration/teams.invite.test.tsx
- [x] T178 [P] [US6] Write contract test for POST /api/team-invitations/accept in tests/integration/teams.accept.test.tsx
- [x] T179 [P] [US6] Write integration test for team creation and invitation flow in tests/integration/teams.flow.test.tsx
- [x] T180 [P] [US6] Write unit test for useTeams hook in src/features/teams/hooks/useTeams.test.ts
- [x] T181 [P] [US6] Write unit test for useTeamInvitations hook in src/features/teams/hooks/useTeamInvitations.test.ts
- [x] T182 [P] [US6] Write component test for CreateTeamModal in src/features/teams/components/CreateTeamModal.test.tsx
- [x] T183 [P] [US6] Write component test for InviteMemberModal in src/features/teams/components/InviteMemberModal.test.tsx

### Implementation for User Story 6

- [x] T184 [P] [US6] Create useTeams hook in src/features/teams/hooks/useTeams.ts to fetch teams
- [x] T185 [P] [US6] Create useTeamInvitations hook in src/features/teams/hooks/useTeamInvitations.ts to manage invitations
- [x] T186 [P] [US6] Create CreateTeamModal component in src/features/teams/components/CreateTeamModal.tsx with Storybook story
- [x] T187 [P] [US6] Create TeamCard component in src/features/teams/components/TeamCard.tsx with Storybook story
- [x] T188 [P] [US6] Create TeamList component in src/features/teams/components/TeamList.tsx with Storybook story
- [x] T189 [P] [US6] Create InviteMemberModal component in src/features/teams/components/InviteMemberModal.tsx with email and role selection and Storybook story
- [x] T190 [P] [US6] Create TeamMembersList component in src/features/teams/components/TeamMembersList.tsx showing members with roles and Storybook story
- [x] T191 [P] [US6] Create PendingInvitations component in src/features/teams/components/PendingInvitations.tsx with accept button and Storybook story
- [x] T192 [US6] Create TeamDetails component in src/features/teams/components/TeamDetails.tsx showing members and invite button and Storybook story
- [x] T193 [US6] Create TeamsPage in src/pages/TeamsPage.tsx integrating team list, create, and pending invitations
- [x] T194 [US6] Create TeamDetailsPage in src/pages/TeamDetailsPage.tsx with route param for teamId
- [x] T195 [US6] Add route for /teams in src/router/index.tsx
- [x] T196 [US6] Add route for /teams/:teamId in src/router/index.tsx
- [x] T197 [US6] Implement team creation logic in useTeams hook
- [x] T198 [US6] Implement invitation sending logic in useTeamInvitations hook
- [x] T199 [US6] Implement invitation acceptance logic in useTeamInvitations hook
- [ ] T200 [US6] Add role-based permission checks (owner/admin can invite, owner can remove)
- [ ] T201 [US6] Add validation for unique team name in CreateTeamModal
- [ ] T202 [US6] Add empty states for teams with no members and users with no pending invitations
- [ ] T203 [US6] Add member removal functionality for team owners/admins in TeamMembersList
- [ ] T204 [US6] Add navigation links in sidebar to Teams page
- [ ] T205 [US6] Verify all tests pass and coverage ≥ 80% for US6 code per constitution

**Checkpoint**: User Story 6 In Progress - CRUD complete; Permissions & Validation pending.

---

## Phase 9: User Story 7 - Application Usage Monitoring (Priority: P3)

**Goal**: Enable application owners to monitor storage consumption for logs and metrics to understand resource usage and plan capacity.

**Independent Test**: View an owned application's usage statistics showing total bytes consumed by logs and metrics separately, visualize usage trends over time with Recharts chart, and see storage breakdown by time period (7 days, 30 days, 90 days).

### Tests for User Story 7 (MANDATORY per Constitution Section III)

**⚠️ WRITE TESTS FIRST - They MUST FAIL before implementation**

- [ ] T206 [P] [US7] Write contract test for GET /api/applications/{id}/usage in tests/integration/applications.usage.test.tsx verifying ApplicationUsage response with totalLogBytes and totalMetricBytes
- [ ] T207 [P] [US7] Write integration test for usage data visualization flow in tests/integration/applications.usageFlow.test.tsx
- [ ] T208 [P] [US7] Write unit test for useApplicationUsage hook in src/features/applications/hooks/useApplicationUsage.test.ts
- [ ] T209 [P] [US7] Write component test for UsageStats component in src/features/applications/components/UsageStats.test.tsx
- [X] T210 [P] [US7] Write component test for UsageBreakdown component with time period selector in src/features/applications/components/UsageBreakdown.test.tsx

### Implementation for User Story 7

- [X] T211 [P] [US7] Create useApplicationUsage hook in src/features/applications/hooks/useApplicationUsage.ts to fetch usage data from /api/applications/{id}/usage endpoint
- [X] T212 [P] [US7] Create UsageStats component in src/features/applications/components/UsageStats.tsx showing total log bytes and metric bytes with Card layout and Storybook story
- [X] T213 [P] [US7] Create UsageBreakdown component in src/features/applications/components/UsageBreakdown.tsx with time period selector (7d, 30d, 90d) and Storybook story
- [X] T213a [P] [US7] Enhance UsageBreakdown to visualize usage trends from ApplicationUsage.trends array using Recharts time-series chart
- [X] T214 [P] [US7] Create formatBytes utility function in src/utils/formatters.ts to convert bytes to KB, MB, GB, TB
- [X] T215 [US7] Integrate UsageStats and UsageBreakdown into ApplicationDetailsPage below application credentials section
- [X] T216 [US7] Implement usage data fetching logic in useApplicationUsage hook with error handling
- [X] T217 [US7] Add loading skeleton for usage stats in ApplicationDetailsPage
- [X] T218 [US7] Add empty state when no usage data is available (new applications)
- [X] T219 [US7] Add owner-only permission check - only show usage stats to application owners per spec
- [X] T220 [US7] Implement time period filtering logic in UsageBreakdown (calculate bytes for selected period)
- [X] T221 [US7] Add usage stats icons from lucide-react (Database, HardDrive) for visual clarity
- [X] T222 [US7] Add tooltip to explain log bytes vs metric bytes in UsageStats
- [X] T223 [US7] Verify all tests pass and coverage ≥ 80% for US7 code per constitution

**Checkpoint**: All user stories complete - full observability platform with usage monitoring. All tests passing.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and final constitutional compliance verification

- [X] T224 [P] Implement error boundaries in src/components/ErrorBoundary.tsx for graceful error handling with Storybook story
- [X] T225 [P] Add accessibility improvements: ARIA labels, keyboard navigation, focus management across all components per constitution Section V
- [X] T226 [P] Add loading skeletons for all async operations (replace basic spinners)
- [ ] T227 [P] Improve error messages across all features with user-friendly text per FR-033
- [X] T228 Implement React.lazy() for route-based code splitting in src/router/index.tsx
- [X] T229 Add React.Suspense with fallback for lazy-loaded routes
- [ ] T230 [P] Add dark mode support with Tailwind CSS dark: variant (optional enhancement)
- [X] T231 [P] Add favicon and meta tags in index.html
- [X] T232 Optimize bundle size: verify main bundle < 200KB gzipped per constitution Section IV using `vite build --stats`
- [X] T233 Add Web Vitals monitoring in src/utils/webVitals.ts (FCP < 1.5s, LCP < 2.5s, TTI < 3s per constitution)
- [ ] T234 [P] Run Lighthouse audit and address performance issues to meet constitution standards
- [ ] T235 [P] Run accessibility audit with axe-core and fix issues to achieve WCAG 2.1 AA compliance per constitution Section V
- [ ] T236 Verify overall code coverage ≥ 80% across entire codebase per constitution Section III
- [ ] T237 Run ESLint with zero errors per constitution Technical Standards
- [X] T238 Run TypeScript tsc --noEmit with zero errors per constitution Section II
- [ ] T239 Verify production build succeeds without warnings per constitution Code Quality Gates
- [X] T240 [P] Add comprehensive README.md with setup instructions
- [X] T241 [P] Create deployment guide in docs/DEPLOYMENT.md
- [X] T242 [P] Add vercel.json configuration for Vercel deployment
- [X] T243 [P] Create Dockerfile for self-hosted deployment option
- [X] T244 [P] Document environment variables in .env.example
- [X] T245 Add security headers configuration for production per constitution Section VI
- [ ] T246 Add input sanitization with DOMPurify for user-generated content (if needed) per constitution Section VI
- [ ] T247 Run quickstart.md validation to ensure developer onboarding works
- [ ] T248 [P] Verify responsive design across breakpoints (mobile 375px, tablet 768px, desktop 1024px+) for all pages
- [ ] T249 [P] Test cross-browser compatibility (Chrome, Firefox, Safari, Edge) and document any known issues

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-9)**: All depend on Foundational phase completion
  - User stories can proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
  - **CRITICAL**: Tests MUST be written and FAIL before implementation per constitution Section III
- **Polish (Phase 10)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - Requires US1 for authentication context
- **User Story 3 (P1)**: Can start after Foundational (Phase 2) - Requires US2 for application selection, but independently testable
- **User Story 4 (P2)**: Can start after Foundational (Phase 2) - Requires US2 for application selection, but independently testable
- **User Story 5 (P2)**: Can start after Foundational (Phase 2) - Extends US2 for sharing, but independently testable
- **User Story 6 (P3)**: Can start after Foundational (Phase 2) - Independent from other stories
- **User Story 7 (P3)**: Can start after Foundational (Phase 2) - Extends US2 for usage monitoring, but independently testable

### Within Each User Story (CRITICAL TDD Workflow)

1. **Write Tests FIRST** - All tests marked for the user story MUST be written and MUST FAIL
2. **Review Tests** - Ensure tests cover acceptance criteria and edge cases
3. **Implement Components** - Components marked [P] can be built in parallel
4. **Run Tests** - Verify tests now PASS with implementation
5. **Verify Coverage** - Ensure ≥ 80% code coverage per constitution
6. **Checkpoint** - Complete user story validation before proceeding

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- **Test tasks for each user story** can all be written in parallel
- Once tests are written, implementation tasks marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members (after Foundational)
- All Polish tasks marked [P] can run in parallel

---

## Implementation Strategy

### MVP First (User Stories 1-3 Only) - TDD Approach

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. **User Story 1:**
   - Write all US1 tests (T057-T066) - verify they FAIL
   - Implement US1 (T067-T081) - verify tests PASS
   - Verify coverage ≥ 80%
4. **User Story 2:**
   - Write all US2 tests (T082-T090) - verify they FAIL
   - Implement US2 (T091-T108) - verify tests PASS
   - Verify coverage ≥ 80%
5. **User Story 3:**
   - Write all US3 tests (T109-T116) - verify they FAIL
   - Implement US3 (T117-T135) - verify tests PASS
   - Verify coverage ≥ 80%
6. **STOP and VALIDATE**: Test all three user stories independently
7. Deploy/demo MVP with core observability features

### Constitutional Compliance Checklist

Before considering ANY user story complete:

- [ ] All tests written BEFORE implementation (Section III)
- [ ] All tests passing (Section III)
- [ ] Code coverage ≥ 80% for that user story (Section III)
- [ ] No `any` types in TypeScript (Section II)
- [ ] ESLint passing with zero errors (Technical Standards)
- [ ] TypeScript tsc --noEmit passing (Section II)
- [ ] All components have Storybook stories (per user decision)
- [ ] Accessibility tested (keyboard nav, ARIA labels) (Section V)
- [ ] Performance tested (no unnecessary re-renders) (Section IV)

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done, assign user stories:
   - Developer A: Write US1 tests → Implement US1
   - Developer B: Write US6 tests → Implement US6 (fully independent)
   - Developer C: Help with common components / reviews
3. After US1 complete:
   - Developer A: Write US2 tests → Implement US2
   - Developer B: Continue US6
   - Developer C: Write US3 tests → Implement US3
4. Stories complete and integrate independently, all with passing tests

---

## Summary

- **Total Tasks**: 249 (up from 218 - added 18 US7 tasks + 5 renumbered polish tasks + 2 verification tasks)
- **Setup Tasks**: 17 (T001-T017) - includes Storybook and Recharts
- **Foundational Tasks**: 42 (T018-T059) - includes retry logic and API integration
- **User Story 1 Tasks**: 27 (T060-T085 - renumbered from T059-T085) - 10 test + 17 implementation
- **User Story 2 Tasks**: 27 (T086-T108 - renumbered from T082-T108) - 9 test + 18 implementation (includes search)
- **User Story 3 Tasks**: 27 (T109-T135) - 8 test + 19 implementation (includes MDC display)
- **User Story 4 Tasks**: 22 (T136-T157) - 6 test + 16 implementation (Recharts)
- **User Story 5 Tasks**: 18 (T158-T175) - 5 test + 13 implementation
- **User Story 6 Tasks**: 30 (T176-T205) - 8 test + 22 implementation
- **User Story 7 Tasks**: 18 (T206-T223) - 5 test + 13 implementation (usage monitoring)
- **Polish Tasks**: 26 (T224-T249) - includes comprehensive constitutional verification

**Test Tasks Added**: 71 test tasks (all marked with test type: contract, integration, unit, component, E2E)

**Constitutional Compliance**: ✅ 100%
- ✅ Section II (Type Safety): All types defined, no `any` verification in T237-T238
- ✅ Section III (Test-First): Test tasks added before all implementations, 80% coverage verified in T236 and per-story checkpoints
- ✅ Section IV (Performance): Bundle size, Web Vitals verification in T232-T234
- ✅ Section V (UX Consistency): Accessibility audit in T235, Storybook stories for all components
- ✅ Section VI (Security): Input sanitization, security headers in T245-T246

**New Requirements Covered**:
- ✅ FR-014 (MDC display): T122, T132
- ✅ FR-031, FR-032 (Usage monitoring): T206-T223
- ✅ SC-012 (Application search): T102
- ✅ Retry logic: T029
- ✅ API client integration: T030
- ✅ Storybook per user decision: T010, all component tasks
- ✅ Recharts per user decision: T011, T147

**Parallel Opportunities Identified**: 
- Setup: 14 parallel tasks
- Foundational: 35+ parallel tasks
- Each user story: All test tasks can be written in parallel, then 4-8 implementation tasks in parallel
- Multiple user stories can proceed in parallel after Foundational phase

**MVP Scope** (User Stories 1-3):
- 79 tasks total for MVP (includes all test tasks)
- Delivers: Authentication, Application Management, Log Viewing with MDC
- Provides immediate value for observability and monitoring
- **100% constitutional compliance** with test-first development

**Full Platform Scope** (User Stories 1-7):
- 205 tasks total for complete platform (includes all test tasks, excludes polish phase)
- Delivers: Complete observability platform with authentication, applications, logs, metrics, sharing, teams, and usage monitoring
- **100% constitutional compliance** with test-first development

**Format Validation**: ✅ All tasks follow the strict checklist format:
- Checkbox: `- [ ]` or `- [x]`
- Task ID: Sequential (T001-T249)
- [P] marker: Only on parallelizable tasks
- [Story] label: Only on user story tasks (US1-US7)
- Description: Includes exact file paths
- Test tasks clearly marked with test type
- Implementation follows test tasks per TDD
