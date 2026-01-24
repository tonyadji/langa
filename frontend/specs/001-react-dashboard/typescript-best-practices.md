# TypeScript Best Practices for Langa Dashboard

## Import Type Discipline

This project uses `verbatimModuleSyntax: true` in `tsconfig.json`, which requires strict import type discipline.

### Core Rule

**Use `import type` for type-only imports, use regular `import` for runtime values.**

### What Requires `import type`

- Interfaces: `export interface Team { ... }`
- Type aliases: `export type Status = 'active' | 'inactive';`
- Type-only re-exports: `export type { Something } from './module';`

### What Requires Regular `import`

- Enums: `export enum TeamRole { OWNER, ADMIN, MEMBER }`
- Classes: `export class ApiClient { ... }`
- Functions: `export function formatDate() { ... }`
- Constants: `export const API_URL = 'http://...';`

### Examples

#### ✅ Correct

```typescript
// Separate imports for types and runtime values
import type { Team, TeamMember } from '@/types/team';
import { TeamRole, InvitationStatus } from '@/types/team';

// Or if you only need types
import type { Application } from '@/types/application';

// Or if you only need runtime values
import { LogLevel } from '@/types/log';
```

#### ❌ Incorrect

```typescript
// Mixing types and enums in one import
import { Team, TeamRole } from '@/types/team'; // ERROR!

// Using regular import for types
import { Team } from '@/types/team'; // ERROR!

// Using import type for enums
import type { TeamRole } from '@/types/team'; // ERROR!
```

### Common Patterns

#### Pattern 1: Component with Props Interface

```typescript
import type { Team } from '@/types/team';
import { TeamRole } from '@/types/team';

interface TeamCardProps {
  team: Team;
  onSelect: (team: Team) => void;
}

export const TeamCard: React.FC<TeamCardProps> = ({ team }) => {
  const isOwner = team.role === TeamRole.OWNER;
  // ...
};
```

#### Pattern 2: Hook with Request/Response Types

```typescript
import { useState } from 'react';
import api from '@/services/api';
import type { CreateTeamRequest, Team } from '@/types/team';

export const useCreateTeam = () => {
  const [isLoading, setIsLoading] = useState(false);
  
  const createTeam = async (data: CreateTeamRequest): Promise<Team> => {
    const response = await api.post<Team>('/teams', data);
    return response.data;
  };
  
  return { createTeam, isLoading };
};
```

#### Pattern 3: Service with Multiple Type Imports

```typescript
import type { Team, TeamMember, CreateTeamRequest } from '@/types/team';
import { TeamRole } from '@/types/team';

class TeamService {
  async createTeam(request: CreateTeamRequest): Promise<Team> {
    // Implementation
  }
  
  filterByRole(members: TeamMember[], role: TeamRole): TeamMember[] {
    return members.filter(m => m.role === role);
  }
}
```

### Why This Matters

1. **Module Resolution**: TypeScript compiler needs to know what to erase vs. preserve
2. **Bundle Size**: Type-only imports are stripped from production bundles
3. **Build Performance**: Faster compilation when types are explicitly marked
4. **Runtime Errors**: Prevents importing types as runtime values (which causes module not found errors)

### Troubleshooting

#### Error: "does not provide an export named 'X'"

**Cause**: You're using regular `import` for a type, or `import type` for an enum.

**Solution**: Check if X is:
- Interface/Type → Use `import type`
- Enum/Class/Function → Use regular `import`

#### Error: "Cannot use namespace 'X' as a value"

**Cause**: Trying to use a type as a runtime value.

**Solution**: 
- If it's an enum, use regular `import`
- If it needs to be a runtime value, convert type to enum or const object

### Migration Checklist

When adding new types or fixing import errors:

- [ ] Identify what you're importing (interface, type, enum, class, function)
- [ ] Use `import type` for interfaces and type aliases
- [ ] Use regular `import` for enums and runtime values
- [ ] Separate imports if mixing types and runtime values
- [ ] Test in browser (not just TypeScript compiler)
- [ ] Clear Vite cache if module errors persist (`rm -rf node_modules/.vite`)

### Additional Resources

- [TypeScript: `verbatimModuleSyntax` Documentation](https://www.typescriptlang.org/tsconfig#verbatimModuleSyntax)
- [TypeScript: Type-Only Imports](https://www.typescriptlang.org/docs/handbook/release-notes/typescript-3-8.html#type-only-imports-and-export)

---

**Last Updated**: January 4, 2026  
**Applies To**: All TypeScript files in the project
