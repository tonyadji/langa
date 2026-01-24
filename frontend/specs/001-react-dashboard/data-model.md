# Data Model: Langa Dashboard TypeScript Interfaces

**Feature**: 001-react-dashboard  
**Date**: December 31, 2025  
**Status**: Complete

## Purpose

This document defines all TypeScript interfaces and types for the Langa Dashboard, matching the backend API specification exactly. These types will be used throughout the application for type safety and autocomplete.

---

## Core Entities

### User

Represents an authenticated user in the system.

```typescript
export interface User {
  email: string;              // Unique user identifier
  accountKey: string;         // Unique account key for multi-tenancy
  role: string;               // User role (e.g., "USER", "ADMIN")
  firstConnection: boolean;   // Whether user has completed first-time setup
  registrationDate?: string;  // ISO 8601 timestamp of registration
}
```

**Usage**: Stored in AuthContext after login, used for permission checks

---

### Application

Represents a monitored application/service.

```typescript
export interface Application {
  id: string;                 // Unique application identifier
  name: string;               // Application name (unique per owner)
  key: string;                // Application key for ingestion
  accountKey: string;         // Account key for multi-tenancy
  ingestionUri: string;       // HTTP ingestion endpoint URL
  owner: string;              // Email of application owner
  sharedWith: ShareWith[];    // Users/teams with shared access
}

export interface ApplicationSecured extends Application {
  secret: string;             // Secret for HMAC signature validation
  retentionPolicy: RetentionPolicy;  // Data retention policy
  http: string;               // HTTP ingestion endpoint
  kafka: string;              // Kafka ingestion endpoint/topic
  usage: ApplicationUsage;    // Storage usage statistics
}
```

**Notes**: 
- `Application` is returned for shared applications (non-owners)
- `ApplicationSecured` is returned for owned applications with sensitive data
- UI must check ownership to determine which type is available

---

### ShareWith

Represents sharing configuration for an application, including revocation and expiration tracking.

```typescript
export interface ShareWith {
  appId: string;                   // Application identifier
  appName: string;                 // Application name
  key: string;                     // User account key or team key
  profile: SharedWithProfile;      // USER or TEAM
  sharedDate: string;              // ISO 8601 timestamp when shared
  expirationDate: string | null;   // Optional expiration timestamp (null = never expires)
  revokedDate: string | null;      // Timestamp when access was revoked (null = not revoked)
  currentlyActive: boolean;        // Whether share is currently active (not expired/revoked)
  expired: boolean;                // Whether share has passed expiration date
  revoked: boolean;                // Whether share has been explicitly revoked
}

export enum SharedWithProfile {
  USER = 'USER',
  TEAM = 'TEAM'
}
```

**Usage**: Displayed in application details to show who has access, including revocation history

**Backend Extensions**: The backend returns extended ShareWith objects that include:
- Revocation tracking (`revoked`, `revokedDate`)
- Expiration management (`expirationDate`, `expired`)
- Active status calculation (`currentlyActive`)
- Application context (`appId`, `appName`)

---

### LogEntry

Represents a log entry from an application.

```typescript
export interface LogEntry {
  appKey: string;             // Application identifier
  accountKey: string;         // Account identifier
  message: string;            // Log message content
  level: LogLevel;            // Log severity level
  loggerName: string;         // Logger/class name
  timestamp: string;          // ISO 8601 timestamp
  threadName?: string;        // Thread name (optional)
  stackTrace?: string;        // Stack trace for errors (optional)
  mdc?: Record<string, string>;  // Mapped Diagnostic Context (optional)
}

export enum LogLevel {
  TRACE = 'TRACE',
  DEBUG = 'DEBUG',
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
  FATAL = 'FATAL'
}
```

**Usage**: Displayed in logs table, filterable by level/logger/time

---

### MetricEntry

Represents a performance metric from an application.

