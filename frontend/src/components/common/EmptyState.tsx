import React from 'react';

export interface EmptyStateProps {
  title: string;
  description?: string;
  icon?: React.ReactNode;
  action?: React.ReactNode;
  className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  description,
  icon,
  action,
  className = '',
}) => {
  return (
    <div className={`text-center py-12 px-4 ${className}`}>
      {icon && (
        <div className="flex justify-center mb-4 text-gray-400 dark:text-gray-500">
          {icon}
        </div>
      )}
      <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">{title}</h3>
      {description && (
        <p className="text-sm text-gray-500 dark:text-gray-400 mb-6 max-w-md mx-auto">{description}</p>
      )}
      {action && <div className="flex justify-center">{action}</div>}
    </div>
  );
};
