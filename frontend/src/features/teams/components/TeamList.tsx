/**
 * TeamList Component
 * 
 * Lists all teams for the current user
 */

import type { Team } from '@/types';
import { TeamCard } from './TeamCard';

interface TeamListProps {
  teams: Team[];
}

export const TeamList: React.FC<TeamListProps> = ({ teams }) => {
  if (teams.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">No teams yet. Create your first team to get started!</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {teams.map((team) => (
        <TeamCard key={team.id} team={team} />
      ))}
    </div>
  );
};
