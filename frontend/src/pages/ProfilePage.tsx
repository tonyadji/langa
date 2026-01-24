import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useTheme } from '@/contexts/ThemeContext';
import { Card } from '@/components/common/Card';
import { Button } from '@/components/common/Button';
import { Spinner } from '@/components/common/Spinner';
import { Moon, Sun, Monitor } from 'lucide-react';

export function ProfilePage() {
  const { user, logout, isAuthenticated, fetchUserProfile } = useAuth();
  const { theme, setTheme } = useTheme();
  const navigate = useNavigate();
  const [isLoadingProfile, setIsLoadingProfile] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    // Fetch latest user profile when page loads
    const loadProfile = async () => {
      if (isAuthenticated) {
        setIsLoadingProfile(true);
        try {
          await fetchUserProfile();
        } catch (error) {
          console.error('Failed to load profile:', error);
        } finally {
          setIsLoadingProfile(false);
        }
      }
    };
    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  if (!user) {
    return null;
  }

  return (
    <div className="flex flex-col h-full">
      {/* Fixed Header */}
      <div className="flex-none pb-4 border-b border-gray-200 dark:border-gray-700">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Profile</h1>
      </div>

      {/* Scrollable Content */}
      <div className="flex-1 overflow-y-auto scrollbar-light">
        <div className="pt-4 max-w-3xl mx-auto">
          {isLoadingProfile ? (
            <div className="flex justify-center items-center py-12">
              <Spinner size="lg" />
            </div>
          ) : (
            <>
              <Card className="p-6 mb-6">
                <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">Account Information</h2>
                
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Email
                    </label>
                    <div className="text-gray-900 dark:text-gray-100">
                      {user.email}
                    </div>
                  </div>
                  
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                      Account Key
                    </label>
                    <div className="text-gray-900 dark:text-gray-100 font-mono text-sm bg-gray-50 dark:bg-gray-700 p-2 rounded">
                      {user.accountKey}
                    </div>
                  </div>
                  
                  {user.username && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                        Username
                      </label>
                      <div className="text-gray-900 dark:text-gray-100">
                        {user.username}
                      </div>
                    </div>
                  )}
                  
                  {user.registrationDate && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                        Member Since
                      </label>
                      <div className="text-gray-900 dark:text-gray-100">
                        {new Date(user.registrationDate).toLocaleDateString('en-US', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                        })}
                      </div>
                    </div>
                  )}
                </div>
              </Card>

              <Card className="p-6 mb-6">
                <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">Appearance</h2>
                
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                      Theme Mode
                    </label>
                    <div className="grid grid-cols-3 gap-3">
                      <button
                        onClick={() => setTheme('light')}
                        className={`flex flex-col items-center gap-2 p-4 rounded-lg border-2 transition-all ${
                          theme === 'light'
                            ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                            : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
                        }`}
                      >
                        <Sun className={`w-6 h-6 ${theme === 'light' ? 'text-blue-500' : 'text-gray-500 dark:text-gray-400'}`} />
                        <span className={`text-sm font-medium ${
                          theme === 'light' ? 'text-blue-700 dark:text-blue-400' : 'text-gray-700 dark:text-gray-300'
                        }`}>
                          Light
                        </span>
                      </button>

                      <button
                        onClick={() => setTheme('dark')}
                        className={`flex flex-col items-center gap-2 p-4 rounded-lg border-2 transition-all ${
                          theme === 'dark'
                            ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                            : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
                        }`}
                      >
                        <Moon className={`w-6 h-6 ${theme === 'dark' ? 'text-blue-500' : 'text-gray-500 dark:text-gray-400'}`} />
                        <span className={`text-sm font-medium ${
                          theme === 'dark' ? 'text-blue-700 dark:text-blue-400' : 'text-gray-700 dark:text-gray-300'
                        }`}>
                          Dark
                        </span>
                      </button>

                      <button
                        onClick={() => setTheme('system')}
                        className={`flex flex-col items-center gap-2 p-4 rounded-lg border-2 transition-all ${
                          theme === 'system'
                            ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                            : 'border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600'
                        }`}
                      >
                        <Monitor className={`w-6 h-6 ${theme === 'system' ? 'text-blue-500' : 'text-gray-500 dark:text-gray-400'}`} />
                        <span className={`text-sm font-medium ${
                          theme === 'system' ? 'text-blue-700 dark:text-blue-400' : 'text-gray-700 dark:text-gray-300'
                        }`}>
                          System
                        </span>
                      </button>
                    </div>
                    <p className="mt-2 text-xs text-gray-500 dark:text-gray-400">
                      {theme === 'system' 
                        ? 'Theme follows your system preferences'
                        : `Theme is always ${theme}`
                      }
                    </p>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">Account Actions</h2>
                
                <div className="space-y-4">
                  <div>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
                      Sign out of your account. You will need to log in again to access the dashboard.
                    </p>
                    <Button
                      variant="secondary"
                      onClick={handleLogout}
                    >
                      Log Out
                    </Button>
                  </div>
                </div>
              </Card>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
