# Langa Dashboard Constitution

## Core Principles

### I. Component-First Development
Components must be modular, reusable, and single-purpose. Every UI component should:
- Be independently testable with unit tests
- Have clear, documented props interface with TypeScript types
- Follow React best practices (hooks, composition over inheritance)
- Be located in appropriate directories (`/components`, `/features`, `/pages`)
- Have co-located tests and styles when applicable
- Export named exports for better refactoring support

### II. Type Safety (NON-NEGOTIABLE)
TypeScript is mandatory across the entire codebase:
- All components, hooks, utilities must have explicit types
- No `any` types except in explicitly documented edge cases
- API contracts must match backend specification exactly
- Define interfaces for all data models (User, Application, LogEntry, MetricEntry, Team, etc.)
- Use discriminated unions for state machines and variants
- Leverage type inference where it improves readability
- Strict mode enabled in `tsconfig.json`
- **Import Type Discipline**: With `verbatimModuleSyntax: true` in tsconfig:
  - Use `import type` for type-only imports (interfaces, type aliases)
  - Use regular `import` for runtime values (enums, classes, functions)
  - Example: `import type { Team } from '@/types/team'; import { TeamRole } from '@/types/team';`
  - This ensures proper module resolution and prevents bundler issues

### III. Test-First Development
Testing is mandatory before feature implementation:
- **Unit Tests**: All utilities, hooks, and pure functions (Jest + React Testing Library)
- **Component Tests**: User interactions, accessibility, edge cases
- **Integration Tests**: API integration, authentication flows, data fetching
- **Visual Regression**: Critical UI components (consider Storybook + Chromatic)
- Minimum 80% code coverage for new code
- Tests must pass before PR approval
- Follow Arrange-Act-Assert (AAA) pattern

### IV. Performance Standards
The dashboard must be fast and responsive:
- **Initial Load**: < 2 seconds on 3G connection
- **Time to Interactive (TTI)**: < 3 seconds
- **First Contentful Paint (FCP)**: < 1.5 seconds
- **Largest Contentful Paint (LCP)**: < 2.5 seconds
- Code splitting for routes and heavy components
- Lazy loading for non-critical features
- Optimize bundle size (target < 200KB gzipped for main bundle)
- Use React.memo, useMemo, useCallback appropriately
- Virtualized lists for large datasets (logs, metrics)
- Debounce/throttle search and filter operations

### V. User Experience Consistency
Ensure a cohesive, accessible user experience:
- **Design System**: Use a consistent component library (e.g., Material-UI, Ant Design, or custom)
- **Layout Structure (NON-NEGOTIABLE)**: All pages must follow this hierarchy:
  - **Viewport**: Full height (`h-screen`) with flex column layout
  - **Navigation**: Fixed navbar (no flex-grow)
  - **Main Content**: `flex-1 overflow-hidden` container
  - **Page Structure**:
    - Fixed header section (`flex-none`) with title, actions, filters
    - Scrollable content area (`flex-1 overflow-y-auto scrollbar-light`)
  - **Table Headers**: Sticky positioning (`sticky top-0 z-10`) with explicit background colors
  - **Back Links**: Fixed positioning outside scrollable content areas
- **Scrollbar Styling (NON-NEGOTIABLE)**: Custom lightweight scrollbar implementation:
  - Width: 6px (vs default ~15px)
  - Track: Transparent background
  - Thumb: Semi-transparent gray (30% opacity, 50% on hover)
  - Dark mode: Darker thumb colors (`rgba(75, 85, 99, 0.5)`)
  - Applied via `.scrollbar-light` class to all scrollable containers
- **Dark Mode Support (NON-NEGOTIABLE)**: All pages and components must support dark mode:
  - Use Tailwind's dark mode variant (`dark:`) for all color-related classes
  - Ensure proper contrast ratios in both light and dark themes
  - Test all pages in both modes before PR submission
  - Background: `bg-white dark:bg-gray-800`, Text: `text-gray-900 dark:text-gray-100`
  - Borders: `border-gray-200 dark:border-gray-700`, Shadows: `shadow-sm dark:shadow-gray-900/50`
  - Theme preference stored in localStorage as 'langa-theme' with options: 'light', 'dark', 'system'
  - Theme selector available in Profile settings
- **Accessibility**: WCAG 2.1 AA compliance minimum
  - Semantic HTML, proper ARIA labels
  - Keyboard navigation for all interactions
  - Screen reader compatibility
  - Color contrast ratios meet standards
- **Responsive Design**: Mobile-first approach, works on tablets and desktop
- **Error Handling**: User-friendly error messages, no technical jargon
- **Loading States**: Clear feedback for async operations
- **Empty States**: Helpful guidance when no data available
- **Consistent Patterns**: Similar actions behave similarly across the app

