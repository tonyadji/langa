# Research & Technical Decisions: Langa Dashboard

**Feature**: 001-react-dashboard  
**Date**: December 31, 2025  
**Status**: Complete

## Purpose

This document resolves all NEEDS CLARIFICATION items from the initial plan, aligning with the constitution's principle of minimal dependencies and Tailwind CSS-first approach.

---

## Decision 1: Charts Library for Metrics Visualization

**Question**: Is Recharts sufficient for metrics visualization, or do we need more advanced features?

**Research**:
- Recharts: 34KB gzipped, React-native, declarative, good for time-series
- Chart.js: 60KB gzipped, requires react-chartjs-2 wrapper, more features but heavier
- D3.js: Extremely powerful but 70KB+ and steep learning curve
- Custom SVG with Tailwind: Possible for simple charts, full control, zero dependencies

**Decision**: **Use Recharts for metrics visualization**

**Rationale**:
- Team decision to use battle-tested charting library for reliability
- Recharts is React-native, declarative, and well-suited for time-series data
- 34KB gzipped is acceptable trade-off for robust charting capabilities
- Reduces development time compared to custom SVG implementation
- Provides consistent, professional-looking charts out of the box

**Implementation**:
- Use Recharts components (LineChart, AreaChart, BarChart) for metrics visualization
- Leverage Recharts' built-in responsive design
- Customize with Tailwind CSS where needed for consistent theming
- Tree-shake unused Recharts components to minimize bundle impact

---

## Decision 2: Virtual Scrolling Library

**Question**: react-window, react-virtual, or custom solution for large log datasets?

**Research**:
- react-window: 6.8KB gzipped, battle-tested, more popular (19K+ stars)
- react-virtual: 2.1KB gzipped, modern hooks API, maintained by TanStack
- Custom solution: Complex to implement correctly (viewport calculations, scroll sync, dynamic heights)

**Decision**: **Use react-virtual (TanStack Virtual)**

**Rationale**:
- Smallest bundle size (2.1KB) aligns with minimal dependencies
- Modern hooks-based API matches our React patterns
- Essential for performance with 100K+ log entries
- Maintained by reputable TanStack team
- Custom implementation would be error-prone and violate "don't reinvent the wheel"

**Implementation**:
- Use `@tanstack/react-virtual` for LogsTable and MetricsTable
- Implement with `useVirtualizer` hook
- Support variable row heights for expandable stack traces

---

## Decision 3: Visual Regression Testing

**Question**: Storybook + Chromatic vs lighter alternatives?

**Research**:
- Storybook: Heavy dependency (~10MB), excellent for component dev, industry standard
- Chromatic: Paid service for visual regression, free tier limited
- Playwright visual comparisons: Built-in screenshot comparison, no extra deps
- No visual regression: Rely on manual QA and E2E tests

**Decision**: **Use Storybook for component development and documentation**

**Rationale**:
- Team decision to invest in component-driven development workflow
- Storybook provides isolated component development environment
- Enables visual regression testing and component documentation
- Supports collaboration between developers and designers
- Living style guide for design system consistency
- Worth the additional dependencies for long-term maintainability

**Implementation**:
- Set up Storybook 7+ for React + TypeScript + Vite
- Create stories for all common components and feature components
- Use Storybook for visual testing and component documentation
- Optionally integrate Chromatic for automated visual regression testing
- Document component props, variants, and usage patterns in stories

---

## Decision 4: Deployment Target

**Question**: Where will the dashboard be deployed?

**Research**:
- Vercel: Free tier, automatic SSL, edge network, simple GitHub integration
- Netlify: Similar to Vercel, generous free tier
- Docker + self-hosted: Full control, can run anywhere, more ops work
- Azure Static Web Apps: Integrates with Azure backend, free tier available
- AWS S3 + CloudFront: Low cost, requires more manual setup

**Decision**: **Vercel (primary) with Docker support (secondary)**

