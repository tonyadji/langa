/**
 * Contract Test: POST /api/auth/login
 * 
 * Validates the login endpoint contract according to the API specification.
 * Tests successful authentication, error cases, and JWT token response format.
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { authApi } from '@/services/authApi';
import type { LoginRequest, AuthResponse } from '@/types/api';

const API_BASE_URL = 'http://localhost:8080/api';

// Mock handlers for the login endpoint
const handlers = [
  http.post(`${API_BASE_URL}/auth/login`, async ({ request }) => {
    const body = await request.json() as LoginRequest;
    
    // Validate required fields
    if (!body.username || !body.password) {
      return HttpResponse.json(
        { error: 'Missing credentials' },
        { status: 400 }
      );
    }
    
    // Simulate invalid credentials - accept testuser for tests
    if (body.username === 'testuser' && body.password === 'correctpassword') {
      // Successful login returns JWT tokens
      const authResponse: AuthResponse = {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.mock-access-token',
        refreshToken: 'mock-refresh-token-uuid-1234',
        email: 'user@example.com',
      };
      
      return HttpResponse.json(authResponse, { status: 200 });
    }
    
    // Invalid credentials for all other combinations
    return HttpResponse.json(
      { error: 'Invalid username or password' },
      { status: 401 }
    );
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('POST /api/auth/login - Contract Tests', () => {
  it('should login successfully with valid credentials', async () => {
    const loginData: LoginRequest = {
      username: 'testuser',
      password: 'correctpassword',
    };
    
    const response = await authApi.login(loginData);
    
    expect(response).toBeDefined();
    expect(response.accessToken).toBeDefined();
    expect(response.refreshToken).toBeDefined();
    expect(response.email).toBe('user@example.com');
    expect(typeof response.accessToken).toBe('string');
    expect(typeof response.refreshToken).toBe('string');
  });
  
  it('should reject login with invalid credentials', async () => {
    const loginData: LoginRequest = {
      username: 'testuser',
      password: 'wrongpassword',
    };
    
    await expect(authApi.login(loginData)).rejects.toThrow();
  });
  
  it('should reject login with non-existent user', async () => {
    const loginData: LoginRequest = {
      username: 'nonexistentuser',
      password: 'somepassword',
    };
    
    await expect(authApi.login(loginData)).rejects.toThrow();
  });
  
  it('should reject login with missing username', async () => {
    const loginData = {
      username: '',
      password: 'somepassword',
    } as LoginRequest;
    
    await expect(authApi.login(loginData)).rejects.toThrow();
  });
  
  it('should reject login with missing password', async () => {
    const loginData = {
      username: 'testuser',
      password: '',
    } as LoginRequest;
    
    await expect(authApi.login(loginData)).rejects.toThrow();
  });
  
  it('should return valid JWT token structure', async () => {
    const loginData: LoginRequest = {
      username: 'testuser',
      password: 'correctpassword',
    };
    
    const response = await authApi.login(loginData);
    
    // Validate JWT token format (header.payload.signature)
    const tokenParts = response.accessToken.split('.');
    expect(tokenParts).toHaveLength(3);
    
    // Validate refresh token exists
    expect(response.refreshToken.length).toBeGreaterThan(0);
  });
  
  it('should store tokens securely after successful login', async () => {
    const loginData: LoginRequest = {
      username: 'testuser',
      password: 'correctpassword',
    };
    
    const response = await authApi.login(loginData);
    
    // Tokens should not contain sensitive user data in plain text
    expect(response.accessToken).not.toContain('password');
    expect(response.refreshToken).not.toContain('password');
  });
});
