// Validation utilities for form inputs

export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const isValidPassword = (password: string): boolean => {
  // Minimum 8 characters, at least one letter and one number
  return password.length >= 8 && /[a-zA-Z]/.test(password) && /\d/.test(password);
};

export const isValidUrl = (url: string): boolean => {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};

export const validateRequired = (value: string): string | undefined => {
  return value.trim() === '' ? 'This field is required' : undefined;
};

export const validateEmail = (email: string): string | undefined => {
  if (!email) return 'Email is required';
  if (!isValidEmail(email)) return 'Invalid email address';
  return undefined;
};

export const validatePassword = (password: string): string | undefined => {
  if (!password) return 'Password is required';
  if (password.length < 8) return 'Password must be at least 8 characters';
  if (!/[a-zA-Z]/.test(password)) return 'Password must contain at least one letter';
  if (!/\d/.test(password)) return 'Password must contain at least one number';
  return undefined;
};

export const validatePasswordConfirmation = (
  password: string,
  confirmation: string
): string | undefined => {
  if (!confirmation) return 'Please confirm your password';
  if (password !== confirmation) return 'Passwords do not match';
  return undefined;
};

export const validateMinLength = (value: string, minLength: number): string | undefined => {
  if (value.length < minLength) {
    return `Must be at least ${minLength} characters`;
  }
  return undefined;
};

export const validateMaxLength = (value: string, maxLength: number): string | undefined => {
  if (value.length > maxLength) {
    return `Must be no more than ${maxLength} characters`;
  }
  return undefined;
};
