/**
 * ApplicationCard Component
 * 
 * Displays a single application with its details including name, owner status,
 * ingestion URI, and creation date.
 */

import { Link, useNavigate } from 'react-router-dom';
import { FileText, BarChart3 } from 'lucide-react';
import { useAuth } from '@/features/auth/hooks/useAuth';
import type { Application } from '@/types';

interface ApplicationCardProps {
  application: Application;
}

export function ApplicationCard({ application }: ApplicationCardProps) {
  const { user } = useAuth();
  const navigate = useNavigate();
  // Compare with username since backend uses username for owner field
  const isOwner = application.owner === user?.username;
  
  const handleQuickAction = (e: React.MouseEvent, path: string) => {
    e.preventDefault();
    e.stopPropagation();
    navigate(path);
  };
  
  return (
    <div
      className="block p-6 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 hover:shadow-lg dark:hover:shadow-gray-900/50 transition-shadow"
      data-testid={`application-card-${application.id}`}
    >
      <Link to={`/applications/${application.id}`} className="block">
        <div className="flex items-start justify-between mb-4">
          <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100">
            {application.name}
          </h3>
          <span
            className={`px-3 py-1 text-sm font-medium rounded-full ${
              isOwner
                ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400'
                : 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300'
            }`}
            data-testid={`badge-${isOwner ? 'owner' : 'shared'}`}
          >
            {isOwner ? 'Owner' : 'Shared'}
          </span>
        </div>
        
        <div className="space-y-2">
          {application.sharedWith && application.sharedWith.length > 0 && (
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Shared with {application.sharedWith.length} user(s)
              </p>
            </div>
          )}
          
          {application.createdAt && (
            <div>
              <p className="text-xs text-gray-400 dark:text-gray-500">
                Created {new Date(application.createdAt).toLocaleDateString()}
              </p>
            </div>
          )}
        </div>
      </Link>
      
      {/* Quick Actions */}
      <div className="flex gap-2 mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
        <button
          onClick={(e) => handleQuickAction(e, `/logs?applicationId=${application.id}`)}
          className="flex-1 px-3 py-2 text-sm font-medium text-blue-600 dark:text-blue-400 hover:text-blue-800 dark:hover:text-blue-300 hover:bg-blue-50 dark:hover:bg-blue-900/20 rounded-md transition-colors flex items-center justify-center gap-2"
          title="View Logs"
        >
          <FileText size={16} />
          <span>Logs</span>
        </button>
        <button
          onClick={(e) => handleQuickAction(e, `/metrics?applicationId=${application.id}`)}
          className="flex-1 px-3 py-2 text-sm font-medium text-green-600 dark:text-green-400 hover:text-green-800 dark:hover:text-green-300 hover:bg-green-50 dark:hover:bg-green-900/20 rounded-md transition-colors flex items-center justify-center gap-2"
          title="View Metrics"
        >
          <BarChart3 size={16} />
          <span>Metrics</span>
        </button>
      </div>
    </div>
  );
}
