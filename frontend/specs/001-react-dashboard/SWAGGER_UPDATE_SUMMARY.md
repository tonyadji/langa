# Swagger API Update Summary

**Date**: January 11, 2026  
**Status**: Documentation Updated  
**Impact**: Medium - New features + API response structure changes

---

## Overview

The backend API has evolved with new endpoints, enhanced filtering capabilities, and restructured response formats. All specification documents have been updated to reflect these changes.

---

## Major Changes

### 1. **New Features Added**

#### Retention Policy Management
- **Endpoint**: `PUT /api/applications/{appId}/update-retention-policy`
- **Purpose**: Configure data retention duration for logs and metrics
- **Schema**: `RetentionPolicy` with duration, unit (Days/Weeks/Months/Years/etc.), lastUpdatedDate
- **Impact**: Application owners can now control how long data is stored
- **UI Tasks**: T108c, T108d, T108e

#### Application Deletion
- **Endpoint**: `DELETE /api/applications/{appId}`
- **Purpose**: Permanently delete applications
- **Impact**: Owners can clean up unused applications
- **UI Tasks**: T108a, T108b

#### Logout Functionality
- **Endpoint**: `POST /api/users/logout`
- **Purpose**: Invalidate user tokens on logout
- **Impact**: Enhanced security with server-side token invalidation
- **UI Tasks**: Already implemented in T084

#### First Connection Flow
- **Endpoints**: 
  - `GET /api/first-connection?token=xxx`
  - `POST /api/first-connection/complete`
- **Purpose**: Handle users invited via email who need to set passwords
- **Impact**: Enables team collaboration with email invitations
- **UI Tasks**: New feature - not yet in tasks.md (future enhancement)

---

### 2. **Enhanced Filtering**

#### Metrics Filtering - Significantly Enhanced
**Old filters**: name, status, startTime, endTime  
**New filters**: name, status, **uri**, **httpMethod**, **httpStatus**, **durationLessThan**, **durationGreaterThan**, **keyword**, startDate, endDate

**Impact**: 
- Users can now filter metrics by HTTP-specific criteria
- Duration range filters enable performance analysis
- Keyword search across metric data
- **UI Task**: T146a to implement enhanced filters

#### Logs Filtering - Simplified
**Old filters**: level, **loggerName**, startTime, endTime  
**New filters**: level, **keyword**, startDate, endDate

**Change**: `loggerName` removed in favor of generic `keyword` search

**Impact**:
- More flexible search (keyword searches across all text fields)
- Simpler UI (one search box instead of multiple fields)
- **Already implemented** in T127 with keyword support

---

### 3. **Response Structure Changes**

#### Logs/Metrics API Responses Now Wrapped
**Old**: Direct `PaginatedResponse<LogEntry>` or `PaginatedResponse<MetricEntry>`  
**New**: Wrapped in `ApplicationLogsResponse` or `ApplicationMetricsResponse`

```typescript
// Before
{
  content: LogEntry[],
  totalElements: number,
  page: number,
  size: number
}

// After
{
  appName: string,
  paginatedLogs: {
    content: LogEntry[],
    totalElements: number,
    page: number,
    size: number
  }
}
```

**Impact**: 
- App name now included in response
- Need to unwrap `paginatedLogs`/`paginatedMetrics` to access data
- **Action Required**: Update existing log/metric query hooks

---

### 4. **Data Model Enhancements**

#### ApplicationUsage - Added Trends
**Before**:
```typescript
{
  id: string;
  appKey: string;
  totalLogBytes: number;
  totalMetricBytes: number;
}
```

**After**:
```typescript
{
  id: string;
  key: string;
  name: string;
  logUsage: number;          // Renamed from totalLogBytes
  metricUsage: number;        // Renamed from totalMetricBytes
  trends: ApplicationUsageTrend[];  // NEW - Historical data
}
```

**New**: `ApplicationUsageTrend` schema for time-series visualization
```typescript
{
  name: string;
  key: string;
  usage: number;
  type: 'LOG' | 'METRIC';
  createdDate: string;
}
```

**Impact**: 
- Can now visualize usage growth over time
- **UI Task**: T213a to add trend charts

