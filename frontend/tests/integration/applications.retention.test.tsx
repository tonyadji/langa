import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationApi } from '@/services/applicationApi';
import { RetentionUnit } from '@/types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const handlers = [
  http.put(`${API_BASE_URL}/applications/:id/update-retention-policy`, async ({ params, request }) => {
    const { id } = params;
    const body = await request.json() as any;
    
    if (id === 'app-123') {
        return HttpResponse.json({
            id: 'app-123',
            name: 'Test App',
            retentionPolicy: body.retentionPolicy
        });
    }
    return new HttpResponse(null, { status: 404 });
  }),
];

const server = setupServer(...handlers);

describe('PUT /api/applications/{id}/update-retention-policy', () => {
  beforeAll(() => server.listen());
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  it('should update retention policy successfully', async () => {
    const response = await applicationApi.updateRetentionPolicy('app-123', {
        retentionPolicy: {
            duration: 30,
            unit: RetentionUnit.Days
        }
    });
    
    expect(response.retentionPolicy.duration).toBe(30);
    expect(response.retentionPolicy.unit).toBe(RetentionUnit.Days);
  });
});
