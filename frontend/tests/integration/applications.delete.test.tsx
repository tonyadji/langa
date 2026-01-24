import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationApi } from '@/services/applicationApi';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const handlers = [
  http.delete(`${API_BASE_URL}/applications/:id`, ({ params }) => {
    const { id } = params;
    if (id === 'existing-app-id') {
      return new HttpResponse(null, { status: 204 });
    }
    return new HttpResponse(null, { status: 404 });
  }),
];

const server = setupServer(...handlers);

describe('DELETE /api/applications/{id}', () => {
  beforeAll(() => server.listen());
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  it('should delete application successfully', async () => {
    await applicationApi.deleteApplication('existing-app-id');
    // If no error thrown, success
  });

  it('should throw error when application not found', async () => {
    await expect(applicationApi.deleteApplication('non-existent-id')).rejects.toThrow();
  });
});
