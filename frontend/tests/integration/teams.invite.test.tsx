/**
 * Contract Tests for POST /api/teams/invite
 * 
 * Tests team invitation endpoint according to API specification.
 * Covers success cases, validation errors, and permission checks.
 */

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import axios from 'axios';
import { TeamInvitation, TeamRole, InvitationStatus, InviteMemberRequest } from '@/types/team';

const API_BASE_URL = 'http://localhost:8080';

const mockUser = {
  email: 'admin@example.com'
};

// Helper to create mock invitation
const createMockInvitation = (teamKey: string, guestEmail: string, role: TeamRole): TeamInvitation => ({
  id: 'inv-456',
  teamKey,
  guestEmail,
  hostEmail: mockUser.email,
  role,
  status: InvitationStatus.PENDING,
  expirationDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
  sentDate: new Date().toISOString()
});

// MSW server setup
const handlers = [
  http.post(`${API_BASE_URL}/api/teams/invite`, async ({ request }) => {
    const body = await request.json() as InviteMemberRequest;
    
    // Validation
    if (!body.teamKey || !body.guestEmail || !body.role) {
      return HttpResponse.json(
        { error: 'Missing required fields: teamKey, guestEmail, role' },
        { status: 400 }
      );
    }
    
    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(body.guestEmail)) {
      return HttpResponse.json(
        { error: 'Invalid email format' },
        { status: 400 }
      );
    }
    
    // Role validation
    if (body.role === TeamRole.OWNER) {
      return HttpResponse.json(
        { error: 'Cannot invite as OWNER role' },
        { status: 400 }
      );
    }
    
    // Team not found
    if (body.teamKey === 'T-notfound') {
      return HttpResponse.json(
        { error: 'Team not found' },
        { status: 404 }
      );
    }
    
    // Not authorized (not owner/admin)
    if (body.teamKey === 'T-unauthorized') {
      return HttpResponse.json(
        { error: 'Only team owners and admins can invite members' },
        { status: 403 }
      );
    }
    
    // Self-invitation
    if (body.guestEmail === mockUser.email) {
      return HttpResponse.json(
        { error: 'Cannot invite yourself' },
        { status: 400 }
      );
    }
    
    // Already a member
    if (body.guestEmail === 'existing@example.com') {
      return HttpResponse.json(
        { error: 'User is already a team member' },
        { status: 409 }
      );
    }
    
    return HttpResponse.json(
      createMockInvitation(body.teamKey, body.guestEmail, body.role),
      { status: 201 }
    );
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('POST /api/teams/invite - Contract Tests', () => {
  it('should send invitation successfully with valid data', async () => {
    const request: InviteMemberRequest = {
      teamKey: 'T-abc123',
      guestEmail: 'newmember@example.com',
      role: TeamRole.MEMBER
    };
    
    const response = await axios.post<TeamInvitation>(
      `${API_BASE_URL}/api/teams/invite`,
      request
    );
    
    expect(response.status).toBe(201);
    expect(response.data).toMatchObject({
      id: expect.any(String),
      teamKey: 'T-abc123',
      guestEmail: 'newmember@example.com',
      hostEmail: mockUser.email,
      role: TeamRole.MEMBER,
      status: InvitationStatus.PENDING
    });
    expect(response.data.expirationDate).toBeDefined();
    expect(response.data.sentDate).toBeDefined();
  });
  
  it('should allow ADMIN role invitation', async () => {
    const request: InviteMemberRequest = {
      teamKey: 'T-abc123',
      guestEmail: 'newadmin@example.com',
      role: TeamRole.ADMIN
    };
    
    const response = await axios.post<TeamInvitation>(
      `${API_BASE_URL}/api/teams/invite`,
      request
    );
    
    expect(response.status).toBe(201);
    expect(response.data.role).toBe(TeamRole.ADMIN);
  });
  
  it('should return 400 for missing fields', async () => {
    const request = {
      teamKey: 'T-abc123'
      // Missing guestEmail and role
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams/invite`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(400);
      expect(error.response.data.error).toContain('required');
    }
  });
  
  it('should return 400 for invalid email format', async () => {
    const request: InviteMemberRequest = {
      teamKey: 'T-abc123',
      guestEmail: 'invalid-email',
      role: TeamRole.MEMBER
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams/invite`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(400);
      expect(error.response.data.error).toContain('Invalid email');
    }
  });
  
  it('should return 400 when trying to invite as OWNER', async () => {
    const request: InviteMemberRequest = {
      teamKey: 'T-abc123',
      guestEmail: 'newowner@example.com',
      role: TeamRole.OWNER
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams/invite`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(400);
      expect(error.response.data.error).toContain('Cannot invite as OWNER');
    }
  });
  
  it('should return 404 for non-existent team', async () => {
    const request: InviteMemberRequest = {
      teamKey: 'T-notfound',
      guestEmail: 'user@example.com',
      role: TeamRole.MEMBER
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams/invite`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(404);
      expect(error.response.data.error).toContain('not found');
    }
  });
  
  it('should return 403 when user is not owner or admin', async () => {
    const request: InviteMemberRequest = {
      teamKey: 'T-unauthorized',
      guestEmail: 'user@example.com',
      role: TeamRole.MEMBER
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams/invite`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(403);
      expect(error.response.data.error).toContain('owners and admins');
    }
  });
  
  it('should return 400 for self-invitation', async () => {
    const request: InviteMemberRequest = {
      teamKey: 'T-abc123',
      guestEmail: mockUser.email,
      role: TeamRole.MEMBER
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams/invite`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(400);
      expect(error.response.data.error).toContain('Cannot invite yourself');
    }
  });
  
  it('should return 409 when user is already a member', async () => {
    const request: InviteMemberRequest = {
      teamKey: 'T-abc123',
      guestEmail: 'existing@example.com',
      role: TeamRole.MEMBER
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams/invite`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(409);
      expect(error.response.data.error).toContain('already a team member');
    }
  });
});