**Rationale**:
- Vercel offers zero-config deployment for Vite apps
- Automatic preview deployments for PRs
- Edge network ensures fast global load times (<2s requirement)
- Free tier sufficient for MVP
- Docker support allows self-hosting for enterprise customers
- Backend is Spring Boot (can run anywhere), so frontend can be independent

**Implementation**:
- Add `vercel.json` configuration
- Create `Dockerfile` for self-hosted option
- Document both deployment methods in README
- Use environment variables for backend API URL (different per environment)

---

## Decision 5: Environment Configuration

**Question**: How to manage environment variables across dev/staging/production?

**Research**:
- Vite's .env files: Native support for .env.development, .env.production
- Runtime config: Fetch config from /config.json endpoint
- Build-time injection: Different builds per environment

**Decision**: **Vite .env files with VITE_ prefix**

**Rationale**:
- Native Vite support, zero additional dependencies
- Type-safe access via import.meta.env
- Different .env files per environment (development, staging, production)
- Git ignore .env.local for developer-specific overrides
- Build-time replacement for security (no runtime config exposure)

**Implementation**:
```
.env                  # Shared defaults
.env.development      # Local dev (http://localhost:8080 backend)
.env.staging          # Staging environment
.env.production       # Production values
.env.local            # Git-ignored, developer overrides
```

**Required variables**:
- `VITE_API_BASE_URL` - Backend API URL
- `VITE_APP_NAME` - Application name
- `VITE_ENABLE_DEBUG` - Debug mode toggle

---

## Decision 6: API Documentation Integration

**Question**: Embed OpenAPI/Swagger UI or rely on external docs?

**Research**:
- Embedding Swagger UI: Adds dependency, increases bundle size
- Link to external docs: No overhead, user navigates away
- No API docs in dashboard: Developers reference backend repo

**Decision**: **No embedded API docs - Link to external documentation**

**Rationale**:
- Dashboard users are end-users, not API developers
- API documentation belongs in backend repo or separate docs site
- Embedding Swagger UI adds unnecessary dependencies
- Link to documentation repo from dashboard footer or help section
- Focus dashboard on user tasks, not API reference

**Implementation**:
- Add "API Documentation" link in footer pointing to backend docs
- Create `/docs` route that redirects to backend OpenAPI URL (if available)
- No additional dependencies

---

## Decision 7: Date/Time Handling

**Question**: date-fns, day.js, or native Intl API?

**Research**:
- date-fns: 13KB gzipped (tree-shakeable), functional approach, popular
- day.js: 2KB gzipped, Moment.js-compatible API, smaller
- Native Intl API: 0KB, built-in, sufficient for basic formatting
- Temporal API: Future standard, not widely supported yet

**Decision**: **Native Intl API with lightweight utilities**

**Rationale**:
- Minimal dependencies principle
- Modern browsers fully support Intl.DateTimeFormat
- Most use cases: format timestamps, relative time ("2 hours ago"), timezone display
- Can achieve all requirements without external library
- If complex operations needed, add day.js (2KB only)

**Implementation**:
- Create `src/utils/formatters.ts` with native Intl utilities
- Implement formatDate, formatRelativeTime, formatDateTime
- Use `Intl.RelativeTimeFormat` for "X minutes ago"
- Use `Intl.DateTimeFormat` for timestamps

**Fallback**: Add day.js if timezone conversions or complex date math required

---

## Decision 8: Form Validation

**Question**: React Hook Form, Formik, or custom validation?

**Research**:
- React Hook Form: 9KB gzipped, excellent performance, minimal re-renders
- Formik: 13KB gzipped, more features but heavier
- Custom validation: HTML5 + useState, full control, zero dependencies
- Zod + React Hook Form: Type-safe schemas, 15KB combined

**Decision**: **HTML5 validation + custom hooks for complex forms**

**Rationale**:
- Most forms are simple (login, register, create application)
- HTML5 validation (required, pattern, type="email") covers 80% of cases
- Tailwind CSS can style validation states (`:invalid`, `:valid`)
- Custom useForm hook for complex forms if needed
- React Hook Form can be added later if forms become complex
- Aligns with minimal dependencies

