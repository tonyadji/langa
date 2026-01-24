/**
 * T226: Skeleton Tests
 */

import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { 
  Skeleton, 
  CardSkeleton, 
  TableRowSkeleton, 
  ListSkeleton, 
  FormSkeleton,
  PageSkeleton 
} from './Skeleton';

describe('Skeleton', () => {
  it('renders with default props', () => {
    const { container } = render(<Skeleton />);
    const skeleton = container.firstChild as HTMLElement;
    
    expect(skeleton).toBeInTheDocument();
    expect(skeleton).toHaveClass('bg-gray-200');
    expect(skeleton).toHaveClass('rounded-md'); // rectangular variant
    expect(skeleton).toHaveClass('animate-pulse'); // pulse animation
  });

  it('renders text variant', () => {
    const { container } = render(<Skeleton variant="text" />);
    const skeleton = container.firstChild as HTMLElement;
    
    expect(skeleton).toHaveClass('h-4');
    expect(skeleton).toHaveClass('rounded');
  });

  it('renders circular variant', () => {
    const { container } = render(<Skeleton variant="circular" />);
    const skeleton = container.firstChild as HTMLElement;
    
    expect(skeleton).toHaveClass('rounded-full');
  });

  it('renders button variant', () => {
    const { container } = render(<Skeleton variant="button" />);
    const skeleton = container.firstChild as HTMLElement;
    
    expect(skeleton).toHaveClass('h-10');
    expect(skeleton).toHaveClass('rounded-md');
  });

  it('applies custom width and height', () => {
    const { container } = render(<Skeleton width={100} height={50} />);
    const skeleton = container.firstChild as HTMLElement;
    
    expect(skeleton.style.width).toBe('100px');
    expect(skeleton.style.height).toBe('50px');
  });

  it('applies custom className', () => {
    const { container } = render(<Skeleton className="custom-class" />);
    const skeleton = container.firstChild as HTMLElement;
    
    expect(skeleton).toHaveClass('custom-class');
  });

  it('is hidden from screen readers', () => {
    const { container } = render(<Skeleton />);
    const skeleton = container.firstChild as HTMLElement;
    
    expect(skeleton).toHaveAttribute('aria-hidden', 'true');
  });

  it('supports no animation', () => {
    const { container } = render(<Skeleton animation="none" />);
    const skeleton = container.firstChild as HTMLElement;
    
    expect(skeleton).not.toHaveClass('animate-pulse');
  });
});

describe('CardSkeleton', () => {
  it('renders card structure', () => {
    const { container } = render(<CardSkeleton />);
    
    expect(container.querySelector('.bg-white')).toBeInTheDocument();
    expect(container.querySelector('.rounded-lg')).toBeInTheDocument();
    expect(container.querySelector('.shadow')).toBeInTheDocument();
  });

  it('renders multiple skeleton elements', () => {
    const { container } = render(<CardSkeleton />);
    const skeletons = container.querySelectorAll('[aria-hidden="true"]');
    
    // Should have title, subtitle, and buttons
    expect(skeletons.length).toBeGreaterThan(2);
  });
});

describe('TableRowSkeleton', () => {
  it('renders default number of columns', () => {
    const { container } = render(
      <table>
        <tbody>
          <TableRowSkeleton />
        </tbody>
      </table>
    );
    
    const cells = container.querySelectorAll('td');
    expect(cells).toHaveLength(4); // default columns
  });

  it('renders custom number of columns', () => {
    const { container } = render(
      <table>
        <tbody>
          <TableRowSkeleton columns={6} />
        </tbody>
      </table>
    );
    
    const cells = container.querySelectorAll('td');
    expect(cells).toHaveLength(6);
  });
});

describe('ListSkeleton', () => {
  it('renders default number of items', () => {
    const { container } = render(<ListSkeleton />);
    const items = container.querySelectorAll('.flex.items-center');
    
    expect(items).toHaveLength(3); // default items
  });

  it('renders custom number of items', () => {
    const { container } = render(<ListSkeleton items={5} />);
    const items = container.querySelectorAll('.flex.items-center');
    
    expect(items).toHaveLength(5);
  });

  it('renders circular avatars and text lines', () => {
    const { container } = render(<ListSkeleton items={1} />);
    
    expect(container.querySelector('.rounded-full')).toBeInTheDocument();
  });
});

describe('FormSkeleton', () => {
  it('renders default number of fields', () => {
    const { container } = render(<FormSkeleton />);
    const fields = container.querySelectorAll('.space-y-2');
    
    expect(fields).toHaveLength(3); // default fields
  });

  it('renders custom number of fields', () => {
    const { container } = render(<FormSkeleton fields={5} />);
    const fields = container.querySelectorAll('.space-y-2');
    
    expect(fields).toHaveLength(5);
  });

  it('renders submit button skeleton', () => {
    const { container } = render(<FormSkeleton />);
    const buttons = container.querySelectorAll('.h-10');
    
    expect(buttons.length).toBeGreaterThan(0);
  });
});

describe('PageSkeleton', () => {
  it('renders page structure', () => {
    const { container } = render(<PageSkeleton />);
    
    expect(container.querySelector('.space-y-6')).toBeInTheDocument();
  });

  it('renders multiple card skeletons', () => {
    const { container } = render(<PageSkeleton />);
    const cards = container.querySelectorAll('.bg-white.rounded-lg.shadow');
    
    expect(cards.length).toBeGreaterThanOrEqual(3);
  });
});
