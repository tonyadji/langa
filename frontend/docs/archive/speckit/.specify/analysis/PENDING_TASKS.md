# Pending Tasks - Blocked by Dependencies

**Last Updated**: January 12, 2026

## 🔒 Blocked Tasks

### TASK-001: Implement Team Invitation Flow (Frontend)
**Status**: 🚫 Blocked  
**Blocker**: Backend endpoint `/api/team-invitations/pending` not implemented  
**Priority**: P2 (Medium)  
**Estimated Effort**: 4 hours  
**Owner**: TBD

#### Background
The frontend has team invitation functionality partially implemented, but the pending invitations endpoint call is currently disabled because the backend doesn't provide the required API.

**Current State**:
- Code exists but is commented out in `src/features/teams/hooks/useTeamInvitations.ts` (line 29)
- Returns empty array with TODO comment
- UI components are ready but receive no data

#### Backend Dependency
**Required Endpoint**: `GET /api/team-invitations/pending`

**Expected Request**:
```http
GET /api/team-invitations/pending
Authorization: Bearer <access_token>
```

**Expected Response** (200 OK):
```json
[
  {
    "id": "inv-123",
    "teamId": "team-456",
    "teamName": "Engineering Team",
    "inviter": "john.doe@example.com",
    "invitee": "jane.smith@example.com",
    "role": "MEMBER",
    "status": "PENDING",
    "expiresAt": "2026-01-19T12:00:00Z",
    "createdAt": "2026-01-12T12:00:00Z"
  }
]
```

**Error Responses**:
- 401 Unauthorized: Invalid or missing token
- 403 Forbidden: User not authenticated
- 500 Internal Server Error: Server error

#### Implementation Checklist

Once backend endpoint is ready:

##### 1. Backend Verification (15 min)
- [ ] Verify endpoint is deployed and accessible
- [ ] Test endpoint manually with Postman/curl
- [ ] Confirm response schema matches expected format
- [ ] Verify error handling (401, 403, 500)

##### 2. Frontend Code Updates (1.5 hours)
- [ ] Uncomment API call in `src/features/teams/hooks/useTeamInvitations.ts`
- [ ] Remove TODO comment
- [ ] Update TypeScript types if response schema differs
- [ ] Verify error handling displays user-friendly messages
- [ ] Test loading states during API call

**File**: `src/features/teams/hooks/useTeamInvitations.ts`
```typescript
// Line 27-29: Replace this
const fetchInvitations = useCallback(async () => {
  try {
    setIsLoading(true);
    setError(null);
    // TODO: Endpoint /team-invitations/pending does not exist in backend
    // const response = await api.get<TeamInvitation[]>('/team-invitations/pending');
    // setInvitations(response.data);
    setInvitations([]);
  } catch (err) {
    setError(err as Error);
    setInvitations([]);
  } finally {
    setIsLoading(false);
  }
}, []);

// With this (once backend is ready):
const fetchInvitations = useCallback(async () => {
  try {
    setIsLoading(true);
    setError(null);
    const response = await api.get<TeamInvitation[]>('/team-invitations/pending');
    setInvitations(response.data);
  } catch (err) {
    setError(err as Error);
    setInvitations([]);
  } finally {
    setIsLoading(false);
  }
}, []);
```

##### 3. UI Component Updates (30 min)
- [ ] Verify TeamInvitationsPage displays pending invitations correctly
- [ ] Test empty state when no invitations
- [ ] Test loading spinner
- [ ] Test error state with mock API failures
- [ ] Verify invitation accept flow works end-to-end

**Files to Review**:
- `src/pages/TeamInvitationsPage.tsx` (or similar)
- `src/features/teams/components/InvitationCard.tsx` (if exists)

