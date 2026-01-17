import { useNavigate } from 'react-router-dom';
import { LoginForm } from '@/features/auth/components/LoginForm';
import { useAuth } from '@/features/auth/hooks/useAuth';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  
  const handleLogin = async (data: { username: string; password: string }) => {
    await login(data.username, data.password);
  };
  
  const handleSuccess = () => {
    // Redirect to dashboard or setup wizard based on firstConnection
    navigate('/dashboard');
  };
  
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center p-4">
      <LoginForm onSubmit={handleLogin} onSuccess={handleSuccess} />
    </div>
  );
}

