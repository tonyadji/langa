/**
 * ApplicationList Component
 * 
 * Displays a searchable grid of applications with filtering capabilities.
 */

import { ApplicationCard } from './ApplicationCard';
import type { Application } from '@/types';

interface ApplicationListProps {
  applications: Application[];
  searchTerm?: string;
}

export function ApplicationList({ applications = [], searchTerm = '' }: ApplicationListProps) {
  const filteredApplications = applications.filter((app) =>
    app.name.toLowerCase().includes(searchTerm.toLowerCase())
  );
  
  return (
    <div>
      
      {filteredApplications.length === 0 ? (
        <div className="text-center py-12">
          {searchTerm ? (
            <p className="text-gray-500">
              No applications found matching "{searchTerm}"
            </p>
          ) : (
            <p className="text-gray-500">
              No applications yet. Create your first application to get started.
            </p>
          )}
        </div>
      ) : (
        <div
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
          data-testid="applications-grid"
        >
          {filteredApplications.map((application) => (
            <ApplicationCard key={application.id} application={application} />
          ))}
        </div>
      )}
    </div>
  );
}
