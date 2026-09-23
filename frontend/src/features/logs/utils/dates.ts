/** datetime-local value (browser time) → UTC timestamp without zone, as expected by the backend */
export function toBackendDate(localValue: string): string | undefined {
  if (!localValue) return undefined;
  const date = new Date(localValue);
  return isNaN(date.getTime()) ? undefined : date.toISOString().slice(0, 19);
}

/** Backend UTC timestamp → datetime-local value (browser time) */
export function toLocalInput(backendValue?: string): string {
  if (!backendValue) return '';
  const date = new Date(`${backendValue}Z`);
  if (isNaN(date.getTime())) return '';
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return local.toISOString().slice(0, 16);
}

/** Log timestamp (ISO 8601 or epoch milliseconds) in a readable form */
export function formatTimestamp(timestamp: string): string {
  const date = /^\d+$/.test(timestamp) ? new Date(Number(timestamp)) : new Date(timestamp);
  if (isNaN(date.getTime())) {
    return timestamp;
  }
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    fractionalSecondDigits: 3,
  });
}
