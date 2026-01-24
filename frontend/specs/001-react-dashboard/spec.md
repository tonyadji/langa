# Feature Specification: Langa Dashboard - React Observability Platform

**Feature Branch**: `001-react-dashboard`  
**Created**: December 31, 2025  
**Status**: Draft  
**Input**: User description: "Build a React Modern application that will be the dashboard of the Langa an observability and monitoring platform. The documentation about the platform is in the documentation folder."

## User Scenarios & Testing

### User Story 1 - User Authentication and Onboarding (Priority: P1)

New users need to register, login, and complete initial setup to access the Langa observability platform. This includes creating an account, authenticating with JWT tokens, and completing first-time setup.

**Why this priority**: Without authentication, users cannot access any platform features. This is the foundational capability that enables all other user journeys.

**Independent Test**: Can be fully tested by registering a new account, logging in with credentials, receiving JWT access/refresh tokens, and completing the first-time setup wizard. Delivers immediate value by allowing users to access the platform.

**Acceptance Scenarios**:

1. **Given** a user visits the registration page, **When** they provide a valid email and matching passwords, **Then** their account is created and they receive a success message
2. **Given** a registered user provides valid credentials (username/email and password) on the login page, **When** they submit the login form, **Then** they receive JWT access and refresh tokens and are redirected to the dashboard
3. **Given** a user's access token has expired, **When** the system detects the expiration, **Then** it automatically uses the refresh token to obtain a new access token without disrupting the user session
4. **Given** a user logs in for the first time, **When** they land on the dashboard, **Then** they see a setup wizard prompting them to complete their profile
5. **Given** an unauthenticated user tries to access a protected route, **When** the request is made, **Then** they are redirected to the login page

---

### User Story 2 - Application Management (Priority: P1)

Users need to create, view, and manage monitored applications. Each application represents a service or system being monitored, with unique credentials for log and metric ingestion.

**Why this priority**: Creating and managing applications is the core workflow - without applications, there's nothing to monitor. This is essential for platform adoption.

**Independent Test**: Can be fully tested by creating a new application with a name, viewing the list of owned and shared applications, accessing application details including ingestion credentials, and verifying the application appears in the list. Delivers value by enabling users to start monitoring their services.

**Acceptance Scenarios**:

1. **Given** an authenticated user on the applications page, **When** they click "Create Application" and provide a unique name, **Then** a new application is created with auto-generated keys, secret, and ingestion URI
2. **Given** a user has created multiple applications, **When** they view the applications list, **Then** they see all applications they own plus applications shared with them
3. **Given** a user owns an application, **When** they click on the application to view details, **Then** they see the full configuration including application key, account key, secret, and ingestion endpoints
4. **Given** a user views a shared application (not owner), **When** they access application details, **Then** they see limited information without sensitive credentials
5. **Given** a user tries to create an application with a name that already exists for their account, **When** they submit the form, **Then** they see an error message indicating the name must be unique

---

### User Story 3 - Log Viewing and Filtering (Priority: P1)

Users need to view, search, and filter logs from their monitored applications to troubleshoot issues and understand system behavior. This includes filtering by time range, log level, keyword search, and pagination.

**Why this priority**: Viewing logs is the primary use case for an observability platform. Users need this immediately after setting up applications to get value from the platform.

**Independent Test**: Can be fully tested by querying logs for an application, applying filters (level, logger name, time range), and paginating through results. Delivers value by enabling users to troubleshoot and monitor their applications.

**Acceptance Scenarios**:

1. **Given** a user selects an application with ingested logs, **When** they navigate to the logs view, **Then** they see the most recent logs in reverse chronological order with pagination
2. **Given** a user is viewing logs, **When** they apply a log level filter (e.g., ERROR only), **Then** only logs matching that level are displayed
3. **Given** a user is viewing logs, **When** they specify a time range filter (start and end timestamps), **Then** only logs within that time window are displayed
4. **Given** a user is viewing logs, **When** they filter by logger name, **Then** only logs from that specific logger are displayed
5. **Given** there are more than 100 log entries, **When** the user views logs, **Then** they see paginated results with controls to navigate between pages
6. **Given** a user is viewing a log entry with an error, **When** the log includes a stack trace, **Then** the stack trace is displayed in a readable, expandable format
7. **Given** a user applies multiple filters (level + time range + logger name), **When** the query executes, **Then** logs matching all criteria are displayed

