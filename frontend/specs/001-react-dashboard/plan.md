# Implementation Plan: Langa Dashboard - React Observability Platform

**Feature Branch**: `001-react-dashboard`  
**Created**: December 31, 2025  
**Status**: Planning  
**Specification**: [spec.md](spec.md)

## Technical Context

### Technology Stack

**Core Framework**:
- React 18.3+ (with hooks, concurrent features, automatic batching)
- TypeScript 5.3+ (strict mode enabled)
- Vite 5+ (fast build tool with HMR)

**Styling**:
- Tailwind CSS 3.4+ (utility-first CSS framework as primary styling solution)
- PostCSS (for Tailwind processing)
- Tailwind Forms plugin (for better form styling)
- Tailwind Typography plugin (for content formatting)

**State Management**:
- React Context API + useReducer (for global auth and user state)
- Custom hooks for local component state
- No Redux - keeping dependencies minimal per requirement

**Routing**:
- React Router v6.21+ (declarative routing, protected routes)

**HTTP Client**:
- Axios 1.6+ (with interceptors for auth, error handling, request/response transformation)

**Data Visualization**:
- Recharts 2.10+ (for metrics time-series charts - React-native, declarative, 34KB gzipped)

**Icons**:
- lucide-react (modern, lightweight icon library with tree-shaking support - 1KB per icon)

**Testing**:
- Vitest 1.0+ (fast unit test runner, Vite-native)
- React Testing Library 14+ (component testing)
- MSW 2.0+ (Mock Service Worker for API mocking)
- Playwright 1.40+ (E2E testing)
- Storybook 7+ (component development, visual testing, documentation)
- Storybook 7+ (component development, visual testing, documentation)

**Code Quality**:
- ESLint 8+ with TypeScript plugin
- Prettier 3+ (code formatting)
- Husky 8+ (git hooks)
- lint-staged (pre-commit linting)

**Build & Deployment**:
- Vite for bundling and optimization
- Deployment target (Vercel, Docker)
- Environment configuration strategy (.env files management)

**Development Tools**:
- Component development environment use Storybook for isolation

### Key Architectural Decisions

1. **Minimal Dependencies**: Following user requirement, avoid heavy libraries. Use native React patterns and Tailwind CSS for most UI needs instead of component libraries like Material-UI or Ant Design.

2. **Custom Component Library**: Build minimal custom components (Button, Input, Modal, Card, etc.) using Tailwind CSS rather than importing a full UI library.

3. **File-based Code Splitting**: Use React.lazy() and Suspense for route-based code splitting to meet performance requirements (<200KB main bundle).

4. **JWT Token Management**: Store access token in memory, refresh token in httpOnly cookie (if backend supports) or localStorage with appropriate security measures.

5. **API Integration**: Single Axios instance with interceptors for auth header injection, token refresh on 401, and centralized error handling.

6. **Virtual Scrolling**: Required for log and metric tables to handle large datasets efficiently (considering react-window or react-virtual for minimal overhead).

7. **Routing Structure**: Organized route hierarchy with public and protected routes:
   - **Public Routes**: `/`, `/login`, `/register` - Accessible without authentication
   - **Protected Routes**: `/dashboard`, `/applications`, `/applications/:id`, `/logs`, `/metrics`, `/profile` - Require authentication
   - **Route Protection**: `ProtectedRoute` wrapper with loading state handling, session verification, and automatic redirect to login
   - **Session Persistence**: Access token in memory, refresh token in localStorage, automatic token refresh via Axios interceptor

### Dependencies on Backend

- Langa Backend REST API as documented in `documents/01-SPECIFICATION.md`
- CORS configuration on backend matching frontend origin
- JWT token format and expiration times as specified
- HMAC signature validation not required for dashboard (only for ingestion agents)

### Known Constraints

- Must work on modern browsers (Chrome 90+, Firefox 88+, Safari 14+, Edge 90+)
- Mobile-responsive design required (tablet and mobile viewports)
- Accessibility compliance WCAG 2.1 AA
- Performance budget: Initial load < 2s on 3G, TTI < 3s, bundle < 200KB gzipped

### Resolved Decisions

1. **Charts Library**: ✅ **RESOLVED** - Recharts chosen and integrated (T011, T147, US4 metrics visualization). Provides sufficient features for current needs with good React integration and TypeScript support.

