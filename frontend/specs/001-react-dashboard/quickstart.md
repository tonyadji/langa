# Quick Start Guide: Langa Dashboard Development

**Feature**: 001-react-dashboard  
**Date**: December 31, 2025  
**Audience**: Developers joining the project

## Prerequisites

Before you begin, ensure you have the following installed:

- **Node.js** 18.0.0 or higher
- **npm** 9.0.0 or higher (or **pnpm** 8.0.0+)
- **Git** for version control
- **VS Code** (recommended) or your preferred editor

## Initial Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/langa-dashboard.git
cd langa-dashboard
```

### 2. Install Dependencies

```bash
npm install
# or
pnpm install
```

### 3. Configure Environment Variables

Create a `.env.local` file in the project root:

```bash
cp .env.example .env.local
```

Edit `.env.local` with your configuration:

```env
# Backend API URL
VITE_API_BASE_URL=http://localhost:8080

# Application name
VITE_APP_NAME=Langa Dashboard

# Enable debug mode (development only)
VITE_ENABLE_DEBUG=true
```

**Note**: Never commit `.env.local` to git. It's already in `.gitignore`.

### 4. Start the Langa Backend

The dashboard requires the Langa Backend API to be running. Follow the backend setup instructions in the [backend documentation](../../../documents/02-TUTORIAL.md).

Quick backend setup:

```bash
cd ../backend  # Navigate to backend directory
./mvnw spring-boot:run
```

Verify the backend is running:
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

### 5. Start the Development Server

```bash
npm run dev
# or
pnpm dev
```

The dashboard will be available at `http://localhost:5173`

## Development Workflow

### Running Tests

```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with coverage
npm run test:coverage

# Run E2E tests
npm run test:e2e
```

### Linting and Formatting

```bash
# Lint code
npm run lint

# Fix lint errors automatically
npm run lint:fix

# Format code with Prettier
npm run format

# Type check
npm run type-check
```

### Building for Production

```bash
# Create production build
npm run build

# Preview production build locally
npm run preview
```

## Project Structure Overview

```
src/
├── components/       # Reusable UI components
│   ├── common/      # Generic components (Button, Input, Modal, etc.)
│   └── layout/      # Layout components (Header, Sidebar, etc.)
├── features/        # Feature-specific modules
│   ├── auth/        # Authentication
│   ├── applications/# Application management
│   ├── logs/        # Log viewing
│   ├── metrics/     # Metrics visualization
│   └── teams/       # Team management
├── pages/           # Route pages
├── hooks/           # Custom React hooks
├── services/        # API clients and services
├── types/           # TypeScript type definitions
├── utils/           # Helper functions
├── styles/          # Global styles and Tailwind config
├── router/          # Routing configuration
└── App.tsx          # Root component
```

## Common Tasks

### Creating a New Component

1. Create component file in appropriate directory:

```typescript
// src/components/common/NewComponent.tsx
import React from 'react';

interface NewComponentProps {
  title: string;
  onAction?: () => void;
}

export const NewComponent: React.FC<NewComponentProps> = ({ title, onAction }) => {
  return (
    <div className="p-4 bg-white rounded-lg shadow">
      <h2 className="text-xl font-bold">{title}</h2>
      {onAction && (
        <button
          onClick={onAction}
          className="mt-2 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Action
        </button>
      )}
    </div>
  );
};
```

2. Create test file:

```typescript
// src/components/common/NewComponent.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { NewComponent } from './NewComponent';

describe('NewComponent', () => {
  it('renders title correctly', () => {
    render(<NewComponent title="Test Title" />);
    expect(screen.getByText('Test Title')).toBeInTheDocument();
  });

  it('calls onAction when button clicked', () => {
    const handleAction = vi.fn();
    render(<NewComponent title="Test" onAction={handleAction} />);
    
    fireEvent.click(screen.getByText('Action'));
    expect(handleAction).toHaveBeenCalledTimes(1);
  });
});
```

### Adding a New API Endpoint

1. Add type to `src/types/api.ts`
2. Add endpoint to appropriate API module in `src/services/`
3. Create hook in relevant feature directory
4. Write tests for the hook

**Important**: When importing types with `verbatimModuleSyntax: true` in tsconfig:
- Use `import type` for interfaces and type aliases
- Use regular `import` for enums, classes, and runtime values
- Example: `import type { Team } from '@/types/team'; import { TeamRole } from '@/types/team';`

