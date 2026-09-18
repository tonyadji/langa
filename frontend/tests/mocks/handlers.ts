import { http, HttpResponse } from 'msw';

const API_BASE_URL = 'http://localhost:8080/api';

export const handlers = [
  // Auth handlers
  http.post(`${API_BASE_URL}/auth/login`, () => {
    return HttpResponse.json({
      token: 'mock-jwt-token',
      user: {
        id: '1',
        email: 'test@example.com',
        name: 'Test User',
        role: 'admin',
      },
    });
  }),

  http.post(`${API_BASE_URL}/auth/logout`, () => {
    return HttpResponse.json({ success: true });
  }),

  http.get(`${API_BASE_URL}/auth/me`, () => {
    return HttpResponse.json({
      id: '1',
      email: 'test@example.com',
      name: 'Test User',
      role: 'admin',
    });
  }),

  // Applications handlers
  http.get(`${API_BASE_URL}/applications`, () => {
    return HttpResponse.json({
      data: [],
      total: 0,
      page: 1,
      limit: 10,
    });
  }),

  // Logs handlers
  http.get(`${API_BASE_URL}/logs`, () => {
    return HttpResponse.json({
      data: [],
      total: 0,
      page: 1,
      limit: 50,
    });
  }),

  // Metrics handlers
  http.get(`${API_BASE_URL}/metrics`, () => {
    return HttpResponse.json({
      cpuUsage: [],
      memoryUsage: [],
      requestRate: [],
    });
  }),
];