2. **Visual Regression Testing**: ✅ **RESOLVED** - Storybook adopted for component development and visual testing documentation.

3. **Deployment Target**: ✅ **RESOLVED** - Vercel and self-hosted Docker support implemented (T242, T243).

4. **Environment Configuration**: ✅ **RESOLVED** - .env files strategy managed for dev/prod environments (T013).

5. **API Documentation**: ✅ **RESOLVED** - Rely on external backend documentation/contracts; no embedded Swagger required in dashboard.

6. **Virtual Scrolling**: ✅ **RESOLVED** - @tanstack/react-virtual selected and integrated for performance (T012).

7. **Date/Time Handling**: ✅ **RESOLVED** - Native Intl API adopted to avoid heavy dependencies (date-fns/dayjs) per minimal dependency requirement (T037).

8. **Form Validation**: ✅ **RESOLVED** - Standard HTML5 validation + custom Hooks; avoiding heavy form libraries for MVP.

## Constitution Check

### Alignment with Core Principles

**I. Component-First Development** ✅
- Plan includes building custom component library with Tailwind CSS
- Components will be modular, reusable, single-purpose
- Will be organized in `/components`, `/features`, `/pages` structure
- TypeScript interfaces for all props
- Co-located tests for all components

**II. Type Safety (NON-NEGOTIABLE)** ✅
- TypeScript 5.3+ with strict mode
- All API contracts will match backend specification exactly
- Interfaces for User, Application, LogEntry, MetricEntry, Team, TeamInvitation, etc.
- No `any` types policy enforced via ESLint rules
- Type-safe API client with explicit request/response types

**III. Test-First Development** ✅
- Vitest + React Testing Library for unit/component tests
- MSW for API mocking in tests
- Playwright for E2E tests
- 80% coverage target
- TDD workflow: tests → approval → implementation

**IV. Performance Standards** ✅
- Vite for fast builds and optimized bundles
- React.lazy() for route-based code splitting
- Virtual scrolling for large log/metric lists
- Bundle size budget < 200KB gzipped
- Performance monitoring with Web Vitals (FCP, LCP, TTI targets)
- Debounced search/filters

**V. User Experience Consistency** ✅
- Custom design system with Tailwind CSS (consistent utilities)
- WCAG 2.1 AA compliance (semantic HTML, ARIA labels, keyboard nav)
- Mobile-first responsive design
- Loading states for all async operations
- Empty states with helpful guidance
- User-friendly error messages

**VI. Security First** ✅
- JWT token management (memory + httpOnly cookie strategy)
- Authorization checks on all protected routes
- Input validation on all forms
- XSS prevention via React's built-in escaping + DOMPurify for rich content
- HTTPS enforcement in production
- Security headers configuration

### Technical Standards Compliance

**Technology Stack** ✅
- React 18+ ✓
- TypeScript 5+ ✓
- Vite (modern, fast) ✓
- React Context (no Redux per minimal dependencies) ✓
- React Router v6+ ✓
- Axios ✓
- Jest/Vitest + RTL ✓
- ESLint + Prettier ✓

**Code Quality Gates** ✅
- ESLint with zero errors ✓
- TypeScript tsc --noEmit with zero errors ✓
- Tests passing with 80% coverage ✓
- Production build succeeds ✓
- Bundle size checks ✓
- Accessibility tests (axe-core integration) ✓

### Potential Violations & Justifications

**Recharts as Chart Library**:
- Constitution suggests minimal dependencies
- Justification: Metrics visualization requires charts; Recharts is lightweight (compared to Chart.js or D3), React-native, and tree-shakeable
- Alternative: Consider if pure Tailwind + SVG custom charts are feasible for simpler use cases

**Virtual Scrolling Library**:
- Adds a dependency (react-window or react-virtual)
- Justification: Essential for performance with large log datasets (100,000+ entries). Implementing custom virtual scrolling would be complex and error-prone. react-virtual is 2.10KB gzipped.
- Alternative: Start without, add if performance testing shows need

**Date Formatting Library**:
- date-fns or day.js adds dependency
- Justification: Native Intl.DateTimeFormat can handle basic formatting. Start with native, add library only if complex timezone/formatting needed.
- **Decision**: Use native Intl API (implemented in src/utils/formatters.ts) - Provides formatDate(), formatDateTime(), formatRelativeTime(), formatDuration() utilities with zero dependencies

