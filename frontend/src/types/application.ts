export const SharedWithProfile = {
  USER: 'USER',
  TEAM: 'TEAM',
} as const;

export type SharedWithProfile = typeof SharedWithProfile[keyof typeof SharedWithProfile];

export interface ShareWith {
  appId: string;
  appName: string;
  key: string; // User account key or team key
  profile: SharedWithProfile;
  sharedDate: string;
  expirationDate: string | null;
  revokedDate: string | null;
  currentlyActive: boolean;
  expired: boolean;
  revoked: boolean;
}

export interface Application {
  id: string;
  name: string;
  key: string;
  accountKey: string;
  ingestionUri: string;
  owner: string;
  shareWith: ShareWith[];
  sharedWith?: ShareWith[]; // Alias for shareWith (backward compatibility)
  createdAt?: string; // ISO 8601 timestamp
  secret?: string; // Application secret (only in ApplicationSecured)
  http?: string; // HTTP endpoint
  kafka?: string; // Kafka endpoint
}

export const RetentionUnit = {
  Nanos: 'NANOS',
  Micros: 'MICROS',
  Millis: 'MILLIS',
  Seconds: 'SECONDS',
  Minutes: 'MINUTES',
  Hours: 'HOURS',
  HalfDays: 'HALF_DAYS',
  Days: 'DAYS',
  Weeks: 'WEEKS',
  Months: 'MONTHS',
  Years: 'YEARS',
  Decades: 'DECADES',
  Centuries: 'CENTURIES',
  Millennia: 'MILLENNIA',
  Eras: 'ERAS',
  Forever: 'FOREVER',
} as const;

export type RetentionUnit = typeof RetentionUnit[keyof typeof RetentionUnit];

export interface RetentionPolicy {
  duration: number;
  unit: RetentionUnit;
  lastUpdatedDate: string;
}

export interface ApplicationUsageTrend {
  name: string;
  key: string;
  usage: number; // Bytes consumed at this point
}

export interface ApplicationUsage {
  id: string;
  key: string;
  name: string;
  logUsage: number;
  metricUsage: number;
  trends: ApplicationUsageTrend[];
}

export interface ApplicationSecured extends Application {
  secret: string;
  retentionPolicy: RetentionPolicy;
  http: string;
  kafka: string;
  usage: ApplicationUsage;
}

// Request Types
export interface CreateApplicationRequest {
  name: string;
}

export interface ShareApplicationRequest {
  shareWith: string; // User email or team key
  profile: SharedWithProfile;
}

export interface RevokeAccessRequest {
  shareWith: string; // User email or team key
  profile: SharedWithProfile;
}

export interface UpdateRetentionPolicyRequest {
  duration: number;
  unit: RetentionUnit;
}
