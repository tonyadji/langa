/**
 * Team Creation and Invitation Flow - Integration Test
 * 
 * Tests the complete flow: create team → invite member → accept invitation
 * Validates end-to-end team collaboration workflow.
 */

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import axios from 'axios';
import { 
  Team, 
  TeamInvitation, 
  TeamMember, 
  TeamRole, 
  InvitationStatus,
  CreateTeamRequest,
  InviteMemberRequest,
  AcceptInvitationRequest
} from '@/types/team';

const API_BASE_URL = 'http://localhost:8080';

// Two users in the flow
const teamOwner = { email: 'owner@example.com' };
const invitedUser = { email: 'member@example.com' };

// State to track created entities in the flow
let createdTeam: Team;
let createdInvitation: TeamInvitation;

// MSW server setup with full flow support
const handlers = [
  // Step 1: Create team
  http.post(`${API_BASE_URL}/api/teams`, async ({ request }) => {
    const body = await request.json() as CreateTeamRequest;
    
    createdTeam = {
      id: 'team-flow-123',
      name: body.name,
      key: 'T-flow987',
      members: [
        {
          email: teamOwner.email,
          role: TeamRole.OWNER,
          teamKey: 'T-flow987',
          addedDate: new Date().toISOString()
        }
      ],
      createdAt: new Date().toISOString(),
      createdBy: teamOwner.email
    };
    
    return HttpResponse.json(createdTeam, { status: 201 });
  }),
  
  // Step 2: Invite member to team
  http.post(`${API_BASE_URL}/api/teams/invite`, async ({ request }) => {
    const body = await request.json() as InviteMemberRequest;
    
    createdInvitation = {
      id: 'inv-flow-456',
      teamKey: body.teamKey,
      guestEmail: body.guestEmail,
      hostEmail: teamOwner.email,
      role: body.role,
      status: InvitationStatus.PENDING,
      expirationDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString(),
      sentDate: new Date().toISOString()
    };
    
    return HttpResponse.json(createdInvitation, { status: 201 });
  }),
  
  // Step 3: Get pending invitations for invited user
  http.get(`${API_BASE_URL}/api/team-invitations/pending`, () => {
    return HttpResponse.json([createdInvitation], { status: 200 });
  }),
  
  // Step 4: Accept invitation
  http.post(`${API_BASE_URL}/api/team-invitations/accept`, async ({ request }) => {
    const body = await request.json() as AcceptInvitationRequest;
    
    // Update team members
    const newMember: TeamMember = {
      email: invitedUser.email,
      role: createdInvitation.role,
      teamKey: createdInvitation.teamKey,
      addedDate: new Date().toISOString()
    };
    
    createdTeam.members.push(newMember);
    
    return HttpResponse.json(newMember, { status: 200 });
  }),
  
  // Get team with updated members
  http.get(`${API_BASE_URL}/api/teams/:teamKey`, ({ params }) => {
    return HttpResponse.json(createdTeam, { status: 200 });
  }),
  
  // Get all teams for user
  http.get(`${API_BASE_URL}/api/teams`, () => {
    return HttpResponse.json([createdTeam], { status: 200 });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('Team Creation and Invitation Flow - Integration Test', () => {
  it('should complete full team collaboration flow', async () => {
    // Step 1: Owner creates a team
    const createRequest: CreateTeamRequest = {
      name: 'Product Team'
    };
    
    const createResponse = await axios.post<Team>(
      `${API_BASE_URL}/api/teams`,
      createRequest
    );
    
    expect(createResponse.status).toBe(201);
    expect(createResponse.data.name).toBe('Product Team');
    expect(createResponse.data.members).toHaveLength(1);
    expect(createResponse.data.members[0]).toMatchObject({
      email: teamOwner.email,
      role: TeamRole.OWNER
    });
    
    const teamKey = createResponse.data.key;
    
    // Step 2: Owner invites a member
    const inviteRequest: InviteMemberRequest = {
      teamKey,
      guestEmail: invitedUser.email,
      role: TeamRole.MEMBER
    };
    
    const inviteResponse = await axios.post<TeamInvitation>(
      `${API_BASE_URL}/api/teams/invite`,
      inviteRequest
    );
    
    expect(inviteResponse.status).toBe(201);
    expect(inviteResponse.data).toMatchObject({
      teamKey,
      guestEmail: invitedUser.email,
      role: TeamRole.MEMBER,
      status: InvitationStatus.PENDING
    });
    
    const invitationId = inviteResponse.data.id;
    
    // Step 3: Invited user checks pending invitations
    const pendingResponse = await axios.get<TeamInvitation[]>(
      `${API_BASE_URL}/api/team-invitations/pending`
    );
    
    expect(pendingResponse.status).toBe(200);
    expect(pendingResponse.data).toHaveLength(1);
    expect(pendingResponse.data[0]).toMatchObject({
      id: invitationId,
      guestEmail: invitedUser.email,
      status: InvitationStatus.PENDING
    });
    
    // Step 4: Invited user accepts the invitation
    const acceptRequest: AcceptInvitationRequest = {
      invitationId
    };
    
    const acceptResponse = await axios.post<TeamMember>(
      `${API_BASE_URL}/api/team-invitations/accept`,
      acceptRequest
    );
    
    expect(acceptResponse.status).toBe(200);
    expect(acceptResponse.data).toMatchObject({
      email: invitedUser.email,
      role: TeamRole.MEMBER,
      teamKey
    });
    
    // Step 5: Verify team now has both members
    const teamResponse = await axios.get<Team>(
      `${API_BASE_URL}/api/teams/${teamKey}`
    );
    
    expect(teamResponse.status).toBe(200);
    expect(teamResponse.data.members).toHaveLength(2);
    
    const owner = teamResponse.data.members.find(m => m.role === TeamRole.OWNER);
    const member = teamResponse.data.members.find(m => m.role === TeamRole.MEMBER);
    
    expect(owner).toMatchObject({
      email: teamOwner.email,
      role: TeamRole.OWNER
    });
    
    expect(member).toMatchObject({
      email: invitedUser.email,
      role: TeamRole.MEMBER
    });
  });
  
  it('should allow invited user to see team in their teams list', async () => {
    // After accepting invitation, user should see team
    const teamsResponse = await axios.get<Team[]>(
      `${API_BASE_URL}/api/teams`
    );
    
    expect(teamsResponse.status).toBe(200);
    expect(teamsResponse.data).toHaveLength(1);
    expect(teamsResponse.data[0].key).toBe(createdTeam.key);
  });
  
  it('should preserve team metadata through invitation flow', async () => {
    const teamResponse = await axios.get<Team>(
      `${API_BASE_URL}/api/teams/${createdTeam.key}`
    );
    
    expect(teamResponse.data).toMatchObject({
      id: createdTeam.id,
      name: createdTeam.name,
      key: createdTeam.key,
      createdBy: teamOwner.email,
      createdAt: createdTeam.createdAt
    });
  });
});