##### 4. Testing (1.5 hours)
- [ ] Write unit tests for `useTeamInvitations` hook with mocked API
- [ ] Write integration tests for invitation list display
- [ ] Test error scenarios (network failure, 401, 403, 500)
- [ ] Test empty state (no pending invitations)
- [ ] Test pagination if endpoint supports it
- [ ] Test real-time updates (refresh after accepting invitation)

**Test Files**:
- `src/features/teams/hooks/useTeamInvitations.test.ts`
- Update MSW handlers in test setup

##### 5. Documentation (30 min)
- [ ] Update API documentation in codebase
- [ ] Update user-facing documentation
- [ ] Add comments for any complex logic
- [ ] Update changelog/release notes

##### 6. Deployment & Verification (30 min)
- [ ] Deploy to staging environment
- [ ] Perform manual end-to-end testing
- [ ] Verify with real backend API
- [ ] Test cross-browser compatibility
- [ ] Get QA approval

#### Success Criteria
- ✅ Users can view their pending team invitations
- ✅ Invitation list updates in real-time after acceptance
- ✅ Error states display user-friendly messages
- ✅ Loading states provide clear feedback
- ✅ Empty state guides users when no invitations
- ✅ All tests passing
- ✅ No console errors or warnings

#### Definition of Done
- [ ] Code implemented and tested locally
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] Code reviewed and approved
- [ ] Deployed to staging
- [ ] QA testing complete
- [ ] Deployed to production
- [ ] User documentation updated

#### Related Files
- `src/features/teams/hooks/useTeamInvitations.ts` - Main hook to update
- `src/features/teams/hooks/useTeamInvitations.test.ts` - Test file
- `src/types/team.ts` - Type definitions for TeamInvitation
- `src/pages/TeamInvitationsPage.tsx` - UI component
- `.specify/analysis/IMPLEMENTATION_VS_SPEC_ANALYSIS.md` - Documentation

#### Backend Ticket Reference
**Status**: To Be Created  
**Priority**: P2  
**Team**: Backend Team  
**Epic**: Team Management

**Suggested Backend Ticket**:
```
Title: Implement GET /api/team-invitations/pending endpoint

Description:
Frontend needs this endpoint to display pending team invitations to users.

Acceptance Criteria:
- Endpoint returns list of pending invitations for authenticated user
- Response includes: id, teamId, teamName, inviter, invitee, role, status, expiresAt, createdAt
- Supports pagination (optional)
- Proper error handling (401, 403, 500)
- Filters out expired invitations
- Sorted by createdAt DESC (newest first)

Security:
- Requires authentication (JWT token)
- Users can only see invitations where they are the invitee
- Validate token and user permissions

Frontend Reference:
- Code ready but disabled: src/features/teams/hooks/useTeamInvitations.ts:29
```

#### Alternative Approach (If Backend Won't Implement)
If backend team decides NOT to implement this endpoint:

**Option A**: Remove Feature
- Remove pending invitations UI completely
- Update spec document to remove FR-026
- Keep only accepted invitations display
- Effort: 1 hour

**Option B**: Poll Team Details
- Fetch team details periodically
- Extract invitation info from team member list
- Less efficient but works with existing endpoints
- Effort: 3 hours

---

## 📋 Task Tracking

| Task ID | Title | Status | Priority | Blocked By | Estimated | Owner |
|---------|-------|--------|----------|------------|-----------|-------|
| TASK-001 | Team Invitation Flow | 🚫 Blocked | P2 | Backend API | 4h | TBD |

**Legend**:
- 🚫 Blocked
- ⏳ In Progress
- ✅ Complete
- ❌ Cancelled

---

## 🔔 Notification Plan

**When to Notify**:
1. Backend ticket created → Notify frontend team
2. Backend endpoint deployed to dev → Start frontend implementation
3. Frontend implementation complete → Notify QA team
4. QA approval → Deploy to production

**Stakeholders**:
- Frontend Team Lead
- Backend Team Lead
- QA Lead
- Product Owner

---

**Next Review**: Check backend ticket status weekly
