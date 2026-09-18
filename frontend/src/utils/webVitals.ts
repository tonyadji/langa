/**
 * T233: Web Vitals Monitoring
 * 
 * Monitors Core Web Vitals metrics:
 * - FCP (First Contentful Paint): < 1.5s
 * - LCP (Largest Contentful Paint): < 2.5s
 * - TTI (Time to Interactive): < 3s
 * - INP (Interaction to Next Paint): < 200ms
 * - CLS (Cumulative Layout Shift): < 0.1
 * 
 * Per constitution Section IV performance requirements.
 */

import { onCLS, onFCP, onINP, onLCP, onTTFB, type Metric } from 'web-vitals';

export interface WebVitalsMetrics {
  fcp?: number;
  lcp?: number;
  inp?: number;
  cls?: number;
  ttfb?: number;
}

type MetricHandler = (metric: Metric) => void;

/**
 * Report Web Vitals to console (development)
 */
function logMetric(metric: Metric): void {
  const { name, value, rating } = metric;
  const emoji = rating === 'good' ? '✅' : rating === 'needs-improvement' ? '⚠️' : '❌';
  
  console.log(`[Web Vitals] ${emoji} ${name}: ${Math.round(value)}ms (${rating})`);
}

/**
 * Report Web Vitals to analytics service (production)
 */
function sendToAnalytics(metric: Metric): void {
  // Send to analytics service (Google Analytics, Custom API, etc.)
  if (import.meta.env.VITE_ENABLE_ANALYTICS === 'true') {
    // Example: Google Analytics 4
    if (typeof window !== 'undefined' && (window as any).gtag) {
      (window as any).gtag('event', metric.name, {
        value: Math.round(metric.name === 'CLS' ? metric.value * 1000 : metric.value),
        metric_id: metric.id,
        metric_value: metric.value,
        metric_delta: metric.delta,
      });
    }

    // Example: Custom API endpoint
    if (import.meta.env.VITE_API_BASE_URL) {
      const endpoint = `${import.meta.env.VITE_API_BASE_URL}/analytics/vitals`;
      
      fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: metric.name,
          value: metric.value,
          rating: metric.rating,
          delta: metric.delta,
          id: metric.id,
          timestamp: Date.now(),
          url: window.location.href,
          userAgent: navigator.userAgent,
        }),
        keepalive: true, // Ensure request completes even if page is closing
      }).catch((error) => {
        console.error('Failed to send vitals to analytics:', error);
      });
    }
  }
}

/**
 * Get thresholds for each metric per constitution
 */
export const VITALS_THRESHOLDS = {
  FCP: 1500,  // First Contentful Paint < 1.5s
  LCP: 2500,  // Largest Contentful Paint < 2.5s
  TTI: 3000,  // Time to Interactive < 3s (approximated by TBT)
  INP: 200,   // Interaction to Next Paint < 200ms
  CLS: 0.1,   // Cumulative Layout Shift < 0.1
  TTFB: 600,  // Time to First Byte < 600ms (not in constitution but good practice)
} as const;

/**
 * Check if metric meets constitution requirements
 */
export function meetsThreshold(name: string, value: number): boolean {
  const threshold = VITALS_THRESHOLDS[name as keyof typeof VITALS_THRESHOLDS];
  return threshold !== undefined && value <= threshold;
}

/**
 * Initialize Web Vitals monitoring
 * Call this once in your application entry point (main.tsx)
 */
export function initWebVitals(onMetric?: MetricHandler): void {
  const isDev = import.meta.env.DEV;
  
  // Default handler: log in dev, send to analytics in prod
  const handler: MetricHandler = onMetric || ((metric) => {
    if (isDev) {
      logMetric(metric);
    } else {
      sendToAnalytics(metric);
    }
  });

  // Register all vital metrics
  onFCP(handler);
  onLCP(handler);
  onINP(handler);
  onCLS(handler);
  onTTFB(handler);

  // Log initialization in development
  if (isDev) {
    console.log('[Web Vitals] Monitoring initialized');
    console.log('[Web Vitals] Thresholds (per constitution):');
    Object.entries(VITALS_THRESHOLDS).forEach(([name, value]) => {
      console.log(`  - ${name}: < ${value}${name === 'CLS' ? '' : 'ms'}`);
    });
  }
}

/**
 * Get current vitals (useful for debugging)
 */
export function getCurrentVitals(): Promise<WebVitalsMetrics> {
  return new Promise((resolve) => {
    const metrics: WebVitalsMetrics = {};
    let reportedCount = 0;
    const expectedCount = 5; // FCP, LCP, INP, CLS, TTFB

    const checkComplete = () => {
      reportedCount++;
      if (reportedCount >= expectedCount) {
        resolve(metrics);
      }
    };

    onFCP((metric) => {
      metrics.fcp = metric.value;
      checkComplete();
    });

    onLCP((metric) => {
      metrics.lcp = metric.value;
      checkComplete();
    });

    onINP((metric) => {
      metrics.inp = metric.value;
      checkComplete();
    });

    onCLS((metric) => {
      metrics.cls = metric.value;
      checkComplete();
    });

    onTTFB((metric) => {
      metrics.ttfb = metric.value;
      checkComplete();
    });

    // Timeout after 10 seconds
    setTimeout(() => resolve(metrics), 10000);
  });
}

/**
 * Create performance mark (for custom measurements)
 */
export function performanceMark(name: string): void {
  if (typeof performance !== 'undefined' && performance.mark) {
    performance.mark(name);
  }
}

/**
 * Measure performance between two marks
 */
export function performanceMeasure(
  name: string,
  startMark: string,
  endMark: string
): number | null {
  if (typeof performance !== 'undefined' && performance.measure) {
    try {
      const measure = performance.measure(name, startMark, endMark);
      return measure.duration;
    } catch (error) {
      console.error('Failed to measure performance:', error);
      return null;
    }
  }
  return null;
}
