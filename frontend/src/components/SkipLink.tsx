/**
 * T225: SkipLink Component
 * 
 * Accessibility enhancement for keyboard users.
 * Provides a "Skip to main content" link that appears on keyboard focus.
 */

interface SkipLinkProps {
  targetId?: string;
  children?: React.ReactNode;
}

export function SkipLink({ 
  targetId = 'main-content', 
  children = 'Skip to main content' 
}: SkipLinkProps) {
  return (
    <a
      href={`#${targetId}`}
      className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-blue-600 focus:text-white focus:rounded-md focus:shadow-lg"
    >
      {children}
    </a>
  );
}
