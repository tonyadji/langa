/**
 * Usage-related type definitions
 * 
 * Note: The backend API returns usage data with different field names:
 * - Backend: { id, key, name, logUsage, metricUsage }
 * - Frontend: { id, appKey, totalLogBytes, totalMetricBytes }
 * 
 * The mapping is handled automatically in useApplicationUsage hook.
 */

export interface ApplicationUsage {
  id: string;                 // Unique identifier
  appKey: string;             // Application key (from backend 'key')
  totalLogBytes: number;      // Total bytes used by logs (from backend 'logUsage')
  totalMetricBytes: number;   // Total bytes used by metrics (from backend 'metricUsage')
}
