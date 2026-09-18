import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { AxiosError } from 'axios';
import { Eye, EyeOff } from 'lucide-react';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Card } from '@/components/common/Card';
import { Alert } from '@/components/common/Alert';
import { isValidEmail, isValidPassword } from '@/utils/validators';
import type { RegisterRequest } from '@/types/api';

export interface RegisterFormProps {
  onSubmit?: (data: RegisterRequest) => Promise<void>;
  onSuccess?: () => void;
}

export function RegisterForm({ onSubmit, onSuccess }: RegisterFormProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const getPasswordStrength = (pwd: string): string => {
    if (pwd.length === 0) return '';
    if (pwd.length < 8) return 'Weak';
    if (pwd.length < 12) return 'Medium';
    if (/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/.test(pwd)) {
      return 'Strong';
    }
    return 'Medium';
  };
  
  const passwordStrength = getPasswordStrength(password);
  
  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};
    
    if (!email) {
      newErrors.email = 'Email is required';
    } else if (!isValidEmail(email)) {
      newErrors.email = 'Invalid email format';
    }
    
    if (!password) {
      newErrors.password = 'Password is required';
    } else if (!isValidPassword(password)) {
      newErrors.password = 'Password must be at least 8 characters';
    }
    
    if (!confirmPassword) {
      newErrors.confirmPassword = 'Please confirm your password';
    } else if (password !== confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setServerError('');
    
    if (!validate()) {
      return;
    }
    
    setIsLoading(true);
    
    try {
      const registerData: RegisterRequest = {
        username: email,
        password,
        confirmationPassword: confirmPassword,
      };
      
      if (onSubmit) {
        await onSubmit(registerData);
      }
      
      if (onSuccess) {
        onSuccess();
      }
    } catch (error) {
      let errorMessage = 'Registration failed. Please try again.';
      
      if (error instanceof AxiosError && error.response?.data?.error) {
        errorMessage = error.response.data.error;
      } else if (error instanceof Error) {
        errorMessage = error.message;
      }
      
      setServerError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleInputChange = () => {
    if (serverError) setServerError('');
    if (Object.keys(errors).length > 0) setErrors({});
  };
  
  return (
    <Card className="w-full max-w-md p-8">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">Create Account</h1>
        <p className="text-gray-600 dark:text-gray-300">Sign up for a Langa account</p>
      </div>
      
      {serverError && (
        <Alert variant="error" className="mb-6">
          {serverError}
        </Alert>
      )}
      
      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Email
          </label>
          <Input
            id="email"
            type="email"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              handleInputChange();
            }}
            placeholder="you@example.com"
            error={errors.email}
            disabled={isLoading}
            autoComplete="email"
            required
          />
        </div>
        
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Password
          </label>
          <div className="relative">
            <Input
              id="password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                handleInputChange();
              }}
              placeholder="••••••••"
              error={errors.password}
              disabled={isLoading}
              autoComplete="new-password"
              required
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300"
              aria-label={showPassword ? 'Hide password' : 'Show password'}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          {password && (
            <div className="mt-2">
              <span
                className={`text-sm font-medium ${
                  passwordStrength === 'Weak'
                    ? 'text-red-600 dark:text-red-400'
                    : passwordStrength === 'Medium'
                    ? 'text-yellow-600 dark:text-yellow-400'
                    : 'text-green-600 dark:text-green-400'
                }`}
              >
                Strength: {passwordStrength}
              </span>
            </div>
          )}
        </div>
        
        <div>
          <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Confirm Password
          </label>
          <Input
            id="confirmPassword"
            type="password"
            value={confirmPassword}
            onChange={(e) => {
              setConfirmPassword(e.target.value);
              handleInputChange();
            }}
            placeholder="••••••••"
            error={errors.confirmPassword}
            disabled={isLoading}
            autoComplete="new-password"
            required
          />
        </div>
        
        <Button
          type="submit"
          variant="primary"
          fullWidth
          disabled={isLoading}
          loading={isLoading}
        >
          {isLoading ? 'Creating account...' : 'Register'}
        </Button>
      </form>
      
      <div className="mt-6 text-center text-sm">
        <span className="text-gray-600 dark:text-gray-400">Already have an account? </span>
        <Link to="/login" className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium">
          Log In
        </Link>
      </div>
    </Card>
  );
}