**Badge Component**:
- Added custom Badge component (not in original plan)
- Justification: Needed for consistent status indicators (log levels, application states, user roles)
- Implementation: Tailwind-based with variants (default, success, warning, error, info) and sizes (sm, md, lg)

**Layout Architecture**:
- Simplified from Header/Sidebar/Footer to Navbar/Layout
- Justification: Dashboard doesn't require persistent sidebar; top navigation more appropriate for application navigation
- Implementation: Navbar component with user dropdown menu and active route highlighting, Layout wrapper with responsive container

### Gate Evaluation

**PROCEED**: All core principles aligned. Minor dependencies (Recharts, react-virtual) justified for essential functionality. No violations that compromise constitution.

## Phase 0: Research & Outline

**Status**: See [research.md](research.md) for detailed findings

**Key Research Tasks**:
1. Evaluate chart libraries (Recharts vs alternatives) for metrics visualization
2. Decide on virtual scrolling approach (react-window vs react-virtual vs custom)
3. Define environment configuration strategy
4. Select deployment target and determine build configuration
5. Investigate visual regression testing options
6. Determine form validation strategy
7. Research Tailwind CSS best practices for component architecture

## Phase 1: Design & Contracts

### Data Model

**Status**: See [data-model.md](data-model.md) for complete entity definitions

**Core Entities**:
- User, Application, LogEntry, MetricEntry, Team, TeamMember, TeamInvitation, ApplicationUsage, ShareWith
- All entities map directly to backend API response models
- TypeScript interfaces in `src/types/`

### API Contracts

**Status**: 
- **Backend Contract**: `contracts/api-client.ts` contains TypeScript client auto-generated from backend OpenAPI spec (reference only)
- **Frontend Integration**: Actual API calls use `src/contracts/` with type-safe request/response interfaces integrated via T029-T030 (retry logic and API client wrapper)
- All API modules (Auth, Applications, Logs, Metrics, Teams, Invitations) use consistent error handling and retry patterns

**API Modules**:
- Auth API: register, login, refresh
- Applications API: CRUD, share, revoke, usage
- Logs API: query with filters, pagination
- Metrics API: query with filters, pagination  
- Teams API: CRUD, members
- Team Invitations API: send, accept, list

### Quick Start Guide

**Status**: See [quickstart.md](quickstart.md) for developer onboarding

**Covers**:
- Prerequisites (Node.js, npm/pnpm)
- Project setup commands
- Development workflow
- Testing commands
- Build and deployment

## Phase 2: Implementation Plan

### File Structure