```typescript
export interface MetricEntry {
  appKey: string;             // Application identifier
  accountKey: string;         // Account identifier
  name: string;               // Metric name
  durationMillis: number;     // Duration in milliseconds
  status: MetricStatus;       // SUCCESS, FAILURE, etc.
  timestamp: string;          // ISO 8601 timestamp
  uri?: string;               // Request URI (for HTTP metrics)
  httpMethod?: string;        // HTTP method (GET, POST, etc.)
  httpStatus?: number;        // HTTP status code
}

export enum MetricStatus {
  SUCCESS = 'SUCCESS',
  FAILURE = 'FAILURE',
  TIMEOUT = 'TIMEOUT',
  ERROR = 'ERROR'
}
```

**Usage**: Displayed in metrics table and charts, filterable by name/status/time

---

### Team

Represents a group of users for collaboration.

```typescript
export interface Team {
  id: string;                 // Unique team identifier
  name: string;               // Team name
  key: string;                // Unique team key
  members: TeamMember[];      // Team members with roles
  createdAt: string;          // ISO 8601 timestamp
  createdBy: string;          // Creator email
}
```

---

### TeamMember

Represents a member of a team.

```typescript
export interface TeamMember {
  email: string;              // Member email
  role: TeamRole;             // Member role in team
  teamKey: string;            // Team identifier
  addedDate: string;          // ISO 8601 timestamp when added
}

export enum TeamRole {
  OWNER = 'OWNER',            // Team creator, full control
  ADMIN = 'ADMIN',            // Can manage members and settings
  MEMBER = 'MEMBER'           // Standard access
}
```

---

### TeamInvitation

Represents a pending invitation to join a team.

```typescript
export interface TeamInvitation {
  identity: TeamInvitationIdentity;           // Composite identifier
  stakeHolders: TeamInvitationStakeHolders;   // Involved parties
  invitationPeriod: TeamInvitationPeriod;     // Time period
  acceptedDate: string | null;                // When accepted (null if not accepted)
  status: InvitationStatus;                   // Current status
  teamId: string;                             // Team identifier
  token: string;                              // Invitation token
  expired: boolean;                           // Whether invitation has expired
}

export interface TeamInvitationIdentity {
  teamId: string;             // Team identifier
  invitationToken: string;    // Unique invitation token
}

export interface TeamInvitationStakeHolders {
  team: string;               // Team name
  host: string;               // Inviter email
  guest: string;              // Invitee email
}

export interface TeamInvitationPeriod {
  inviteDate: string;         // ISO 8601 timestamp when sent
  expiryDate: string;         // ISO 8601 timestamp when expires
}

export enum InvitationStatus {
  CREATED = 'CREATED',
  SENT = 'SENT',
  ACCEPTED = 'ACCEPTED',
  EXPIRED = 'EXPIRED'
}
```

---

### ApplicationUsage

Tracks storage consumption for an application.

```typescript
export interface ApplicationUsage {
  id: string;                 // Application identifier
  key: string;                // Application key
  name: string;               // Application name
  logUsage: number;           // Total bytes used by logs
  metricUsage: number;        // Total bytes used by metrics
  trends: ApplicationUsageTrend[];  // Historical usage trends
}
```

**Usage**: Displayed in application details, visualized as charts with trend charts

---

### RetentionPolicy

Defines data retention rules for logs and metrics.

```typescript
export interface RetentionPolicy {
  duration: number;           // Retention duration value
  unit: RetentionUnit;        // Time unit (Days, Weeks, Months, Years, etc.)
  lastUpdatedDate: string;    // ISO 8601 timestamp of last policy update
}

export enum RetentionUnit {
  Nanos = 'Nanos',
  Micros = 'Micros',
  Millis = 'Millis',
  Seconds = 'Seconds',
  Minutes = 'Minutes',
  Hours = 'Hours',
  HalfDays = 'HalfDays',
  Days = 'Days',
  Weeks = 'Weeks',
  Months = 'Months',
  Years = 'Years',
  Decades = 'Decades',
  Centuries = 'Centuries',
  Millennia = 'Millennia',
  Eras = 'Eras',
  Forever = 'Forever'
}
```

**Usage**: Displayed in application settings, allows owners to configure data retention duration

---

### ApplicationUsageTrend

Historical usage data point for trend visualization.

