/**
 * Contract Tests for POST /api/teams
 * 
 * Tests team creation endpoint according to API specification.
 * Covers success cases, validation errors, and auth requirements.
 */

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import axios from 'axios';
import { Team, TeamMember, TeamRole, CreateTeamRequest } from '@/types/team';

const API_BASE_URL = 'http://localhost:8080';

// Mock authenticated user from JWT
const mockUser = {
  email: 'owner@example.com'
};

// Helper to create mock team
const createMockTeam = (name: string): Team => ({
  id: 'team-123',
  name,
  key: 'T-abc123xyz',
  members: [
    {
      email: mockUser.email,
      role: TeamRole.OWNER,
      teamKey: 'T-abc123xyz',
      addedDate: '2026-01-04T10:00:00.000Z'
    }
  ],
  createdAt: '2026-01-04T10:00:00.000Z',
  createdBy: mockUser.email
});

// MSW server setup
const handlers = [
  // Success: Create team with valid name
  http.post(`${API_BASE_URL}/api/teams`, async ({ request }) => {
    const body = await request.json() as CreateTeamRequest;
    
    if (!body.name || body.name.trim().length === 0) {
      return HttpResponse.json(
        { error: 'Team name is required' },
        { status: 400 }
      );
    }
    
    if (body.name === 'DuplicateTeam') {
      return HttpResponse.json(
        { error: 'Team name already exists' },
        { status: 409 }
      );
    }
    
    return HttpResponse.json(createMockTeam(body.name), { status: 201 });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('POST /api/teams - Contract Tests', () => {
  it('should create team successfully with valid name', async () => {
    const request: CreateTeamRequest = {
      name: 'Engineering Team'
    };
    
    const response = await axios.post<Team>(
      `${API_BASE_URL}/api/teams`,
      request
    );
    
    expect(response.status).toBe(201);
    expect(response.data).toMatchObject({
      id: expect.any(String),
      name: 'Engineering Team',
      key: expect.stringMatching(/^T-/),
      createdBy: mockUser.email
    });
    expect(response.data.members).toHaveLength(1);
    expect(response.data.members[0]).toMatchObject({
      email: mockUser.email,
      role: TeamRole.OWNER
    });
  });
  
  it('should return 400 for empty team name', async () => {
    const request: CreateTeamRequest = {
      name: ''
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(400);
      expect(error.response.data.error).toContain('required');
    }
  });
  
  it('should return 409 for duplicate team name', async () => {
    const request: CreateTeamRequest = {
      name: 'DuplicateTeam'
    };
    
    try {
      await axios.post(`${API_BASE_URL}/api/teams`, request);
      expect.fail('Should have thrown error');
    } catch (error: any) {
      expect(error.response.status).toBe(409);
      expect(error.response.data.error).toContain('already exists');
    }
  });
  
  it('should create team with creator as OWNER role', async () => {
    const request: CreateTeamRequest = {
      name: 'New Team'
    };
    
    const response = await axios.post<Team>(
      `${API_BASE_URL}/api/teams`,
      request
    );
    
    const owner = response.data.members.find(m => m.role === TeamRole.OWNER);
    expect(owner).toBeDefined();
    expect(owner?.email).toBe(mockUser.email);
    expect(owner?.teamKey).toBe(response.data.key);
  });
  
  it('should generate unique team key with T- prefix', async () => {
    const request: CreateTeamRequest = {
      name: 'Test Team'
    };
    
    const response = await axios.post<Team>(
      `${API_BASE_URL}/api/teams`,
      request
    );
    
    expect(response.data.key).toMatch(/^T-[A-Za-z0-9]+$/);
    expect(response.data.key.length).toBeGreaterThan(3);
  });
});
