export const TeamRole = {
  OWNER: 'OWNER',
  ADMIN: 'ADMIN',
  MEMBER: 'MEMBER',
} as const;

export type TeamRole = typeof TeamRole[keyof typeof TeamRole];

export const InvitationStatus = {
  CREATED: 'CREATED',
  SENT: 'SENT',
  ACCEPTED: 'ACCEPTED',
  EXPIRED: 'EXPIRED',
} as const;

export type InvitationStatus = typeof InvitationStatus[keyof typeof InvitationStatus];

export interface TeamMember {
  email: string;
  role: TeamRole;
  teamKey: string;
  addedDate: string;
}

export interface Team {
  id: string;
  name: string;
  key: string;
  members: TeamMember[] | null;
  memberCount: number;
  invitations?: TeamInvitation[];
  createdDate: string; // ISO 8601
  createdBy: string;
}

export interface TeamInvitationIdentity {
  teamId: string;
  invitationToken: string;
}

export interface TeamInvitationStakeHolders {
  team: string; // Team name
  host: string; // Inviter email
  guest: string; // Invitee email
}

export interface TeamInvitationPeriod {
  inviteDate: string; // ISO 8601
  expiryDate: string; // ISO 8601
}

export interface TeamInvitation {
  identity: TeamInvitationIdentity;
  stakeHolders: TeamInvitationStakeHolders;
  invitationPeriod: TeamInvitationPeriod;
  acceptedDate: string | null;
  status: InvitationStatus;
  teamId: string;
  token: string;
  expired: boolean;
}

export interface CreateTeamRequest {
  name: string;
}

export interface InviteMemberRequest {
  guest: string;
  team: string;
}

export interface InviteTeamMemberRequest {
  teamKey: string;
  guestEmail: string;
  role: TeamRole;
}

export interface AcceptInvitationRequest {
  guest: string;
  invitationToken: string;
}

export interface AcceptInvitationResponse {
  id: string;
  token: string;
  team: string;
  host: string;
  guest: string;
  expiryDate: string;
  status: InvitationStatus;
}

// Helper specific for the response of invite endpoint if it returns DTO
export interface TeamResponseDto {
    team: Team; // Simplification unless specified otherwise
}

export interface GetInvitationResponse extends TeamInvitation {
}

// Additional types for API
export interface TeamWithMembers extends Team {
  members: TeamMember[];
}

export interface CreateTeamDto {
  name: string;
}

export interface UpdateTeamDto {
  name?: string;
}

export interface AddTeamMemberDto {
  guestEmail: string;
  role: TeamRole;
}

export interface RemoveMemberRequest {
  teamKey: string;
  memberEmail: string;
}
