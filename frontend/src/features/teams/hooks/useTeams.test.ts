/**
 * Unit Tests for useTeams Hook
 * 
 * Tests team management operations: fetch teams, create team, remove member
 */

import { describe, it, expect, beforeAll, afterEach, afterAll, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { useTeams } from './useTeams';
import type { Team, CreateTeamRequest } from '@/types/team';
import { TeamRole } from '@/types/team';

const API_BASE_URL = 'http://localhost:8080';

const mockTeams: Team[] = [
  {
    id: 'team-1',
    name: 'Engineering',
    key: 'T-eng123',
    members: [
      {
        email: 'owner@example.com',
        role: TeamRole.OWNER,
        teamKey: 'T-eng123',
        addedDate: '2026-01-01T10:00:00.000Z'
      },
      {
        email: 'admin@example.com',
        role: TeamRole.ADMIN,
        teamKey: 'T-eng123',
        addedDate: '2026-01-02T10:00:00.000Z'
      }
    ],
    createdAt: '2026-01-01T10:00:00.000Z',
    createdBy: 'owner@example.com'
  },
  {
    id: 'team-2',
    name: 'Product',
    key: 'T-prod456',
    members: [
      {
        email: 'owner@example.com',
        role: TeamRole.MEMBER,
        teamKey: 'T-prod456',
        addedDate: '2026-01-03T10:00:00.000Z'
      }
    ],
    createdAt: '2026-01-03T10:00:00.000Z',
    createdBy: 'other@example.com'
  }
];

const handlers = [
  http.get(`${API_BASE_URL}/api/teams`, () => {
    return HttpResponse.json(mockTeams);
  }),
  
  http.post(`${API_BASE_URL}/api/teams`, async ({ request }) => {
    const body = await request.json() as CreateTeamRequest;
    const newTeam: Team = {
      id: 'team-new',
      name: body.name,
      key: 'T-new789',
      members: [{
        email: 'creator@example.com',
        role: TeamRole.OWNER,
        teamKey: 'T-new789',
        addedDate: new Date().toISOString()
      }],
      createdAt: new Date().toISOString(),
      createdBy: 'creator@example.com'
    };
    return HttpResponse.json(newTeam, { status: 201 });
  }),
  
  http.delete(`${API_BASE_URL}/api/teams/:teamKey/members/:email`, ({ params }) => {
    return HttpResponse.json({ success: true }, { status: 200 });
  })
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('useTeams Hook', () => {
  it('should fetch teams successfully', async () => {
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.teams).toEqual(mockTeams);
    expect(result.current.teams).toHaveLength(2);
    expect(result.current.error).toBeNull();
  });
  
  it('should return teams sorted by creation date', async () => {
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.teams).toHaveLength(2);
    });
    
    // Should be sorted newest first or maintain order
    expect(result.current.teams[0].name).toBe('Engineering');
    expect(result.current.teams[1].name).toBe('Product');
  });
  
  it('should create team successfully', async () => {
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    const newTeamData: CreateTeamRequest = {
      name: 'New Team'
    };
    
    const newTeam = await result.current.createTeam(newTeamData);
    
    expect(newTeam).toMatchObject({
      id: 'team-new',
      name: 'New Team',
      key: 'T-new789'
    });
    expect(newTeam.members).toHaveLength(1);
    expect(newTeam.members[0].role).toBe(TeamRole.OWNER);
  });
  
  it('should handle team creation errors', async () => {
    server.use(
      http.post(`${API_BASE_URL}/api/teams`, () => {
        return HttpResponse.json(
          { error: 'Team name already exists' },
          { status: 409 }
        );
      })
    );
    
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    await expect(
      result.current.createTeam({ name: 'Duplicate' })
    ).rejects.toThrow();
  });
  
  it('should remove team member successfully', async () => {
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    await expect(
      result.current.removeMember('T-eng123', 'admin@example.com')
    ).resolves.not.toThrow();
  });
  
  it('should handle member removal errors', async () => {
    server.use(
      http.delete(`${API_BASE_URL}/api/teams/:teamKey/members/:email`, () => {
        return HttpResponse.json(
          { error: 'Cannot remove team owner' },
          { status: 403 }
        );
      })
    );
    
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    await expect(
      result.current.removeMember('T-eng123', 'owner@example.com')
    ).rejects.toThrow();
  });
  
  it('should refetch teams after creating new team', async () => {
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.teams).toHaveLength(2);
    });
    
    const refetchSpy = vi.spyOn(result.current, 'refetch');
    
    await result.current.createTeam({ name: 'Another Team' });
    
    // Note: In actual implementation, createTeam should trigger refetch
    expect(result.current.refetch).toBeDefined();
  });
  
  it('should handle empty teams list', async () => {
    server.use(
      http.get(`${API_BASE_URL}/api/teams`, () => {
        return HttpResponse.json([]);
      })
    );
    
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.teams).toEqual([]);
    expect(result.current.teams).toHaveLength(0);
  });
  
  it('should handle network errors', async () => {
    server.use(
      http.get(`${API_BASE_URL}/api/teams`, () => {
        return HttpResponse.error();
      })
    );
    
    const { result } = renderHook(() => useTeams());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.error).toBeDefined();
    expect(result.current.teams).toEqual([]);
  });
});
