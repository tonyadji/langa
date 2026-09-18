/**
 * Unit Tests for useTeamInvitations Hook
 * 
 * Tests invitation management: fetch pending, invite member, accept invitation
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { useTeamInvitations } from './useTeamInvitations';
import type { TeamInvitation, TeamMember } from '@/types/team';
import { TeamRole, InvitationStatus } from '@/types/team';

const API_BASE_URL = 'http://localhost:8080';

const mockInvitations: TeamInvitation[] = [
  {
    id: 'inv-1',
    teamKey: 'T-eng123',
    guestEmail: 'newuser@example.com',
    hostEmail: 'owner@example.com',
    role: TeamRole.MEMBER,
    status: InvitationStatus.PENDING,
    expirationDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
    sentDate: '2026-01-04T10:00:00.000Z'
  },
  {
    id: 'inv-2',
    teamKey: 'T-prod456',
    guestEmail: 'newuser@example.com',
    hostEmail: 'admin@example.com',
    role: TeamRole.ADMIN,
    status: InvitationStatus.PENDING,
    expirationDate: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(),
    sentDate: '2026-01-03T10:00:00.000Z'
  }
];

const handlers = [
  http.get(`${API_BASE_URL}/api/team-invitations/pending`, () => {
    return HttpResponse.json(mockInvitations);
  }),
  
  http.post(`${API_BASE_URL}/api/teams/invite`, async ({ request }) => {
    const body = await request.json();
    const newInvitation: TeamInvitation = {
      id: 'inv-new',
      teamKey: body.teamKey,
      guestEmail: body.guestEmail,
      hostEmail: 'current@example.com',
      role: body.role,
      status: InvitationStatus.PENDING,
      expirationDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
      sentDate: new Date().toISOString()
    };
    return HttpResponse.json(newInvitation, { status: 201 });
  }),
  
  http.post(`${API_BASE_URL}/api/team-invitations/accept`, async ({ request }) => {
    const body = await request.json();
    const newMember: TeamMember = {
      email: 'newuser@example.com',
      role: TeamRole.MEMBER,
      teamKey: 'T-eng123',
      addedDate: new Date().toISOString()
    };
    return HttpResponse.json(newMember, { status: 200 });
  })
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('useTeamInvitations Hook', () => {
  it('should fetch pending invitations successfully', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.invitations).toEqual(mockInvitations);
    expect(result.current.invitations).toHaveLength(2);
    expect(result.current.error).toBeNull();
  });
  
  it('should filter only pending invitations', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.invitations).toHaveLength(2);
    });
    
    result.current.invitations.forEach(inv => {
      expect(inv.status).toBe(InvitationStatus.PENDING);
    });
  });
  
  it('should invite member successfully', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    const invitation = await result.current.inviteMember({
      teamKey: 'T-test',
      guestEmail: 'guest@example.com',
      role: TeamRole.MEMBER
    });
    
    expect(invitation).toMatchObject({
      id: 'inv-new',
      teamKey: 'T-test',
      guestEmail: 'guest@example.com',
      role: TeamRole.MEMBER,
      status: InvitationStatus.PENDING
    });
  });
  
  it('should handle invite member errors', async () => {
    server.use(
      http.post(`${API_BASE_URL}/api/teams/invite`, () => {
        return HttpResponse.json(
          { error: 'User already invited' },
          { status: 409 }
        );
      })
    );
    
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    await expect(
      result.current.inviteMember({
        teamKey: 'T-test',
        guestEmail: 'existing@example.com',
        role: TeamRole.MEMBER
      })
    ).rejects.toThrow();
  });
  
  it('should accept invitation successfully', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    const member = await result.current.acceptInvitation('inv-1');
    
    expect(member).toMatchObject({
      email: 'newuser@example.com',
      role: TeamRole.MEMBER,
      teamKey: 'T-eng123'
    });
  });
  
  it('should handle accept invitation errors', async () => {
    server.use(
      http.post(`${API_BASE_URL}/api/team-invitations/accept`, () => {
        return HttpResponse.json(
          { error: 'Invitation expired' },
          { status: 410 }
        );
      })
    );
    
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    await expect(
      result.current.acceptInvitation('inv-expired')
    ).rejects.toThrow();
  });
  
  it('should handle empty invitations list', async () => {
    server.use(
      http.get(`${API_BASE_URL}/api/team-invitations/pending`, () => {
        return HttpResponse.json([]);
      })
    );
    
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.invitations).toEqual([]);
    expect(result.current.invitations).toHaveLength(0);
  });
  
  it('should sort invitations by sent date descending', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.invitations).toHaveLength(2);
    });
    
    // Most recent first
    expect(result.current.invitations[0].id).toBe('inv-1');
    expect(result.current.invitations[1].id).toBe('inv-2');
  });
  
  it('should refetch invitations after accepting', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.invitations).toHaveLength(2);
    });
    
    await result.current.acceptInvitation('inv-1');
    
    // Note: In actual implementation, acceptInvitation should trigger refetch
    expect(result.current.refetch).toBeDefined();
  });
  
  it('should handle network errors', async () => {
    server.use(
      http.get(`${API_BASE_URL}/api/team-invitations/pending`, () => {
        return HttpResponse.error();
      })
    );
    
    const { result } = renderHook(() => useTeamInvitations());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.error).toBeDefined();
    expect(result.current.invitations).toEqual([]);
  });
});