---

### User Story 4 - Metrics Visualization and Querying (Priority: P2)

Users need to view and analyze metrics from their applications, including performance data, request durations, success/failure rates, and HTTP metrics. This enables performance monitoring and trend analysis.

**Why this priority**: Metrics complement logs by providing quantitative performance data. While important, it's secondary to log viewing since troubleshooting often starts with logs.

**Independent Test**: Can be fully tested by querying metrics for an application, filtering by metric name and status, visualizing time-series data, and analyzing performance trends. Delivers value by enabling proactive performance monitoring.

**Acceptance Scenarios**:

1. **Given** a user selects an application with ingested metrics, **When** they navigate to the metrics view, **Then** they see metric entries with duration, status, timestamp, and HTTP details
2. **Given** a user is viewing metrics, **When** they filter by metric name, **Then** only metrics matching that name are displayed
3. **Given** a user is viewing metrics, **When** they filter by status (SUCCESS or FAILURE), **Then** only metrics with that status are displayed
4. **Given** a user is viewing HTTP metrics, **When** they examine the details, **Then** they see URI, HTTP method, and HTTP status code for each entry
5. **Given** a user is viewing metrics over time, **When** the data is visualized, **Then** they see a time-series chart showing performance trends
6. **Given** a user wants to analyze response times, **When** they view metric durations, **Then** they see statistics like average, median, p95, and p99 response times

---

### User Story 5 - Application Sharing and Collaboration (Priority: P2)

Application owners need to share read access with other users or teams, enabling collaboration across organizations. This supports team-based monitoring and centralized visibility.

**Why this priority**: Sharing is important for team collaboration but not essential for initial platform use. Individual users can get value before needing to share.

**Independent Test**: Can be fully tested by sharing an application with a specific user email or team key, verifying the shared user can view (but not modify) the application, and revoking access. Delivers value by enabling cross-team collaboration.

**Acceptance Scenarios**:

1. **Given** a user owns an application, **When** they share it with another user's email, **Then** that user can view the application in their applications list with restricted permissions
2. **Given** a user owns an application, **When** they share it with a team key, **Then** all team members can view the application with restricted permissions
3. **Given** an application is shared with a user, **When** the shared user views application details, **Then** they see logs and metrics but not sensitive credentials (keys, secrets)
4. **Given** a user has shared an application, **When** they view the sharing configuration, **Then** they see a list of all users and teams with access and when access was granted
5. **Given** a user owns an application, **When** they revoke sharing from a specific user or team, **Then** that user/team can no longer access the application
6. **Given** a user tries to share an application they don't own, **When** they attempt the share action, **Then** they see an error indicating only owners can share
7. **Given** an application share has been revoked, **When** the owner views the share list, **Then** they see a "Revoked" badge, the revoked date, and the revoke action is disabled for that share
8. **Given** a share has an expiration date, **When** viewing the share list, **Then** the UI shows expiration status and date (if configured)

---

### User Story 6 - Team Management and Invitations (Priority: P3)

Users need to create teams, invite members, accept invitations, and manage team membership. Teams provide a way to organize users and share applications at the team level.

**Why this priority**: Teams are a convenience feature for larger organizations but not essential for core observability functionality. Can be added after individual user workflows are stable.

**Independent Test**: Can be fully tested by creating a team, inviting users via email, accepting invitations, and managing member roles. Delivers value by simplifying multi-user application sharing.

**Acceptance Scenarios**:

1. **Given** an authenticated user, **When** they create a new team with a unique name, **Then** the team is created with them as the OWNER
2. **Given** a user owns or is an admin of a team, **When** they invite a user by email with a specific role (ADMIN or MEMBER), **Then** an invitation is sent to that user's email
3. **Given** a user receives a team invitation, **When** they view their pending invitations, **Then** they see the team name, inviter, proposed role, and expiration date
4. **Given** a user has a pending team invitation, **When** they accept it, **Then** they become a member of the team with the specified role
5. **Given** a team owner or admin, **When** they view team members, **Then** they see all members with their roles and join dates
6. **Given** a team owner or admin, **When** they remove a member from the team, **Then** that user loses access to team-shared applications
7. **Given** a team admin, **When** they try to remove the team owner, **Then** they see an error indicating only owners can be removed by themselves

