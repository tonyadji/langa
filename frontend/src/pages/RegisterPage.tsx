import { useNavigate } from 'react-router-dom';
import { RegisterForm } from '@/features/auth/components/RegisterForm';
import { useAuth } from '@/features/auth/hooks/useAuth';

export function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  
  const handleRegister = async (data: { username: string; password: string; confirmationPassword: string }) => {
    await register(data.username, data.password, data.confirmationPassword);
  };
  
  const handleSuccess = () => {
    // Redirect to login page after successful registration
    navigate('/login');
  };
  
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center p-4">
      <RegisterForm onSubmit={handleRegister} onSuccess={handleSuccess} />
    </div>
  );
}
