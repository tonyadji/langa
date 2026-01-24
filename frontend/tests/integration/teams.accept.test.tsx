/**
 * Contract Tests for POST /api/team-invitations/accept
 * 
 * Tests invitation acceptance endpoint according to API specification.
 * Covers success cases, validation errors, and business rules.
 */

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import axios from 'axios';
import { TeamMember, TeamRole, AcceptInvitationRequest } from '@/types/team';

const API_BASE_URL = 'http://localhost:8080';

const mockUser = {
  email: 'newmember@example.com'
};

// Helper to create mock team member after accepting invitation
const createMockTeamMember = (teamKey: string, role: TeamRole): TeamMember => ({
  email: mockUser.email,
  role,
  teamKey,
  addedDate: new Date().toISOString()
});

// MSW server setup
const handlers = [
  http.post(`${API_BASE_URL}/api/team-invitations/accept`, async ({ request }) => {
    const body = await request.json() as AcceptInvitationRequest;
    
    // Validation
    if (!body.invitationId) {
      return HttpResponse.json(
        { error: 'Invitation ID is required' },
        { status: 400 }
      );
    }
    
    // Invitation not found
    if (body.invitationId === 'inv-notfound') {
      return HttpResponse.json(
        { error: 'Invitation not found' },
        { status: 404 }
      );
    }
    
    // Already accepted
    if (body.invitationId === 'inv-accepted') {
      return HttpResponse.json(
        { error: 'Invitation already accepted' },
        { status: 409 }
      );
    }
    
    // Expired invitation
    if (body.invitationId === 'inv-expired') {
      return HttpResponse.json(
        { error: 'Invitation has expired' },
        { status: 410 }
      );
    }
    
    // Not the invited user
    if (body.invitationId === 'inv-unauthorized') {
      return HttpResponse.json(
        { error: 'This invitation is not for you' },
        { status: 403 }
      );
    }
    
    // Success - return team member with MEMBER role by default
    const role = body.invitationId === 'inv-admin' ? TeamRole.ADMIN : TeamRole.MEMBER;
    
    return HttpResponse.json(
      createMockTeamMember('T-abc123', role),
      { status: 200 }
    );
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('POST /api/team-invitations/accept - Contract Tests', () => {
  it('should accept invitation successfully', async () => {
    const request: AcceptInvitationRequest = {
      invitationId: 'inv-valid123'
    };
    
    const response = await axios.post<TeamMember>(
      `${API_BASE_URL}/api/team-invitations/accept`,
      request
    );
    
    expect(response.status).toBe(200);
    expect(response.data).toMatchObject({
      email: mockUser.email,
      role: TeamRole.MEMBER,
      teamKey: expect.stringMatching(/^T-/),
      addedDate: expect.any(String)
    });
  });
  
  it('should accept invitation with ADMIN role when invited as admin', async () => {
    const request: AcceptInvitationRequest = {
      invitationId: 'inv-admin'
    };
    
    const response = await axios.post<TeamMember>(
      `${API_BASE_URL}/api/team-invitations/accept`,
      request
    );
    
    expect(response.status).toBe(200);
    expect(response.data.role).toBe(TeamRole.ADMIN);
  });
  
  it('should return 400 for missing invitation ID', async () => {
    const request = {};
    
    try {
      await axios.post(`${API_BASE_URL}/api/team-invitations/accept`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(400);
      expect(error.response.data.error).toContain('required');
    }
  });
  
  it('should return 404 for non-existent invitation', async () => {
    const request: AcceptInvitationRequest = {
      invitationId: 'inv-notfound'
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/team-invitations/accept`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(404);
      expect(error.response.data.error).toContain('not found');
    }
  });
  
  it('should return 409 for already accepted invitation', async () => {
    const request: AcceptInvitationRequest = {
      invitationId: 'inv-accepted'
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/team-invitations/accept`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(409);
      expect(error.response.data.error).toContain('already accepted');
    }
  });
  
  it('should return 410 for expired invitation', async () => {
    const request: AcceptInvitationRequest = {
      invitationId: 'inv-expired'
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/team-invitations/accept`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(410);
      expect(error.response.data.error).toContain('expired');
    }
  });
  
  it('should return 403 when accepting invitation not for current user', async () => {
    const request: AcceptInvitationRequest = {
      invitationId: 'inv-unauthorized'
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/team-invitations/accept`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(403);
      expect(error.response.data.error).toContain('not for you');
    }
  });
  
  it('should return team member with correct addedDate timestamp', async () => {
    const request: AcceptInvitationRequest = {
      invitationId: 'inv-valid123'
    };
    
    const response = await axios.post<TeamMember>(
      `${API_BASE_URL}/api/team-invitations/accept`,
      request
    );
    
    const addedDate = new Date(response.data.addedDate);
    expect(addedDate).toBeInstanceOf(Date);
    expect(addedDate.getTime()).toBeLessThanOrEqual(Date.now());
  });
});
