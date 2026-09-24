import { type ReactNode } from 'react';
import { CheckCircle, XCircle, AlertTriangle, Info } from 'lucide-react';

export interface AlertProps {
  children: ReactNode;
  variant?: 'success' | 'error' | 'warning' | 'info';
  className?: string;
}

const variantStyles = {
  success: 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800 text-green-800 dark:text-green-400',
  error: 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800 text-red-800 dark:text-red-400',
  warning: 'bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-800 text-yellow-800 dark:text-yellow-400',
  info: 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800 text-blue-800 dark:text-blue-400',
};

const variantIcons = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
};

export function Alert({ children, variant = 'info', className = '' }: AlertProps) {
  return (
    <div
      className={`flex items-start gap-3 p-4 border rounded-lg ${variantStyles[variant]} ${className}`}
      role="alert"
    >
      <span className="flex-shrink-0">
        {(() => {
          const IconComponent = variantIcons[variant];
          return <IconComponent size={20} />;
        })()}
      </span>
      <div className="flex-1 text-sm">{children}</div>
    </div>
  );
}