```typescript
export interface ApplicationUsageTrend {
  name: string;               // Application name
  key: string;                // Application key
  usage: number;              // Bytes consumed at this point
  type: UsageTrendType;       // LOG or METRIC
  createdDate: string;        // ISO 8601 timestamp of data point
}

export enum UsageTrendType {
  LOG = 'LOG',
  METRIC = 'METRIC'
}
```

**Usage**: Visualized as time-series charts showing storage growth over time

---

## API Request/Response Types

### Authentication

```typescript
export interface RegisterRequest {
  username: string;           // Email address
  password: string;           // Password
  confirmationPassword: string;  // Password confirmation
}

export interface LoginRequest {
  username: string;           // Username, email, or any identifier
  password: string;           // Password
}

export interface AuthResponse {
  accessToken: string;        // JWT access token
  refreshToken: string;       // JWT refresh token
  email: string;              // User email
}

export interface RefreshTokenRequest {
  refreshToken: string;       // Refresh token to exchange
}
```

---

### Applications

```typescript
export interface CreateApplicationRequest {
  name: string;               // Application name (must be unique per owner)
}

export interface ShareApplicationRequest {
  shareWith: string;          // User email or team key
  profile: SharedWithProfile; // USER or TEAM
}

export interface RevokeAccessRequest {
  shareWith: string;          // User email or team key
  profile: SharedWithProfile; // USER or TEAM
}
```

---

### Teams

```typescript
export interface CreateTeamRequest {
  name: string;               // Team name
}

export interface InviteTeamMemberRequest {
  teamKey: string;            // Team identifier
  guestEmail: string;         // Email to invite
  role: TeamRole;             // Proposed role
}

export interface AcceptInvitationRequest {
  guest: string;              // Guest email
  invitationToken: string;    // Invitation token to accept
}

export interface UpdateRetentionPolicyRequest {
  duration: number;           // Retention duration value
  unit: RetentionUnit;        // Time unit
}

export interface CompleteFirstConnectionRequest {
  firstConnectionToken: string;  // Token from email
  password: string;              // New password
  confirmationPassword: string;  // Password confirmation
}

export interface GetInvitationResponse {
  id: string;                 // Invitation identifier
  token: string;              // Invitation token
  team: string;               // Team name
  host: string;               // Inviter email
  guest: string;              // Invitee email
  expiryDate: string;         // ISO 8601 timestamp
  status: InvitationStatus;   // Current status
}
```

---

### Pagination

```typescript
export interface PaginatedResponse<T> {
  content: T[];               // Array of items
  totalElements: number;      // Total count of items
  totalPages: number;         // Total number of pages
  page: number;               // Current page (0-indexed)
  size: number;               // Items per page
}

export interface ApplicationLogsResponse {
  appName: string;            // Application name
  paginatedLogs: PaginatedResponse<LogEntry>;  // Paginated log data
}

export interface ApplicationMetricsResponse {
  appName: string;            // Application name
  paginatedMetrics: PaginatedResponse<MetricEntry>;  // Paginated metric data
}

export interface PaginationParams {
  page?: number;              // Page number (default: 0)
  size?: number;              // Page size (default: 100)
}
```

**Usage**: Used for logs and metrics endpoints

---

### Filtering

```typescript
export interface LogFilterParams extends PaginationParams {
  level?: string;             // Filter by log level
  keyword?: string;           // Search keyword (searches across message, logger, etc.)
  startDate?: string;         // ISO 8601 start timestamp
  endDate?: string;           // ISO 8601 end timestamp
}

export interface MetricFilterParams extends PaginationParams {
  name?: string;              // Filter by metric name
  status?: string;            // Filter by status (SUCCESS, FAILURE, etc.)
  uri?: string;               // Filter by request URI
  httpMethod?: string;        // Filter by HTTP method (GET, POST, etc.)
  httpStatus?: number;        // Filter by HTTP status code
  durationLessThan?: number;  // Filter by max duration (milliseconds)
  durationGreaterThan?: number;  // Filter by min duration (milliseconds)
  keyword?: string;           // Search keyword
  startDate?: string;         // ISO 8601 start timestamp
  endDate?: string;           // ISO 8601 end timestamp
}
```

---

## Error Responses