Example:

```typescript
// src/features/applications/hooks/useDeleteApplication.ts
import { useState } from 'react';
import { applicationsApi } from '@/services/api';

export const useDeleteApplication = () => {
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const deleteApplication = async (appId: string) => {
    setIsDeleting(true);
    setError(null);
    
    try {
      await applicationsApi.delete(appId);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete application');
      return false;
    } finally {
      setIsDeleting(false);
    }
  };

  return { deleteApplication, isDeleting, error };
};
```

### Styling with Tailwind CSS

The project uses Tailwind CSS for styling. Follow these guidelines:

```typescript
// ✅ Good: Use Tailwind utilities
<div className="flex items-center justify-between p-4 bg-gray-100 rounded-lg">
  <h2 className="text-xl font-semibold text-gray-800">Title</h2>
  <button className="px-4 py-2 text-white bg-blue-500 rounded hover:bg-blue-600">
    Action
  </button>
</div>

// ❌ Avoid: Inline styles
<div style={{ display: 'flex', padding: '16px' }}>
  ...
</div>

// ✅ For complex reusable styles, create components
import { Button } from '@/components/common/Button';
<Button variant="primary" size="md">Action</Button>
```

## Debugging

### VS Code Launch Configuration

Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "chrome",
      "request": "launch",
      "name": "Launch Chrome",
      "url": "http://localhost:5173",
      "webRoot": "${workspaceFolder}/src"
    }
  ]
}
```

### Browser DevTools

- React DevTools extension for component inspection
- Redux DevTools (if using Redux later)
- Network tab for API requests
- Console for errors and logs

### Common Issues

**Issue**: `VITE_API_BASE_URL is undefined`
- **Solution**: Ensure `.env.local` exists and variables start with `VITE_`

**Issue**: CORS errors when calling backend
- **Solution**: Backend must have CORS configured for `http://localhost:5173`

**Issue**: TypeScript errors about missing types
- **Solution**: Run `npm install` to ensure all type definitions are installed

## Git Workflow

### Branch Naming

- Feature: `feature/add-log-filtering`
- Bug fix: `fix/token-refresh-issue`
- Refactor: `refactor/auth-context`

### Commit Messages

Follow conventional commits:

```bash
feat: add log filtering by level
fix: resolve token refresh race condition
docs: update quickstart guide
test: add tests for useAuth hook
refactor: simplify API error handling
```

### Pull Request Process

1. Create feature branch from `main`
2. Implement feature with tests
3. Ensure all checks pass:
   - `npm run lint`
   - `npm run type-check`
   - `npm test`
   - `npm run build`
4. Create PR with description and screenshots
5. Request review
6. Address review comments
7. Merge when approved

## Performance Best Practices

- Use `React.memo()` for expensive component re-renders
- Use `useMemo()` for expensive calculations
- Use `useCallback()` for callback stability
- Lazy load routes with `React.lazy()`
- Virtualize large lists with `@tanstack/react-virtual`
- Optimize images (use WebP, lazy load)
- Monitor bundle size with `npm run build -- --stats`

## Accessibility Guidelines

- Use semantic HTML (`<nav>`, `<main>`, `<button>`, etc.)
- Add ARIA labels where needed
- Ensure keyboard navigation works
- Test with screen readers
- Maintain color contrast ratios (WCAG AA)
- Run `npm run test:a11y` for accessibility tests

## Resources

- [React Documentation](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Tailwind CSS Docs](https://tailwindcss.com/docs)
- [Vite Guide](https://vitejs.dev/guide/)
- [React Testing Library](https://testing-library.com/react)
- [Langa Backend Specification](../../../documents/01-SPECIFICATION.md)

## Getting Help

- **Documentation**: Check `docs/` folder for detailed guides
- **Issues**: Check existing GitHub issues or create a new one
- **Team Chat**: Reach out on Slack/Discord
- **Code Review**: Ask for pair programming session

## Next Steps

1. Read the [feature specification](spec.md)
2. Review the [implementation plan](plan.md)
3. Check the [data model](data-model.md)
4. Explore the [API contracts](contracts/api-client.ts)
5. Start with a small task to familiarize yourself with the codebase

Happy coding! 🚀