**Implementation**:
- Use native HTML5 validation attributes
- Create `useFormValidation` hook for custom logic
- Style validation with Tailwind (`peer-invalid:`, etc.)
- Add React Hook Form only if form complexity increases significantly

**Validation examples**:
```tsx
<input 
  type="email" 
  required 
  pattern="[^@]+@[^@]+\.[^@]+" 
  className="peer ..."
/>
<p className="peer-invalid:block hidden text-red-500">Invalid email</p>
```

---

## Summary of Decisions

| Question | Decision | Rationale |
|----------|----------|-----------|
| Charts Library | Recharts | Battle-tested, React-native, worth 34KB for robust features |
| Virtual Scrolling | @tanstack/react-virtual | Smallest (2.1KB), essential for performance, modern API |
| Icons | lucide-react | Modern, lightweight, tree-shakeable, ~1KB per icon |
| Visual Regression | Storybook + optional Chromatic | Component-driven development, visual docs, team collaboration |
| Deployment | Vercel (primary) + Docker (secondary) | Zero-config deployment, free tier, self-host option |
| Environment Config | Vite .env files | Native support, type-safe, secure |
| API Documentation | External link, no embedding | Users don't need API docs, avoid dependencies |
| Date Handling | Native Intl API + utilities | Zero dependencies, modern browser support |
| Form Validation | HTML5 + custom hooks | Native validation sufficient, minimal dependencies |

---

## Dependency Analysis

**Absolute Required** (cannot avoid):
- React 18.3+
- TypeScript 5.3+
- Vite 5+
- React Router 6.21+
- Axios 1.6+
- Tailwind CSS 3.4+
- lucide-react (icons, ~1KB per icon with tree-shaking)

**Performance Essential** (justified by requirements):
- @tanstack/react-virtual (2.1KB) - Required for 100K+ log entries performance

**Testing Essential**:
- Vitest, React Testing Library, MSW, Playwright - Required by constitution

**Approved Additional Dependencies**:
- Recharts (~34KB gzipped) - For metrics visualization charts
- Storybook (dev dependency only) - For component development and documentation
- @tanstack/react-virtual (~2KB) - For virtual scrolling performance

**Deferred/Optional** (add only if proven necessary):
- React Hook Form - Only if HTML5 validation proves insufficient
- day.js - Only if complex date operations required

---

## Icon Library

### Question: Which icon library to use?

**Options Considered**:
- **lucide-react**: Modern fork of Feather Icons, 1000+ icons, tree-shakeable, ~1KB per icon
- **react-icons**: Aggregates multiple icon sets (Font Awesome, Material, etc.), 10+ icon packs
- **heroicons**: Official Tailwind CSS icons, 200+ icons, MIT license
- **Font Awesome React**: Popular but heavy, requires careful tree-shaking setup

**Decision: lucide-react**

**Rationale**:
- **Minimal bundle impact**: Tree-shaking ensures only imported icons are bundled (~1KB each)
- **Modern API**: Simple React component API, no need for font loading
- **Comprehensive set**: 1000+ consistent, high-quality icons covering all UI needs
- **TypeScript support**: Built-in TypeScript definitions, full IDE autocomplete
- **Tailwind friendly**: Works seamlessly with Tailwind classes (size, color)
- **Active maintenance**: Regular updates, community-driven

**Usage Pattern**:
```tsx
import { FileText, Eye, EyeOff, Copy, Check } from 'lucide-react';

// Size and color via props or Tailwind
<FileText size={24} className="text-blue-600" />
<Eye size={16} />
```

**Icon Mapping**:
- Logs: `FileText`
- Metrics/Charts: `BarChart3`
- Visibility toggle: `Eye` / `EyeOff`
- Copy action: `Copy`
- Success indicator: `Check`
- Alerts: `CheckCircle`, `XCircle`, `AlertTriangle`, `Info`
- Users/Teams: `Users`
- Generic empty state: `Inbox`


**Total Bundle Impact**: ~36KB additional production bundle (Recharts + react-virtual)