#### SecuredApplicationDto - Enhanced Fields
**Added**:
- `retentionPolicy: RetentionPolicy` - Data retention configuration
- `http: string` - HTTP ingestion endpoint
- `kafka: string` - Kafka ingestion endpoint
- `usage: ApplicationUsageDto` - Embedded usage data

#### ShareWith - Status Tracking
**Added**:
- `currentlyActive: boolean` - Computed active status
- `revoked: boolean` - Whether share was revoked
- `expired: boolean` - Whether share passed expiration

**Impact**: Better UX showing share lifecycle

#### TeamInvitation - Restructured
**Old**: Flat structure with simple fields  
**New**: Nested structure with:
- `identity: { teamId, invitationToken }`
- `stakeHolders: { team, host, guest }`
- `invitationPeriod: { inviteDate, expiryDate }`

**Impact**: More structured, matches backend domain model

---

### 5. **Query Parameter Changes**

#### Logs/Metrics Queries Now Use `filterDto`
**Old**: Flat query params  
**New**: Nested `filterDto` object + page/size params

```typescript
// Before
GET /api/applications/{id}/logs?level=ERROR&page=0&size=20

// After
GET /api/applications/{id}/logs?filterDto={level:'ERROR'}&page=0&size=20
```

**Impact**: May require API client adjustments

---

## Updated Documents

1. ✅ **data-model.md**
   - Added `RetentionPolicy`, `ApplicationUsageTrend`, `UsageTrendType`
   - Updated `ApplicationSecured`, `ApplicationUsage`, `ShareWith`, `TeamInvitation`
   - Enhanced `LogFilterParams` (keyword), `MetricFilterParams` (7 new filters)
   - Added request/response types for new endpoints

2. ✅ **contracts/api-client.ts**
   - Added `authApi.logout()`
   - Added `applicationsApi.delete()`, `applicationsApi.updateRetentionPolicy()`
   - Added `firstConnectionApi.getUserInfo()`, `firstConnectionApi.complete()`
   - Updated `logsApi.query()` and `metricsApi.query()` return types
   - Updated `teamInvitationsApi` with new endpoints and responses

3. ✅ **spec.md**
   - Updated FR-011 (log filtering with keyword)
   - Updated FR-016 (enhanced metric filtering)
   - Added FR-036 to FR-040 (retention policy, deletion, logout, first connection)
   - Updated Key Entities section

4. ✅ **tasks.md**
   - Updated T020-T023 type definitions
   - Updated T084 (logout with API call)
   - Added T108a-T108e (deletion + retention policy)
   - Updated T127 (keyword search clarification)
   - Added T146a (enhanced metric filters)
   - Added T213a (usage trends visualization)

5. ✅ **plan.md**
   - Resolved open questions section (already up to date)

---

## Action Items

### High Priority (Breaking Changes)
1. **Update Logs/Metrics API Consumers**: Unwrap `paginatedLogs`/`paginatedMetrics` from response
2. **Update Log Filters**: Replace `loggerName` with `keyword` in UI components
3. **Update ApplicationUsage References**: Rename `totalLogBytes`→`logUsage`, `totalMetricBytes`→`metricUsage`

### Medium Priority (New Features)
4. **Implement Retention Policy UI**: Tasks T108c, T108d, T108e
5. **Implement Delete Application**: Tasks T108a, T108b
6. **Enhance Metric Filters**: Task T146a (URI, HTTP, duration filters)
7. **Add Usage Trend Charts**: Task T213a

### Low Priority (Enhancements)
8. **First Connection Flow**: Not yet in tasks.md - future enhancement
9. **Team Invitation Restructuring**: Update existing code to use new nested structure

---

## Risk Assessment

**Low Risk**:
- Most changes are additive (new endpoints, new fields)
- Existing functionality preserved

**Medium Risk**:
- Response structure changes (logs/metrics wrapping) - **requires code updates**
- Filter param changes (loggerName → keyword) - **may affect existing filters**
- ApplicationUsage field renames - **requires update to usage components**

**Mitigation**:
- All type definitions updated - TypeScript will catch issues
- Run full test suite after updating API consumers
- Update UI components incrementally

---

## Next Steps

1. **Review** this summary with the team
2. **Prioritize** action items based on user needs
3. **Update** API consumer code (hooks, components)
4. **Test** thoroughly with new backend
5. **Implement** new features (retention policy, deletion, enhanced filters)
