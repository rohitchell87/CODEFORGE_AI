import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { AlertCircle } from 'lucide-react';

function SettingsPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const handleDeleteAccount = async () => {
    if (!confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      return;
    }

    setDeleteLoading(true);
    setDeleteError(null);

    try {
      // TODO: Call the delete account API endpoint
      // await deleteAccount();
      // navigate('/login');
      console.log('Delete account functionality to be implemented');
    } catch (err) {
      setDeleteError('Failed to delete account. Please try again.');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className="h-full overflow-y-auto flex flex-col">
      <div className="flex-1">
        <div className="space-y-8 max-w-2xl">
          {/* Header */}
          <div className="pt-8">
            <h1 className="text-3xl font-semibold text-white">Settings</h1>
            <p className="mt-2 text-sm text-textSecondary">Manage your account and preferences</p>
          </div>

          {/* Account Information Section */}
          <div className="space-y-6">
            <div className="space-y-4">
              {/* User ID */}
              <div className="border-b border-white/10 pb-6">
                <label className="block text-xs uppercase tracking-[0.25em] text-textSecondary mb-2">User ID</label>
                <div className="text-white font-mono text-sm">{user?.userId || 'N/A'}</div>
              </div>

              {/* Email */}
              <div className="border-b border-white/10 pb-6">
                <label className="block text-xs uppercase tracking-[0.25em] text-textSecondary mb-2">Email</label>
                <div className="text-white text-sm">{user?.email || 'N/A'}</div>
              </div>

              {/* Phone Number */}
              <div className="border-b border-white/10 pb-6">
                <label className="block text-xs uppercase tracking-[0.25em] text-textSecondary mb-2">Phone Number</label>
                <div className="text-white text-sm">Not provided</div>
              </div>
            </div>
          </div>

          {/* Delete Account Section */}
          <div className="space-y-4 py-6">
            <div className="flex items-center gap-3 text-sm text-textSecondary mb-4">
              <AlertCircle size={16} />
              <span>Danger Zone</span>
            </div>
            
            {deleteError && (
              <div className="border border-white/10 bg-white/3 rounded-2xl p-4 text-sm text-white">
                {deleteError}
              </div>
            )}

            <button
              onClick={handleDeleteAccount}
              disabled={deleteLoading}
              className="w-full border border-white/10 rounded-2xl px-6 py-3 text-sm font-semibold text-white transition hover:bg-white/5 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {deleteLoading ? 'Deleting...' : 'Delete Account'}
            </button>
            <p className="text-xs text-textSecondary">Permanently delete your account and all associated data.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SettingsPage;
