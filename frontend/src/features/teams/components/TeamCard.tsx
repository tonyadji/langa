/**
 * TeamCard Component
 * 
 * Displays team summary with member count and creation info
 */

import { Users } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { Team } from '@/types';

interface TeamCardProps {
  team: Team;
}

export const TeamCard: React.FC<TeamCardProps> = ({ team }) => {
  const createdDate = new Date(team.createdDate).toLocaleDateString();

  return (
    <Link
      to={`/teams/${team.id}`}
      className="block p-4 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg hover:shadow-md dark:hover:shadow-gray-900/50 transition-shadow"
    >
      <div className="flex items-start justify-between mb-2">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">{team.name}</h3>
        <div className="flex items-center text-sm text-gray-500 dark:text-gray-400">
          <Users size={16} className="mr-1" />
          <span>{team.memberCount}</span>
        </div>
      </div>

      <div className="text-sm text-gray-600 dark:text-gray-400">
        <p><span className="font-semibold">Team Key:</span> <span className="font-mono text-xs">{team.key}</span></p>
        <p className="mt-1">Created <span className="font-semibold">by</span> {team.createdBy} <span className="font-semibold">on</span> {createdDate}</p>
      </div>
    </Link>
  );
};