---

### User Story 7 - Application Usage Monitoring (Priority: P3)

Application owners need to monitor storage consumption for logs and metrics to understand resource usage and plan capacity. This provides visibility into data growth and costs.

**Why this priority**: Usage monitoring is valuable for resource planning but not critical for initial platform adoption. Can be deferred until users have substantial data volume.

**Independent Test**: Can be fully tested by viewing storage statistics for an application showing total bytes used by logs and metrics, visualizing usage trends over time. Delivers value by enabling cost management and capacity planning.

**Acceptance Scenarios**:

1. **Given** a user owns an application, **When** they view usage statistics, **Then** they see total bytes consumed by logs and metrics separately
2. **Given** an application has ingested data over time, **When** usage is visualized, **Then** the user sees a trend chart showing growth in storage consumption
3. **Given** a user wants to understand data retention costs, **When** they view usage, **Then** they see a breakdown of storage by time period (last 7 days, 30 days, 90 days)

---

### Edge Cases

- What happens when a user's refresh token expires? The system should redirect to login and require re-authentication
- How does the system handle network failures during API requests? Implement retry logic with exponential backoff and show user-friendly error messages
- What happens when a user tries to access an application that was deleted? Show a "not found" error and redirect to applications list
- How does pagination handle real-time log ingestion? New logs should appear on refresh, but pagination should remain stable during a session
- What happens when filtering logs with a very large time range (e.g., 1 year)? Implement query limits and warn users about performance implications
- How does the system handle malformed API responses? Validate response schemas and show graceful error messages without crashing
- What happens when multiple filters return zero results? Display an empty state with clear guidance on adjusting filters
- How are very long log messages or stack traces displayed? Implement text truncation with "show more" expansion
- What happens when a user has access to 100+ applications? Implement search and sorting in the applications list
- How does the system handle concurrent sharing conflicts (two owners sharing with same user simultaneously)? Backend handles this, but UI should refresh to show current state

## Requirements

### Functional Requirements

- **FR-001**: System MUST implement user registration with email validation and password confirmation
- **FR-002**: System MUST authenticate users using JWT tokens (access and refresh) matching the backend specification
- **FR-003**: System MUST automatically refresh expired access tokens using valid refresh tokens without user intervention
- **FR-004**: System MUST redirect unauthenticated users to the login page when accessing protected routes
- **FR-005**: System MUST display a first-time setup wizard for new users to complete profile configuration
- **FR-006**: System MUST allow users to create applications by providing a unique name
- **FR-007**: System MUST display a list of all applications the user owns or has shared access to
- **FR-008**: System MUST show full application details (including keys and secrets) to application owners
- **FR-009**: System MUST show restricted application details (without sensitive credentials) to users with shared access
- **FR-010**: System MUST display logs for an application with pagination (100 entries per page by default)
- **FR-011**: System MUST support filtering logs by level, keyword search, and time range (start/end timestamps)
- **FR-012**: System MUST support combining multiple log filters simultaneously
- **FR-013**: System MUST display log stack traces in an expandable, readable format
- **FR-014**: System MUST display log MDC (Mapped Diagnostic Context) data when available
- **FR-015**: System MUST display metrics for an application with pagination
- **FR-016**: System MUST support filtering metrics by metric name, status, URI, HTTP method, HTTP status, duration ranges, keyword, and time range
- **FR-017**: System MUST visualize metrics as time-series charts showing performance trends
- **FR-018**: System MUST calculate and display metric statistics (average, median, p95, p99 durations)
- **FR-019**: System MUST allow application owners to share applications with users by email address
- **FR-020**: System MUST allow application owners to share applications with teams by team key
- **FR-021**: System MUST allow application owners to revoke sharing from users or teams
- **FR-022**: System MUST display sharing configuration showing all users/teams with access
- **FR-023**: System MUST prevent non-owners from sharing or modifying applications
- **FR-024**: System MUST allow users to create teams with unique names
- **FR-025**: System MUST allow team owners and admins to invite users by email with specific roles (OWNER, ADMIN, MEMBER)
- **FR-026**: System MUST display pending team invitations for the current user
- **FR-027**: System MUST allow users to accept team invitations
- **FR-028**: System MUST display team members with their roles and join dates
- **FR-029**: System MUST allow team owners and admins to remove team members
- **FR-030**: System MUST prevent team admins from removing team owners
- **FR-031**: System MUST display application storage usage statistics (log bytes and metric bytes)
- **FR-032**: System MUST visualize usage trends over time with charts
- **FR-033**: System MUST handle API errors gracefully with user-friendly messages
- **FR-034**: System MUST implement loading states for all asynchronous operations
- **FR-035**: System MUST display empty states with helpful guidance when no data is available
- **FR-036**: System MUST allow application owners to configure data retention policies (duration and unit)
- **FR-037**: System MUST allow application owners to delete applications permanently
- **FR-038**: System MUST display retention policy information in application details
- **FR-039**: System MUST provide logout functionality that invalidates user tokens
- **FR-040**: System MUST support first-connection flow for users invited via email
- **FR-041**: System MUST fetch user profile data from `/users/me` endpoint after successful authentication
- **FR-042**: System MUST allow users to configure theme preference (Light/Dark/System) in Profile settings
- **FR-043**: System MUST split team invitations into "Pending" and "Accepted" sections (owner-only visibility)
- **FR-044**: System MUST use 0-based page indexing for backend API calls while displaying 1-based page numbers to users
- **FR-045**: System MUST extract totalPages directly from backend pagination response

