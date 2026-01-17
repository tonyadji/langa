# Input Sanitization & Security Guide

## Overview

This document outlines input sanitization practices implemented in the Langa Dashboard frontend to prevent security vulnerabilities such as XSS, SQL injection, and other input-based attacks.

## Core Principles

1. **Never trust user input**: All user input must be validated and sanitized
2. **Defense in depth**: Multiple layers of protection (client + server)
3. **Escape by default**: Use frameworks that escape automatically (React)
4. **Validate early**: Check input at entry points before processing

## React Built-in Protection

React provides automatic XSS protection through:

### JSX Auto-Escaping

React automatically escapes all values embedded in JSX:

\`\`\`typescript
// Safe - React escapes the content
const userInput = '<script>alert("XSS")</script>';
return <div>{userInput}</div>;
// Renders: &lt;script&gt;alert("XSS")&lt;/script&gt;
\`\`\`

### Dangerous Exceptions

Never use `dangerouslySetInnerHTML` unless absolutely necessary:

\`\`\`typescript
// ❌ DANGEROUS - Avoid this
<div dangerouslySetInnerHTML={{ __html: userInput }} />

// ✅ SAFE - Use React's escaping
<div>{userInput}</div>
\`\`\`

## Input Validation Patterns

### Email Validation

\`\`\`typescript
// src/features/auth/utils/validation.ts
export function validateEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

// Usage in forms
if (!validateEmail(email)) {
  setError('Invalid email address');
  return;
}
\`\`\`

### String Length Limits

\`\`\`typescript
// Enforce maximum length
const MAX_NAME_LENGTH = 255;
const MAX_DESCRIPTION_LENGTH = 1000;

function validateName(name: string): string | null {
  if (!name || name.trim().length === 0) {
    return 'Name is required';
  }
  if (name.length > MAX_NAME_LENGTH) {
    return \`Name must be less than \${MAX_NAME_LENGTH} characters\`;
  }
  return null;
}
\`\`\`

### Whitespace Trimming

\`\`\`typescript
// Always trim user input
const sanitizedName = name.trim();
const sanitizedEmail = email.trim().toLowerCase();
\`\`\`

### Special Characters

\`\`\`typescript
// Remove or escape special characters if needed
function sanitizeApplicationName(name: string): string {
  // Remove potentially dangerous characters
  return name.replace(/[<>\"'&]/g, '');
}

// Or allow only specific characters
function validateSlug(slug: string): boolean {
  return /^[a-z0-9-]+$/.test(slug);
}
\`\`\`

## Form Input Sanitization

### Search Inputs

\`\`\`typescript
// src/features/logs/components/LogFilters.tsx
const handleSearchChange = (value: string) => {
  // Trim whitespace
  const sanitized = value.trim();
  
  // Limit length to prevent performance issues
  const maxLength = 500;
  const truncated = sanitized.slice(0, maxLength);
  
  setSearch(truncated);
};
\`\`\`

### URL Parameters

\`\`\`typescript
// Validate URL parameters before use
const appId = params.appId;

// Check format (UUID)
if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(appId)) {
  navigate('/applications');
  return;
}
\`\`\`

### File Uploads

Currently not implemented, but if adding file uploads:

\`\`\`typescript
function validateFile(file: File): string | null {
  // Check file type
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
  if (!allowedTypes.includes(file.type)) {
    return 'Invalid file type';
  }
  
  // Check file size (5MB max)
  const maxSize = 5 * 1024 * 1024;
  if (file.size > maxSize) {
    return 'File too large (max 5MB)';
  }
  
  return null;
}
\`\`\`

## API Request Sanitization

### Request Body Validation

\`\`\`typescript
// src/services/applicationService.ts
export async function createApplication(data: CreateApplicationRequest) {
  // Sanitize before sending to API
  const sanitized = {
    name: data.name.trim().slice(0, 255),
    description: data.description?.trim().slice(0, 1000) || '',
  };
  
  return api.post('/applications', sanitized);
}
\`\`\`

### Query Parameters

\`\`\`typescript
// Sanitize query parameters
const params = {
  search: search.trim().slice(0, 500),
  limit: Math.min(Math.max(Number(limit) || 20, 1), 100),
  offset: Math.max(Number(offset) || 0, 0),
};
\`\`\`

## Content Security Policy

Security headers configured in `vite.config.ts`:

\`\`\`typescript
headers: {
  'X-Frame-Options': 'DENY',
  'X-Content-Type-Options': 'nosniff',
  'X-XSS-Protection': '1; mode=block',
  'Referrer-Policy': 'strict-origin-when-cross-origin',
}
\`\`\`

## Clipboard Operations

Safe clipboard usage:

\`\`\`typescript
// src/utils/clipboard.ts
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    // Use modern Clipboard API
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    // Fallback for older browsers
    return fallbackCopy(text);
  }
}

// Never copy user-provided HTML
// Only copy plain text
\`\`\`

## localStorage Security

\`\`\`typescript
// src/services/authService.ts
// Store only necessary data
localStorage.setItem('accessToken', token); // JWT tokens are safe
localStorage.setItem('userId', userId); // UUIDs are safe

// ❌ Don't store sensitive data
// localStorage.setItem('password', password); // NEVER
// localStorage.setItem('creditCard', card); // NEVER
\`\`\`

## Current Sanitization Coverage

### ✅ Protected Areas

1. **Authentication Forms**
   - Email validation and trimming
   - Password length validation
   - No special character injection

2. **Application Management**
   - Name and description length limits
   - Trimmed whitespace
   - React auto-escaping

3. **Search Inputs**
   - Length limits (500 chars)
   - Trimmed whitespace
   - No SQL injection (API uses prepared statements)

4. **Team Invitations**
   - Email validation
   - Duplicate checking

5. **Sharing Features**
   - User ID validation (UUID format)
   - Permission enum validation

### ⚠️ Areas Requiring Additional Validation

1. **Log Filtering**
   - Currently accepts any search string
   - Recommendation: Add regex validation for special characters

2. **Metric Time Ranges**
   - Currently uses predefined enums (safe)
   - If custom ranges added, validate date formats

3. **User Profile Updates** (if implemented)
   - Would need comprehensive validation

## Best Practices Checklist

- [X] Use React's built-in XSS protection
- [X] Avoid `dangerouslySetInnerHTML`
- [X] Validate email addresses
- [X] Trim all string inputs
- [X] Enforce length limits
- [X] Use TypeScript for type safety
- [X] Sanitize API request bodies
- [X] Validate URL parameters
- [X] Configure security headers
- [X] Never store sensitive data in localStorage
- [ ] Implement rate limiting (backend responsibility)
- [ ] Add CSRF tokens (backend responsibility)

## Testing Sanitization

### Manual Testing

Test with malicious inputs:

\`\`\`
<script>alert('XSS')</script>
javascript:alert('XSS')
"><img src=x onerror=alert('XSS')>
'; DROP TABLE users; --
../../../etc/passwd
\${7*7}
{{7*7}}
\`\`\`

All should be safely escaped or rejected.

### Automated Testing

\`\`\`typescript
// Example test
describe('Input Sanitization', () => {
  it('should escape XSS attempts', () => {
    const malicious = '<script>alert("XSS")</script>';
    render(<div>{malicious}</div>);
    expect(screen.getByText(malicious)).toBeInTheDocument();
    expect(screen.queryByRole('script')).not.toBeInTheDocument();
  });
});
\`\`\`

## Security Updates

- Review dependencies monthly: `npm audit`
- Update packages regularly: `npm update`
- Monitor security advisories
- Test after each update

## Reporting Security Issues

If you discover a security vulnerability:

1. **Do NOT** open a public issue
2. Email security team directly
3. Provide detailed reproduction steps
4. Allow time for patch before disclosure

## References

- [OWASP XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)
- [React Security Best Practices](https://react.dev/reference/react-dom/components/common#security-caveats)
- [MDN: Input Validation](https://developer.mozilla.org/en-US/docs/Learn/Forms/Form_validation)

---

**Last Updated**: January 7, 2026
**Version**: 1.0
