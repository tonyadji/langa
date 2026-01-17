import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Card } from '@/components/common/Card';
import { Alert } from '@/components/common/Alert';
import type { LoginRequest } from '@/types/api';

export interface LoginFormProps {
  onSubmit?: (data: LoginRequest) => Promise<void>;
  onSuccess?: () => void;
}

export function LoginForm({ onSubmit, onSuccess }: LoginFormProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};
    
    if (!username) {
      newErrors.username = 'Username is required';
    }
    
    if (!password) {
      newErrors.password = 'Password is required';
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
      const loginData: LoginRequest = {
        username,
        password,
      };
      
      if (onSubmit) {
        await onSubmit(loginData);
      }
      
      if (onSuccess) {
        onSuccess();
      }
    } catch (error) {
      setServerError(
        error instanceof Error ? error.message : 'Invalid credentials. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleInputChange = () => {
    // Clear errors when user starts typing
    if (serverError) setServerError('');
    if (Object.keys(errors).length > 0) setErrors({});
  };
  
  return (
    <Card className="w-full max-w-md p-8">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">Welcome Back</h1>
        <p className="text-gray-600 dark:text-gray-300">Sign in to your Langa account</p>
      </div>
      
      {serverError && (
        <Alert variant="error" className="mb-6">
          {serverError}
        </Alert>
      )}
      
      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label htmlFor="username" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Username
          </label>
          <Input
            id="username"
            type="text"
            value={username}
            onChange={(e) => {
              setUsername(e.target.value);
              handleInputChange();
            }}
            placeholder="Username or email"
            error={errors.username}
            disabled={isLoading}
            autoComplete="username"
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
              autoComplete="current-password"
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
        </div>
        
        <Button
          type="submit"
          variant="primary"
          fullWidth
          disabled={isLoading}
          loading={isLoading}
        >
          {isLoading ? 'Signing in...' : 'Log In'}
        </Button>
      </form>
      
      <div className="mt-6 text-center text-sm">
        <span className="text-gray-600 dark:text-gray-400">Don't have an account? </span>
        <Link to="/register" className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium">
          Register
        </Link>
      </div>
    </Card>
  );
}