```typescript
export interface ApiError {
  message: string;            // Human-readable error message
  status: number;             // HTTP status code
  timestamp: string;          // ISO 8601 timestamp
  path?: string;              // Request path that caused error
  errors?: ValidationError[]; // Validation errors (if applicable)
}

export interface ValidationError {
  field: string;              // Field name
  message: string;            // Validation error message
}
```

---

## Component State Types

### Authentication State

```typescript
export interface AuthState {
  user: User | null;          // Current authenticated user
  accessToken: string | null; // JWT access token
  refreshToken: string | null;  // JWT refresh token
  isAuthenticated: boolean;   // Whether user is logged in
  isLoading: boolean;         // Auth operation in progress
  error: string | null;       // Auth error message
}

export type AuthAction =
  | { type: 'LOGIN_START' }
  | { type: 'LOGIN_SUCCESS'; payload: { user: User; accessToken: string; refreshToken: string } }
  | { type: 'LOGIN_FAILURE'; payload: string }
  | { type: 'LOGOUT' }
  | { type: 'TOKEN_REFRESH_SUCCESS'; payload: string }
  | { type: 'CLEAR_ERROR' };
```

---

### Application State

```typescript
export interface ApplicationListState {
  applications: Application[];
  isLoading: boolean;
  error: string | null;
}

export interface ApplicationDetailsState {
  application: Application | ApplicationSecured | null;
  usage: ApplicationUsage | null;
  isLoading: boolean;
  error: string | null;
}
```

---

### Logs State

```typescript
export interface LogsState {
  logs: PaginatedResponse<LogEntry> | null;
  filters: LogFilterParams;
  isLoading: boolean;
  error: string | null;
}
```

---

### Metrics State

```typescript
export interface MetricsState {
  metrics: PaginatedResponse<MetricEntry> | null;
  filters: MetricFilterParams;
  stats: MetricStats | null;
  isLoading: boolean;
  error: string | null;
}

export interface MetricStats {
  average: number;            // Average duration
  median: number;             // Median duration
  p95: number;                // 95th percentile
  p99: number;                // 99th percentile
  min: number;                // Minimum duration
  max: number;                // Maximum duration
}
```

---

## File Organization

These types will be organized in `src/types/`:

```
src/types/
├── index.ts              # Re-export all types
├── user.ts               # User, AuthState, AuthAction
├── application.ts        # Application, ApplicationSecured, ShareWith, ApplicationUsage
├── log.ts                # LogEntry, LogLevel, LogFilterParams, LogsState
├── metric.ts             # MetricEntry, MetricStatus, MetricFilterParams, MetricsState
├── team.ts               # Team, TeamMember, TeamInvitation, TeamRole, InvitationStatus
├── api.ts                # Request/Response types, ApiError, PaginatedResponse
└── common.ts             # Shared types (PaginationParams, etc.)
```

---

## Type Guards

Utility functions to check types at runtime:

```typescript
// src/types/guards.ts

export function isApplicationSecured(
  app: Application | ApplicationSecured
): app is ApplicationSecured {
  return 'secret' in app;
}

export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    'status' in error
  );
}

export function isPaginatedResponse<T>(
  data: unknown
): data is PaginatedResponse<T> {
  return (
    typeof data === 'object' &&
    data !== null &&
    'content' in data &&
    'totalElements' in data &&
    'pageNumber' in data
  );
}
```

---

## Constants

```typescript
// src/types/constants.ts

export const DEFAULT_PAGE_SIZE = 100;
export const MAX_PAGE_SIZE = 1000;
export const DEFAULT_PAGE_NUMBER = 0;

export const LOG_LEVELS: LogLevel[] = [
  LogLevel.TRACE,
  LogLevel.DEBUG,
  LogLevel.INFO,
  LogLevel.WARN,
  LogLevel.ERROR,
  LogLevel.FATAL
];

export const METRIC_STATUSES: MetricStatus[] = [
  MetricStatus.SUCCESS,
  MetricStatus.FAILURE,
  MetricStatus.TIMEOUT,
  MetricStatus.ERROR
];

export const TEAM_ROLES: TeamRole[] = [
  TeamRole.OWNER,
  TeamRole.ADMIN,
  TeamRole.MEMBER
];
```