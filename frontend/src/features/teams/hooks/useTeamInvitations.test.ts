/**
 * Unit Test: useTeamInvitations Hook
 *
 * - pending invitations: the backend has no endpoint listing them yet, the list stays empty
 * - inviteMember: POST /teams/invite, returns the updated team
 * - acceptInvitation: POST /team-invitations/{teamId}/accept?invitationToken=..., without body
 *   (the backend takes the guest from the access token)
 */

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { useTeamInvitations } from './useTeamInvitations';
import { InvitationStatus } from '@/types/team';

const API_BASE_URL = 'http://localhost:8080/api';

const acceptedInvitation = {
  id: 'team-1',
  token: 'token-1',
  team: 'Dev Team',
  host: 'owner@example.com',
  guest: 'guest@example.com',
  expiryDate: '2030-01-01T00:00:00',
  status: InvitationStatus.ACCEPTED,
};

let acceptRequest: { url: URL; body: string } | null = null;

const server = setupServer(
  http.post(`${API_BASE_URL}/teams/invite`, async ({ request }) => {
    const body = (await request.json()) as { guest: string; team: string };
    return HttpResponse.json(
      { id: 'team-1', name: 'Dev Team', owner: 'owner@example.com', invitations: [{ guest: body.guest }] },
      { status: 201 }
    );
  }),
  http.post(`${API_BASE_URL}/team-invitations/:teamId/accept`, async ({ request }) => {
    acceptRequest = { url: new URL(request.url), body: await request.text() };
    return HttpResponse.json(acceptedInvitation);
  })
);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
  server.resetHandlers();
  acceptRequest = null;
});
afterAll(() => server.close());

describe('useTeamInvitations Hook', () => {
  it('should expose no pending invitation (no backend endpoint yet)', async () => {
    const { result } = renderHook(() => useTeamInvitations());

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.invitations).toEqual([]);
    expect(result.current.error).toBeNull();
  });

  it('should invite a member and return the updated team', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    const team = await result.current.inviteMember({ guest: 'guest@example.com', team: 'team-key' });

    expect(team).toMatchObject({ id: 'team-1', name: 'Dev Team' });
  });

  it('should propagate invite errors', async () => {
    server.use(
      http.post(`${API_BASE_URL}/teams/invite`, () =>
        HttpResponse.json({ code: '400-203', message: 'Already a member' }, { status: 400 })
      )
    );
    const { result } = renderHook(() => useTeamInvitations());
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await expect(result.current.inviteMember({ guest: 'guest@example.com', team: 'team-key' })).rejects.toThrow();
  });

  it('should accept an invitation for the signed-in user, without sending the guest', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    const invitation = await result.current.acceptInvitation('team-1', 'token-1');

    expect(invitation).toEqual(acceptedInvitation);
    expect(acceptRequest?.url.pathname).toBe('/api/team-invitations/team-1/accept');
    expect(acceptRequest?.url.searchParams.get('invitationToken')).toBe('token-1');
    expect(acceptRequest?.body).toBe('');
  });

  it('should propagate accept errors', async () => {
    server.use(
      http.post(`${API_BASE_URL}/team-invitations/:teamId/accept`, () =>
        HttpResponse.json({ code: '400-204', message: 'Invitation not found or expired' }, { status: 400 })
      )
    );
    const { result } = renderHook(() => useTeamInvitations());
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await expect(result.current.acceptInvitation('team-1', 'expired')).rejects.toThrow();
  });

  it('should expose a refetch function', async () => {
    const { result } = renderHook(() => useTeamInvitations());
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await result.current.refetch();

    expect(result.current.invitations).toEqual([]);
  });
});
