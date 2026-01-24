/**
 * Application Details Page
 * 
 * Displays detailed information about a specific application,
 * including credentials for owners with show/hide functionality.
 */

import { useParams, Link, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { FileText, BarChart3, Eye, EyeOff, Copy, Check, Share2, Trash2 } from 'lucide-react';
import { applicationApi } from '@/services/applicationApi';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { ShareApplicationModal } from '@/features/applications/components/ShareApplicationModal';
import { SharedUsersList } from '@/features/applications/components/SharedUsersList';
import { useApplicationUsage } from '@/features/usage/hooks/useApplicationUsage';
import { UsageStats } from '@/features/usage/components/UsageStats';
import { UsageBreakdown } from '@/features/usage/components/UsageBreakdown';
import { Modal } from '@/components/common/Modal';
import { Button } from '@/components/common/Button';
import { RetentionPolicySettings } from '@/features/applications/components/RetentionPolicySettings';
import type { Application, ApplicationSecured, RetentionUnit } from '@/types';

export function ApplicationDetailsPage() {
  const { appId } = useParams<{ appId: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [application, setApplication] = useState<Application | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [showSecrets, setShowSecrets] = useState<Record<string, boolean>>({});
  const [copiedField, setCopiedField] = useState<string | null>(null);
  const [isShareModalOpen, setIsShareModalOpen] = useState(false);
  const [timePeriod, setTimePeriod] = useState<'7d' | '30d' | '90d'>('30d');
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  
  const isOwner = application?.owner === user?.username;
  
  const toggleSecretVisibility = (key: string) => {
    setShowSecrets(prev => ({ ...prev, [key]: !prev[key] }));
  };
  
  const maskSecret = (secret: string | null | undefined) => {
    if (!secret) return '';
    return '•'.repeat(Math.min(secret.length, 40));
  };
  
  const copyToClipboard = async (text: string, fieldName: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedField(fieldName);
      setTimeout(() => setCopiedField(null), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };
  
  const handleShareSuccess = async () => {
    if (!appId) return;
    // Refetch application to get updated shareWith list
    try {
      const data = await applicationApi.getSecuredDetails(appId);
      setApplication(data);
    } catch (err) {
      // If forbidden, fall back to basic details
      if (err instanceof Error && err.message.includes('403')) {
        const basicData = await applicationApi.getApplication(appId);
        setApplication(basicData);
      }
    }
  };

  const handleUpdateRetention = async (duration: number, unit: RetentionUnit) => {
    if (!application) return;
    try {
      await applicationApi.updateRetentionPolicy(application.id, {
        duration,
        unit
      });
      // Refetch to reflect changes
      const data = await applicationApi.getSecuredDetails(application.id);
      setApplication(data);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to update retention policy'));
      throw err;
    }
  };

  const handleDeleteApplication = async () => {
    if (!application) return;
    try {
      setIsDeleting(true);
      await applicationApi.deleteApplication(application.id);
      setIsDeleteModalOpen(false);
      navigate('/applications');
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to delete application'));
      setIsDeleteModalOpen(false);
    } finally {
      setIsDeleting(false);
    }
  };
  
  useEffect(() => {
    const fetchApplication = async () => {
      if (!appId) return;
      
      try {
        setIsLoading(true);
        setError(null);
        
        // Try to fetch secured details (only works for owners)
        try {
          const data = await applicationApi.getSecuredDetails(appId);
          setApplication(data);
        } catch (securedErr) {
          // If secured details fail (403, 400, etc.), fall back to basic details
          // This happens when the app is shared with the user but they're not the owner
          const basicData = await applicationApi.getApplication(appId);
          setApplication(basicData);
        }
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Failed to fetch application'));
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchApplication();
  }, [appId]);
  
  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-400"></div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="p-6 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
          <h2 className="text-lg font-semibold text-red-900 dark:text-red-400 mb-2">Error</h2>
          <p className="text-sm text-red-800 dark:text-red-400">{error.message}</p>
        </div>
        <Link
          to="/applications"
          className="mt-4 inline-block text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300"
        >
          ← Back to Applications
        </Link>
      </div>
    );
  }
  
  if (!application) {
    return (
      <div className="max-w-2xl mx-auto">
        <div className="p-6 bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg">
          <p className="text-gray-600 dark:text-gray-400">Application not found</p>
        </div>
        <Link
          to="/applications"
          className="mt-4 inline-block text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300"
        >
          ← Back to Applications
        </Link>
      </div>
    );
  }
  
  return (
    <div className="flex flex-col h-full">
      {/* Fixed Header */}
      <div className="flex-none max-w-4xl mx-auto w-full pb-4">
        <Link
          to="/applications"
          className="inline-flex items-center text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300"
        >
          ← Back to Applications
        </Link>
      </div>

      {/* Scrollable Content */}
      <div className="flex-1 overflow-y-auto scrollbar-light">
        <div className="max-w-4xl mx-auto">
          <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-8">
        <div className="flex justify-between items-start mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">{application.name}</h1>
            {application.createdAt && (
              <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
                Created {new Date(application.createdAt).toLocaleDateString()}
              </p>
            )}
          </div>
          <span
            className={`px-4 py-2 text-sm font-medium rounded-full ${
              isOwner
                ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400'
                : 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300'
            }`}
          >
            {isOwner ? 'Owner' : 'Shared'}
          </span>
        </div>
        
        {/* Quick Actions */}
        <div className="mt-6 flex gap-3">
          <Link
            to={`/logs?applicationId=${application.id}`}
            className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-3 bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-400 border border-blue-200 dark:border-blue-800 rounded-lg hover:bg-blue-100 dark:hover:bg-blue-900/30 transition-colors"
          >
            <FileText size={18} />
            <span className="font-medium">View Logs</span>
          </Link>
          <Link
            to={`/metrics?applicationId=${application.id}`}
            className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-3 bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 border border-green-200 dark:border-green-800 rounded-lg hover:bg-green-100 dark:hover:bg-green-900/30 transition-colors"
          >
            <BarChart3 size={18} />
            <span className="font-medium">View Metrics</span>
          </Link>
          {isOwner && (
            <button
              onClick={() => setIsShareModalOpen(true)}
              className="flex-1 inline-flex items-center justify-center gap-2 px-4 py-3 bg-purple-50 dark:bg-purple-900/20 text-purple-700 dark:text-purple-400 border border-purple-200 dark:border-purple-800 rounded-lg hover:bg-purple-100 dark:hover:bg-purple-900/30 transition-colors"
            >
              <Share2 size={18} />
              <span className="font-medium">Share</span>
            </button>
          )}
        </div>
        
        <div className="space-y-6 mt-6">
          {/* Application ID */}
          <div>
            <h2 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Application ID</h2>
            <div className="flex items-center gap-2">
              <p className="flex-1 text-sm text-gray-900 dark:text-gray-100 font-mono bg-gray-50 dark:bg-gray-700 p-3 rounded">
                {showSecrets['id'] ? application.id : maskSecret(application.id)}
              </p>
              <button
                onClick={() => toggleSecretVisibility('id')}
                className="px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                aria-label={showSecrets['id'] ? 'Hide ID' : 'Show ID'}
              >
                {showSecrets['id'] ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
              <button
                onClick={() => copyToClipboard(application.id, 'id')}
                className="px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                aria-label="Copy ID"
              >
                {copiedField === 'id' ? <Check size={16} /> : <Copy size={16} />}
              </button>
            </div>
          </div>
          
          {/* Account Key */}
          <div>
            <h2 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Account Key</h2>
            <div className="flex items-center gap-2">
              <p className="flex-1 text-sm text-gray-900 dark:text-gray-100 font-mono bg-gray-50 dark:bg-gray-700 p-3 rounded break-all">
                {showSecrets['accountKey'] ? application.accountKey : maskSecret(application.accountKey)}
              </p>
              <button
                onClick={() => toggleSecretVisibility('accountKey')}
                className="px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                aria-label={showSecrets['accountKey'] ? 'Hide Account Key' : 'Show Account Key'}
              >
                {showSecrets['accountKey'] ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
              <button
                onClick={() => copyToClipboard(application.accountKey, 'accountKey')}
                className="px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                aria-label="Copy Account Key"
              >
                {copiedField === 'accountKey' ? <Check size={16} /> : <Copy size={16} />}
              </button>
            </div>
          </div>
          
          {/* Owner */}
          <div>
            <h2 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Owner</h2>
            <p className="text-sm text-gray-900 dark:text-gray-100 bg-gray-50 dark:bg-gray-700 p-3 rounded">
              {application.owner}
            </p>
          </div>
          
          {/* Owner-only Section: Application Key, Secret, and Ingestion Endpoints */}
          {isOwner && application.key && (
            <div className="border-t pt-6">
              <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4 mb-4">
                <p className="text-sm text-blue-800 dark:text-blue-400">
                  <strong>Owner Access:</strong> The following credentials are only visible to you as the application owner.
                </p>
              </div>
              
              {/* Application Key */}
              <div className="mb-4">
                <h2 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Application Key</h2>
                <div className="flex items-center gap-2">
                  <p className="flex-1 text-sm text-gray-900 dark:text-gray-100 font-mono bg-gray-50 dark:bg-gray-700 p-3 rounded break-all">
                    {showSecrets['key'] ? application.key : maskSecret(application.key)}
                  </p>
                  <button
                    onClick={() => toggleSecretVisibility('key')}
                    className="px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                    aria-label={showSecrets['key'] ? 'Hide Key' : 'Show Key'}
                  >
                    {showSecrets['key'] ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                  <button
                    onClick={() => copyToClipboard(application.key, 'key')}
                    className="px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                    aria-label="Copy Application Key"
                  >
                    {copiedField === 'key' ? <Check size={16} /> : <Copy size={16} />}
                  </button>
                </div>
              </div>
              
              {/* Application Secret */}
              {application.secret && (
                <div className="mb-4">
                  <h2 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Application Secret</h2>
                  <div className="flex items-center gap-2">
                    <p className="flex-1 text-sm text-gray-900 dark:text-gray-100 font-mono bg-gray-50 dark:bg-gray-700 p-3 rounded break-all">
                      {showSecrets['secret'] ? application.secret : maskSecret(application.secret)}
                    </p>
                    <button
                      onClick={() => toggleSecretVisibility('secret')}
                      className="px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                      aria-label={showSecrets['secret'] ? 'Hide Secret' : 'Show Secret'}
                    >
                      {showSecrets['secret'] ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                    <button
                      onClick={() => copyToClipboard(application.secret ?? '', 'secret')}
                      className="px-3 py-2 text-sm text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
                      aria-label="Copy Application Secret"
                    >
                      {copiedField === 'secret' ? <Check size={16} /> : <Copy size={16} />}
                    </button>
                  </div>
                </div>
              )}
              
              {/* Ingestion Endpoints */}
              <div className="border-t border-gray-200 dark:border-gray-700 pt-4">
                <h2 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Ingestion Endpoints</h2>
                
                {/* HTTP Endpoint */}
                {application.http && (
                  <div className="mb-3">
                    <h3 className="text-xs font-medium text-gray-600 dark:text-gray-400 mb-1">HTTP</h3>
                    <p className="text-sm text-gray-900 dark:text-gray-100 font-mono bg-gray-50 dark:bg-gray-700 p-3 rounded break-all">
                      {application.http}
                    </p>
                  </div>
                )}
                
                {/* Kafka Endpoint */}
                {application.kafka && (
                  <div>
                    <h3 className="text-xs font-medium text-gray-600 dark:text-gray-400 mb-1">Kafka</h3>
                    <p className="text-sm text-gray-900 dark:text-gray-100 font-mono bg-gray-50 dark:bg-gray-700 p-3 rounded break-all">
                      {application.kafka}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Retention Policy (Owner Only) */}
          {isOwner && (
            <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
              <RetentionPolicySettings
                initialPolicy={(application as ApplicationSecured).retentionPolicy}
                onSave={handleUpdateRetention}
              />
            </div>
          )}
          
          {/* Non-owner message */}
          {!isOwner && (
            <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
              <div className="bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-4">
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  This application is shared with you. Contact the owner ({application.owner}) for API credentials.
                </p>
              </div>
            </div>
          )}
          
          {/* Shared With Section */}
          <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
            <h2 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Shared With</h2>
            <SharedUsersList
              applicationId={application.id}
              sharedWith={application.shareWith || []}
              isOwner={isOwner}
              onRevokeSuccess={handleShareSuccess}
            />
          </div>

          {/* Danger Zone (Owner Only) */}
          {isOwner && (
            <div className="border-t border-gray-200 dark:border-gray-700 mt-8 pt-8">
              <h2 className="text-lg font-semibold text-red-600 dark:text-red-400 mb-4">Danger Zone</h2>
              <div className="bg-red-50 dark:bg-red-900/10 border border-red-200 dark:border-red-800 rounded-lg p-6 flex items-center justify-between">
                <div>
                  <h3 className="font-medium text-red-900 dark:text-red-300">Delete Application</h3>
                  <p className="text-sm text-red-700 dark:text-red-400 mt-1">
                    Permanently delete this application and all associated data. This action cannot be undone.
                  </p>
                </div>
                <Button
                  variant="danger"
                  onClick={() => setIsDeleteModalOpen(true)}
                  className="flex items-center gap-2"
                >
                  <Trash2 size={16} />
                  Delete Application
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
      
      {/* Usage Statistics Section (Owner Only - FR-023) */}
      {isOwner && <UsageSection appId={application.id} timePeriod={timePeriod} onTimePeriodChange={setTimePeriod} />}
      
      {/* Share Modal */}
      {isOwner && (
        <ShareApplicationModal
          isOpen={isShareModalOpen}
          onClose={() => setIsShareModalOpen(false)}
          applicationId={application.id}
          applicationName={application.name}
          onSuccess={handleShareSuccess}
        />
      )}

      {/* Delete Confirmation Modal */}
      {isDeleteModalOpen && application && (
        <Modal
          isOpen={isDeleteModalOpen}
          onClose={() => setIsDeleteModalOpen(false)}
          title="Delete Application"
        >
          <div className="space-y-4">
            <p className="text-gray-600 dark:text-gray-300">
              Are you sure you want to delete <strong>{application.name}</strong>? This action will permanently remove all logs, metrics, and configuration. This cannot be undone.
            </p>
            <div className="flex justify-end gap-3 mt-6">
              <Button variant="secondary" onClick={() => setIsDeleteModalOpen(false)}>Cancel</Button>
              <Button variant="danger" onClick={handleDeleteApplication} isLoading={isDeleting}>Delete Application</Button>
            </div>
          </div>
        </Modal>
      )}
        </div>
      </div>
    </div>
  );
}

// Usage Section Component (T215, T219)
function UsageSection({ 
  appId, 
  timePeriod, 
  onTimePeriodChange 
}: { 
  appId: string; 
  timePeriod: '7d' | '30d' | '90d'; 
  onTimePeriodChange: (period: '7d' | '30d' | '90d') => void;
}) {
  const { data: usage, isLoading, isError } = useApplicationUsage(appId);
  
  return (
    <div className="mt-6 space-y-6">
      <UsageStats usage={usage} isLoading={isLoading} isError={isError} />
      <UsageBreakdown 
        usage={usage} 
        isLoading={isLoading} 
        isError={isError} 
        timePeriod={timePeriod}
        onTimePeriodChange={onTimePeriodChange}
      />
    </div>
  );
}

export default ApplicationDetailsPage;