### UI/UX Requirements

- **UX-001**: System MUST support dark mode across all pages and components with proper contrast ratios
- **UX-002**: System MUST use a 3-tier layout structure with fixed navigation, fixed page headers, and scrollable content
- **UX-003**: Table headers in Logs and Metrics pages MUST be sticky during vertical scrolling
- **UX-004**: System MUST use lightweight scrollbars (6px width, semi-transparent at 30% opacity) for modern aesthetics
- **UX-005**: Log level filters MUST display as toggle buttons showing full level names (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
- **UX-006**: Filter sections MUST be compact and display on single line with responsive flex-wrap behavior
- **UX-007**: Page headers MUST consolidate title and primary actions/selectors on the same line for space optimization
- **UX-008**: Applications page MUST display search input inline with page title
- **UX-009**: Application Details page MUST have fixed "Back to Applications" link with scrollable content below
- **UX-010**: Scrollbars MUST have hover state with increased opacity (50%) for better visibility
- **UX-011**: System MUST persist theme preference in localStorage as 'langa-theme'
- **UX-012**: Main layout MUST fill full viewport height (h-screen) with flex column structure

### Key Entities

- **User**: Represents an authenticated user with email, account key, and role. Owns applications and belongs to teams.
- **Application**: Represents a monitored service/system with unique identifiers (id, key, account key), ingestion credentials (secret, URI), ownership information, and sharing configuration.
- **LogEntry**: Represents a log message with level, logger name, timestamp, message content, optional thread name, stack trace, and MDC data.
- **MetricEntry**: Represents a performance metric with name, duration in milliseconds, status, timestamp, and HTTP metadata (URI, method, status code).
- **Team**: Represents a group of users with a unique name and key, containing members with roles (OWNER, ADMIN, MEMBER).
- **TeamInvitation**: Represents a pending invitation to join a team with inviter, invitee, role, status, and expiration date.
- **ApplicationUsage**: Represents storage consumption statistics for an application, tracking total bytes used by logs and metrics, plus historical usage trends for visualization.
- **RetentionPolicy**: Defines data retention rules with duration, time unit, and last update timestamp.
- **ShareWith**: Represents sharing configuration indicating a user or team has access to an application, with shared-at timestamp.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Users can complete account registration and login in under 1 minute
- **SC-002**: Users can create a new application and view its credentials in under 30 seconds
- **SC-003**: Log filtering (by level, time range, logger) returns results in under 2 seconds for datasets up to 100,000 entries
- **SC-004**: Dashboard initial load time is under 2 seconds on 3G connection
- **SC-005**: 90% of users successfully complete their first application creation without errors
- **SC-006**: Metric visualization renders charts for 1,000 data points in under 1 second
- **SC-007**: Application sharing is completed in under 3 clicks from the application details page
- **SC-008**: System handles 1,000 concurrent users viewing logs without performance degradation
- **SC-009**: Zero application crashes due to malformed API responses (graceful error handling)
- **SC-010**: Pagination remains stable during active log ingestion (no skipped or duplicate entries in a session)
- **SC-011**: Refresh token expiration is handled transparently 95% of the time without requiring re-login
- **SC-012**: Users can find a specific application in a list of 100+ applications in under 10 seconds using search
---

## Implementation Notes

### Icon System
The application uses **lucide-react** as the icon library for a professional, consistent visual experience. Icons are tree-shakeable (approximately 1KB per icon) and include:

- **FileText**: Log entries and document references
- **BarChart3**: Metrics and analytics
- **Eye/EyeOff**: Password visibility toggles
- **Copy/Check**: Copy-to-clipboard actions with success feedback
- **CheckCircle/XCircle/AlertTriangle/Info**: Alert component status indicators
- **Users**: Team and user management
- **Inbox**: Empty states and placeholders

All icons use consistent sizing (w-5 h-5 for standard, w-4 h-4 for inline) and follow Tailwind color utilities for theming.

### Component Library
The application implements a custom component library built with Tailwind CSS, avoiding heavy UI frameworks:

**Common Components**:
- **Badge**: Reusable badge component with variants (default, success, warning, error, info) and sizes (sm, md, lg) for status indicators and labels
- **Alert**: Context-aware notifications with lucide-react icons
- **Button, Input, Modal, Card, Table, Pagination**: Core UI components with consistent styling
- **EmptyState**: User-friendly empty state component with icon support
- **Spinner**: Loading indicator for async operations
- **ErrorMessage**: Standardized error display

**Layout Components**:
- **Navbar**: Top navigation bar with:
  - Brand logo and primary navigation links (Dashboard, Applications, Logs, Metrics, Teams)
  - Active route highlighting with blue border-bottom indicator
  - User dropdown menu with profile access and logout
  - Responsive design with mobile-friendly interactions
  - Fixed positioning (sticky at top of viewport)
- **Layout**: Main layout wrapper implementing 3-tier architecture:
  - **Tier 1 - Viewport Container**: `h-screen flex flex-col overflow-hidden`
  - **Tier 2 - Navbar**: Fixed at top (no flex-grow)
  - **Tier 3 - Main Content**: `flex-1 overflow-hidden` with max-width container
  - Pages receive `h-full` container for their own layout control

### Layout Architecture (Critical)
The application uses a consistent 3-tier layout structure across all pages:

**Tier 1 - Main Layout** (`components/layout/Layout.tsx`):
- Full viewport height (`h-screen flex flex-col`)
- Overflow hidden to prevent body scroll
- Fixed navbar at top
- Main content area with `flex-1 overflow-hidden`

**Tier 2 - Page Structure** (ApplicationsPage, LogsPage, MetricsPage, ApplicationDetailsPage):
- **Container**: `flex flex-col h-full`
- **Fixed Header Section**: `flex-none` with:
  - Page title and primary actions/selectors on same line
  - Filter components (compact, single-line with flex-wrap)
  - Bottom border for visual separation
- **Scrollable Content**: `flex-1 overflow-y-auto scrollbar-light`
  - Independent scroll control
  - Custom lightweight scrollbar styling

**Tier 3 - Table Components** (LogsTable, MetricsTable):
- **Table Headers**: `sticky top-0 z-10` with explicit background colors
- **Table Body**: Standard tbody with pagination controls
- Headers remain visible during vertical scroll

### Scrollbar Styling
Custom lightweight scrollbar implementation in `src/styles/index.css`:

```css
.scrollbar-light {
  scrollbar-width: thin;
  scrollbar-color: rgba(156, 163, 175, 0.3) transparent;
}

.scrollbar-light::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.scrollbar-light::-webkit-scrollbar-track {
  background: transparent;
}

.scrollbar-light::-webkit-scrollbar-thumb {
  background-color: rgba(156, 163, 175, 0.3);
  border-radius: 3px;
}

.scrollbar-light::-webkit-scrollbar-thumb:hover {
  background-color: rgba(156, 163, 175, 0.5);
}

/* Dark mode variants */
.dark .scrollbar-light {
  scrollbar-color: rgba(75, 85, 99, 0.5) transparent;
}

.dark .scrollbar-light::-webkit-scrollbar-thumb {
  background-color: rgba(75, 85, 99, 0.5);
}

.dark .scrollbar-light::-webkit-scrollbar-thumb:hover {
  background-color: rgba(75, 85, 99, 0.7);
}
```

**Key Features**:
- Width: 6px (vs default ~15px)
- Semi-transparent (30% opacity, 50% on hover)
- Automatic dark mode adaptation
- Applied to all scrollable content areas

### Pagination Pattern
All paginated endpoints follow this standardized pattern:

**Frontend (User-Facing)**:
- Page numbers are 1-based (page 1, 2, 3...)
- User sees natural page numbering in UI
- Current page state: `const [currentPage, setCurrentPage] = useState(1);`

**API Layer (Backend Communication)**:
- Convert to 0-based before API call: `const page = frontendPage > 0 ? frontendPage - 1 : 0;`
- Send `page` and `size` parameters to backend
- Example: Frontend page 1 → Backend page 0

**Response Handling**:
- Extract `totalPages` directly from backend response
- Don't calculate from `total / limit` (can cause off-by-one errors)
- Convert backend page back to frontend if needed: `page: frontendPage`

**Implementation Examples**:
- `src/features/logs/api/logsApi.ts`
- `src/features/metrics/api/metricsApi.ts`
- `src/services/applicationApi.ts`

**Critical**: Always destructure `page` and `limit` from params before spreading to prevent overwriting:
```typescript
const { page: _, limit: __, ...otherParams } = params;
const queryParams = { ...otherParams, page, size };
```

### Routing Structure
The application implements the following route structure using React Router v6:

**Public Routes**:
- `/` - Landing/redirect to login or dashboard
- `/login` - User login page
- `/register` - User registration page

**Protected Routes** (require authentication):
- `/dashboard` - Main dashboard with setup wizard for first-time users
- `/applications` - List of all owned and shared applications
- `/applications/:id` - Application details with credentials (owners only)
- `/logs` - Log viewer with filtering and pagination
- `/metrics` - Metrics visualization and querying
- `/profile` - User profile and account settings

**Route Protection**: All protected routes use the `ProtectedRoute` wrapper component that:
- Checks authentication status with loading state handling
- Redirects to `/login` if unauthenticated
- Displays loading spinner during session verification
- Preserves intended destination for post-login redirect

### Session Persistence
User sessions are maintained across page refreshes using:
- Access token stored in memory (React state)
- Refresh token stored in localStorage (`langa_refresh_token`)
- User data cached in localStorage (`langa_user`)
- Automatic token refresh on 401 errors via Axios interceptor
- Session restoration on application mount through `AuthContext`

### Date & Time Formatting
The application uses the **native Intl API** for all date and time formatting, avoiding external dependencies:
- `Intl.DateTimeFormat` for locale-aware date formatting
- `Intl.RelativeTimeFormat` for relative time displays ("2 hours ago", "3 days ago")
- Custom utility functions in `src/utils/formatters.ts` for common formats:
  - `formatDate()`: Standard date formatting with customizable options
  - `formatDateTime()`: Combined date and time display
  - `formatRelativeTime()`: Human-friendly relative timestamps
  - `formatDuration()`: Millisecond to human-readable duration conversion

This approach keeps the bundle size minimal while providing full internationalization support.

### Data Visualization
Metrics visualization uses **Recharts** (React-native charting library):
- `LineChart` for time-series metric data with responsive container
- Custom tooltips with formatted values and timestamps
- Configurable chart colors, units, and titles
- Lightweight implementation (~34KB gzipped) with tree-shaking support
- Located in `src/features/metrics/components/MetricChart.tsx`

Note: Virtual scrolling with `@tanstack/react-virtual` was planned for LogsTable and MetricsTable but the current implementation uses standard pagination instead, optimized for datasets under 100,000 entries as per SC-003 requirements.

### Log Viewing Features
The LogsTable component includes advanced features for troubleshooting:
- **Expandable Rows**: Click any log entry to reveal detailed metadata
- **Stack Trace Display**: Error logs with stack traces show full trace in expanded view
- **MDC (Mapped Diagnostic Context)**: Metadata displayed as key-value pairs when available
- **Thread Information**: Thread names displayed for concurrent execution tracking
- **Pagination**: Standard pagination controls for navigating large log datasets
- **Empty States**: User-friendly guidance when no logs match current filters