### VI. Security First
Security is paramount for an observability platform:
- **Authentication**: JWT token management with secure storage (httpOnly cookies preferred)
- **Authorization**: Implement role-based access control matching backend (OWNER, VIEWER)
- **API Security**: Always send authentication headers, handle 401/403 gracefully
- **Secrets Management**: Never commit API keys, tokens, or secrets
- **Input Sanitization**: Validate and sanitize all user inputs
- **XSS Prevention**: Escape user-generated content, use DOMPurify for rich text
- **CSRF Protection**: Implement CSRF tokens for state-changing operations
- **HTTPS Only**: Enforce secure connections in production
- **Dependencies**: Regular security audits (`npm audit`), keep dependencies updated

## Technical Standards

### Technology Stack
- **Framework**: React 18+ (with hooks, concurrent features)
- **Language**: TypeScript 5+
- **Build Tool**: Vite or Create React App
- **State Management**: React Context + hooks (or Redux Toolkit for complex state)
- **Routing**: React Router v6+
- **HTTP Client**: Axios or Fetch API with interceptors
- **Testing**: Jest + React Testing Library + MSW (Mock Service Worker)
- **Linting**: ESLint with TypeScript plugin, Prettier for formatting
- **Package Manager**: npm or pnpm (consistent across team)

### API Integration
Must align with Langa Backend specification:
- **Base URL**: Configurable via environment variables
- **Endpoints**: Match documented API contracts exactly
- **Pagination (NON-NEGOTIABLE)**: All paginated endpoints must follow this pattern:
  - **Frontend**: 1-based page numbers (user-facing, starting at page 1)
  - **Backend**: 0-based page indices (API layer, starting at page 0)
  - **Conversion**: `backendPage = frontendPage - 1` in API layer before request
  - **Response**: Extract `totalPages` directly from backend response (don't calculate)
  - **Applies to**: logsApi, metricsApi, applicationApi, and any future paginated endpoints
- **Authentication**: JWT in `Authorization: Bearer <token>` header
- **Error Handling**: Standardized error response parsing
- **Retry Logic**: Implement exponential backoff for transient failures
- **Request Cancellation**: Use AbortController for cleanup
- **Optimistic Updates**: For better UX where appropriate

### Code Quality Gates
All code must pass before merge:
- **Linting**: `eslint` with zero errors
- **Type Checking**: `tsc --noEmit` with zero errors
- **Tests**: All tests passing, coverage thresholds met
- **Build**: Production build succeeds without warnings
- **Bundle Size**: Check against size budgets
- **Accessibility**: Automated a11y tests passing

### File Structure
```
src/
├── components/       # Reusable UI components
│   ├── common/      # Buttons, inputs, modals, etc.
│   └── layout/      # Header, sidebar, footer
├── features/        # Feature-specific modules
│   ├── applications/
│   ├── logs/
│   ├── metrics/
│   ├── teams/
│   └── auth/
├── hooks/           # Custom React hooks
├── services/        # API clients and business logic
├── types/           # TypeScript type definitions
├── utils/           # Helper functions
├── styles/          # Global styles, theme
├── config/          # App configuration
└── __tests__/       # Global test utilities
```

## Development Workflow

### Feature Development Process
1. **Specification Review**: Understand requirements from Langa Backend docs
2. **Design Approval**: UI mockups reviewed and approved
3. **Type Definitions**: Define TypeScript interfaces first
4. **Test Writing**: Write failing tests (TDD)
5. **Implementation**: Build feature to pass tests
6. **Code Review**: Peer review with checklist
7. **QA Testing**: Manual testing in staging environment
8. **Documentation**: Update README, comments, Storybook

### Code Review Requirements
Every PR must:
- Have descriptive title and detailed description
- Link to relevant issue/ticket
- Include screenshots/videos for UI changes
- Pass all automated checks (linting, tests, build)
- Be reviewed by at least one team member
- Address all review comments before merge
- Keep PRs small (<400 lines changed when possible)

### Quality Checklist
Before submitting PR:
- [ ] TypeScript types are explicit and correct
- [ ] Unit tests written and passing
- [ ] Component tests cover user interactions
- [ ] No console.log or debugging code
- [ ] Error handling implemented
- [ ] Loading states implemented
- [ ] Accessibility verified (keyboard, screen reader)
- [ ] Responsive design tested (mobile, tablet, desktop)
- [ ] Performance optimized (no unnecessary re-renders)
- [ ] Documentation updated (comments, README)

## Governance

This constitution supersedes all other development practices. All code changes, architecture decisions, and process modifications must align with these principles.

### Amendment Process
- Amendments require team consensus (75% approval)
- Must document rationale and impact assessment
- Migration plan required for breaking changes
- Version bump and changelog entry mandatory

### Compliance
- All PRs must demonstrate compliance with constitution
- Architecture decisions must be justified against principles
- Technical debt must be documented and tracked
- Regular audits to ensure ongoing compliance

**Version**: 1.1.0 | **Ratified**: December 31, 2025 | **Last Amended**: January 12, 2026

### Amendment History
- **v1.1.0** (January 12, 2026): Added layout structure requirements, scrollbar styling standards, enhanced dark mode specifications, pagination pattern standardization
- **v1.0.0** (December 31, 2025): Initial constitution ratified