```
langa-dashboard/
├── public/
│   └── favicon.ico
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── common/         # Generic components (Button, Input, Modal, etc.)
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Pagination.tsx
│   │   │   ├── EmptyState.tsx
│   │   │   ├── LoadingSpinner.tsx
│   │   │   └── ErrorMessage.tsx
│   │   └── layout/         # Layout components
│   │       ├── Header.tsx
│   │       ├── Sidebar.tsx
│   │       ├── Footer.tsx
│   │       └── MainLayout.tsx
│   ├── features/           # Feature-specific modules
│   │   ├── auth/
│   │   │   ├── components/
│   │   │   │   ├── LoginForm.tsx
│   │   │   │   ├── RegisterForm.tsx
│   │   │   │   └── SetupWizard.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useAuth.ts
│   │   │   │   └── useTokenRefresh.ts
│   │   │   ├── context/
│   │   │   │   └── AuthContext.tsx
│   │   │   └── api/
│   │   │       └── authApi.ts
│   │   ├── applications/
│   │   │   ├── components/
│   │   │   │   ├── ApplicationList.tsx
│   │   │   │   ├── ApplicationCard.tsx
│   │   │   │   ├── CreateApplicationModal.tsx
│   │   │   │   ├── ApplicationDetails.tsx
│   │   │   │   ├── ShareApplicationModal.tsx
│   │   │   │   └── ApplicationUsageChart.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useApplications.ts
│   │   │   │   ├── useCreateApplication.ts
│   │   │   │   └── useShareApplication.ts
│   │   │   └── api/
│   │   │       └── applicationsApi.ts
│   │   ├── logs/
│   │   │   ├── components/
│   │   │   │   ├── LogsTable.tsx (virtualized)
│   │   │   │   ├── LogEntry.tsx
│   │   │   │   ├── LogFilters.tsx
│   │   │   │   ├── LogLevelBadge.tsx
│   │   │   │   └── StackTraceViewer.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useLogs.ts
│   │   │   │   └── useLogFilters.ts
│   │   │   └── api/
│   │   │       └── logsApi.ts
│   │   ├── metrics/
│   │   │   ├── components/
│   │   │   │   ├── MetricsTable.tsx
│   │   │   │   ├── MetricsChart.tsx
│   │   │   │   ├── MetricsFilters.tsx
│   │   │   │   ├── MetricsStats.tsx
│   │   │   │   └── MetricEntry.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useMetrics.ts
│   │   │   │   └── useMetricsStats.ts
│   │   │   └── api/
│   │   │       └── metricsApi.ts
│   │   └── teams/
│   │       ├── components/
│   │       │   ├── TeamList.tsx
│   │       │   ├── CreateTeamModal.tsx
│   │       │   ├── TeamDetails.tsx
│   │       │   ├── TeamMembersList.tsx
│   │       │   ├── InviteMemberModal.tsx
│   │       │   └── PendingInvitations.tsx
│   │       ├── hooks/
│   │       │   ├── useTeams.ts
│   │       │   └── useTeamInvitations.ts
│   │       └── api/
│   │           └── teamsApi.ts
│   ├── pages/              # Route pages
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── ApplicationsPage.tsx
│   │   ├── ApplicationDetailsPage.tsx
│   │   ├── LogsPage.tsx
│   │   ├── MetricsPage.tsx
│   │   ├── TeamsPage.tsx
│   │   └── NotFoundPage.tsx
│   ├── hooks/              # Global custom hooks
│   │   ├── useApi.ts
│   │   ├── usePagination.ts
│   │   ├── useDebounce.ts
│   │   └── useLocalStorage.ts
│   ├── services/           # API clients and services
│   │   ├── api.ts         # Axios instance with interceptors
│   │   └── errorHandler.ts
│   ├── types/              # TypeScript type definitions
│   │   ├── user.ts
│   │   ├── application.ts
│   │   ├── log.ts
│   │   ├── metric.ts
│   │   ├── team.ts
│   │   └── api.ts
│   ├── utils/              # Helper functions
│   │   ├── formatters.ts   # Date, number formatting
│   │   ├── validators.ts   # Form validation
│   │   └── constants.ts    # App constants
│   ├── styles/             # Global styles
│   │   └── index.css       # Tailwind directives + custom CSS
│   ├── config/             # App configuration
│   │   └── env.ts          # Environment variables
│   ├── router/             # Routing configuration
│   │   ├── index.tsx       # Route definitions
│   │   └── ProtectedRoute.tsx
│   ├── App.tsx
│   ├── main.tsx
│   └── vite-env.d.ts
├── tests/                  # Test utilities and setup
│   ├── setup.ts
│   ├── mocks/
│   │   ├── handlers.ts     # MSW handlers
│   │   └── server.ts       # MSW server
│   └── utils/
│       └── testUtils.tsx   # Custom render, etc.
├── .env.example
├── .env.development
├── .env.production
├── .eslintrc.cjs
├── .prettierrc
├── tsconfig.json
├── tsconfig.node.json
├── vite.config.ts
├── vitest.config.ts
├── tailwind.config.js
├── postcss.config.js
├── package.json
└── README.md
```

### Implementation Tasks

**Phase 1: Project Setup (Priority: P0)**
- [ ] Initialize Vite + React + TypeScript project
- [ ] Configure Tailwind CSS with PostCSS
- [ ] Set up ESLint + Prettier
- [ ] Configure Vitest + React Testing Library
- [ ] Set up MSW for API mocking
- [ ] Configure environment variables (.env files)
- [ ] Create base file structure
- [ ] Set up git hooks with Husky + lint-staged

**Phase 2: Core Infrastructure (Priority: P0)**
- [ ] Create Axios client with interceptors (auth, error handling, token refresh)
- [ ] Define TypeScript types for all API entities
- [ ] Build AuthContext and auth hooks
- [ ] Implement protected route wrapper
- [ ] Set up router with route definitions
- [ ] Create basic layout components (Header, Sidebar, MainLayout)
- [ ] Build common components (Button, Input, Card, Modal, etc.)

