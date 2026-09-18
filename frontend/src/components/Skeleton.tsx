/**
 * T226: Skeleton Component
 * 
 * Reusable skeleton loading component for async operations.
 * Provides better UX than basic spinners by showing content placeholders.
 */

interface SkeletonProps {
  className?: string;
  variant?: 'text' | 'circular' | 'rectangular' | 'button';
  animation?: 'pulse' | 'wave' | 'none';
  width?: string | number;
  height?: string | number;
}

export function Skeleton({ 
  className = '',
  variant = 'rectangular',
  animation = 'pulse',
  width,
  height 
}: SkeletonProps) {
  const animationClass = animation === 'pulse' 
    ? 'animate-pulse' 
    : animation === 'wave'
    ? 'animate-shimmer'
    : '';

  const variantClass = {
    text: 'h-4 rounded',
    circular: 'rounded-full',
    rectangular: 'rounded-md',
    button: 'h-10 rounded-md',
  }[variant];

  const style: React.CSSProperties = {};
  if (width) style.width = typeof width === 'number' ? `${width}px` : width;
  if (height) style.height = typeof height === 'number' ? `${height}px` : height;

  const classNames = ['bg-gray-200', variantClass, animationClass, className]
    .filter(Boolean)
    .join(' ');

  return (
    <div
      className={classNames}
      style={style}
      aria-hidden="true"
    />
  );
}

/**
 * Card skeleton for loading application/team cards
 */
export function CardSkeleton() {
  return (
    <div className="bg-white rounded-lg shadow p-6 space-y-4">
      <Skeleton variant="text" className="w-3/4" />
      <Skeleton variant="text" className="w-1/2" />
      <div className="flex gap-2 mt-4">
        <Skeleton variant="button" className="w-24" />
        <Skeleton variant="button" className="w-24" />
      </div>
    </div>
  );
}

/**
 * Table row skeleton for loading table data
 */
export function TableRowSkeleton({ columns = 4 }: { columns?: number }) {
  return (
    <tr className="border-b">
      {Array.from({ length: columns }).map((_, i) => (
        <td key={i} className="px-6 py-4">
          <Skeleton variant="text" />
        </td>
      ))}
    </tr>
  );
}

/**
 * List skeleton for loading lists
 */
export function ListSkeleton({ items = 3 }: { items?: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: items }).map((_, i) => (
        <div key={i} className="flex items-center gap-3">
          <Skeleton variant="circular" width={40} height={40} />
          <div className="flex-1 space-y-2">
            <Skeleton variant="text" className="w-3/4" />
            <Skeleton variant="text" className="w-1/2" />
          </div>
        </div>
      ))}
    </div>
  );
}

/**
 * Form skeleton for loading forms
 */
export function FormSkeleton({ fields = 3 }: { fields?: number }) {
  return (
    <div className="space-y-4">
      {Array.from({ length: fields }).map((_, i) => (
        <div key={i} className="space-y-2">
          <Skeleton variant="text" className="w-32" height={16} />
          <Skeleton variant="rectangular" className="w-full" height={40} />
        </div>
      ))}
      <Skeleton variant="button" className="w-full mt-6" />
    </div>
  );
}

/**
 * Page skeleton for loading full pages
 */
export function PageSkeleton() {
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="space-y-2">
        <Skeleton variant="text" className="w-1/3" height={32} />
        <Skeleton variant="text" className="w-1/2" height={20} />
      </div>

      {/* Content */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <CardSkeleton />
        <CardSkeleton />
        <CardSkeleton />
      </div>
    </div>
  );
}
