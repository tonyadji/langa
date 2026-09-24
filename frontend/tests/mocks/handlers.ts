import { http, HttpResponse } from 'msw';

const API_BASE_URL = 'http://localhost:8080/api';

export const handlers = [
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