**Phase 3: Authentication & Onboarding (Priority: P1)**
- [ ] Build LoginPage and LoginForm component
- [ ] Build RegisterPage and RegisterForm component
- [ ] Implement useAuth hook with login, register, logout
- [ ] Implement automatic token refresh logic
- [ ] Build SetupWizard for first-time users
- [ ] Add form validation for auth forms
- [ ] Write tests for auth flow

**Phase 4: Application Management (Priority: P1)**
- [ ] Build ApplicationsPage with list view
- [ ] Create ApplicationCard component
- [ ] Build CreateApplicationModal
- [ ] Build ApplicationDetailsPage
- [ ] Implement useApplications hook
- [ ] Implement useCreateApplication hook
- [ ] Add application search and filtering
- [ ] Write tests for application management

**Phase 5: Log Viewing (Priority: P1)**
- [ ] Build LogsPage with filters
- [ ] Create LogsTable with virtual scrolling
- [ ] Build LogFilters component (level, logger, time range)
- [ ] Create LogEntry component with expandable stack trace
- [ ] Implement useLogs hook with pagination
- [ ] Implement useLogFilters hook with debouncing
- [ ] Build StackTraceViewer component
- [ ] Write tests for log viewing

**Phase 6: Metrics Visualization (Priority: P2)**
- [ ] Build MetricsPage
- [ ] Create MetricsChart component with Recharts
- [ ] Build MetricsTable component
- [ ] Create MetricsFilters component
- [ ] Build MetricsStats component (avg, median, p95, p99)
- [ ] Implement useMetrics hook
- [ ] Implement useMetricsStats hook
- [ ] Write tests for metrics

**Phase 7: Sharing & Collaboration (Priority: P2)**
- [ ] Build ShareApplicationModal
- [ ] Add share button to ApplicationDetails
- [ ] Implement useShareApplication hook
- [ ] Display shared users/teams in ApplicationDetails
- [ ] Add revoke sharing functionality
- [ ] Handle permission-based UI (owner vs shared access)
- [ ] Write tests for sharing

**Phase 8: Team Management (Priority: P3)**
- [ ] Build TeamsPage
- [ ] Create CreateTeamModal
- [ ] Build TeamDetails component
- [ ] Create TeamMembersList
- [ ] Build InviteMemberModal
- [ ] Create PendingInvitations component
- [ ] Implement useTeams and useTeamInvitations hooks
- [ ] Write tests for team management

**Phase 9: Usage Monitoring (Priority: P3)**
- [ ] Add ApplicationUsageChart to ApplicationDetails
- [ ] Implement usage data fetching
- [ ] Build usage trend visualization
- [ ] Add storage breakdown (logs vs metrics)
- [ ] Write tests for usage monitoring

**Phase 10: Polish & Optimization (Priority: P4)**
- [ ] Implement loading states for all async operations
- [ ] Add empty states with helpful messaging
- [ ] Improve error messages and error boundaries
- [ ] Add accessibility improvements (ARIA labels, keyboard nav)
- [ ] Optimize bundle size (code splitting, tree shaking)
- [ ] Add performance monitoring (Web Vitals)
- [ ] Conduct accessibility audit with axe
- [ ] Write E2E tests with Playwright

**Phase 11: Documentation & Deployment (Priority: P4)**
- [ ] Write comprehensive README
- [ ] Document component props and usage
- [ ] Create deployment guide
- [ ] Set up CI/CD pipeline
- [ ] Configure production environment
- [ ] Deploy to hosting platform
- [ ] Set up monitoring and analytics

### Testing Strategy

**Unit Tests**:
- All utilities, formatters, validators
- Custom hooks (auth, data fetching, pagination)
- Pure components (presentational components)

**Component Tests**:
- User interactions (clicks, form submissions)
- Conditional rendering (loading, error, empty states)
- Accessibility (keyboard navigation, ARIA labels)

**Integration Tests**:
- API integration with MSW mocks
- Authentication flow (login → token → protected routes)
- Data fetching and state management

**E2E Tests**:
- Critical user journeys (register → login → create app → view logs)
- Cross-browser compatibility
- Mobile responsive behavior

### Success Metrics

- Initial load time < 2s (measure with Lighthouse)
- TTI < 3s (measure with Web Vitals)
- FCP < 1.5s
- LCP < 2.5s
- Bundle size < 200KB gzipped (measure with Vite build analysis)
- Test coverage ≥ 80%
- Lighthouse accessibility score ≥ 90
- Zero ESLint errors
- Zero TypeScript errors