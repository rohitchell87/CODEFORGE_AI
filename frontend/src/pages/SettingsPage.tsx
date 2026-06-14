import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Activity, Globe, Moon, Code2 } from 'lucide-react';
import { DashboardStats, getDashboardStats } from '../services/dashboard';
import * as authService from '../services/auth';

function SettingsPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    getDashboardStats().then((data) => {
      if (active) {
        setStats(data);
      }
    });

    return () => {
      active = false;
    };
  }, []);

  const displayName = `${user?.firstName || ''}${user?.firstName && user?.lastName ? ' ' : ''}${user?.lastName || ''}`.trim() || 'User';
  const firstLetter = displayName.charAt(0).toUpperCase();
  const codingMetrics = [
    { label: 'Problems Solved', value: stats?.solvedCount ?? 0 },
    { label: 'Submissions', value: stats?.totalSubmissions ?? 0 },
    { label: 'Acceptance Rate', value: `${stats?.accuracy ?? 0}%` },
    { label: 'Contests Joined', value: stats?.contests ?? 0 },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleDeleteAccount = async () => {
    if (!confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      return;
    }

    setDeleteLoading(true);
    setDeleteError(null);

    try {
      await authService.deleteAccount();
      logout();
      navigate('/login');
      window.alert('Your account has been deleted successfully.');
    } catch (err) {
      setDeleteError('Failed to delete account. Please try again.');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-black py-8 px-4 sm:px-6 md:px-8" style={{ paddingBottom: '80px' }}>
      <div style={{ width: 'calc(100% - 40px)', maxWidth: '1400px', margin: '0 auto' }} className="space-y-6">
        {/* Header Card */}
        <div className="border border-white/15 rounded-[16px] bg-black p-5">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <p className="text-[12px] uppercase tracking-[0.15em] text-[#A0A0A0] font-medium">Profile settings</p>
              <h1 className="mt-3 text-[32px] font-semibold text-white leading-tight">Account dashboard</h1>
              <p className="mt-2 text-[14px] text-[#A0A0A0] max-w-2xl">
                Centralize your CodeForge AI account controls, review live coding metrics, and keep your profile sharp.
              </p>
            </div>
            <div className="border border-white/15 rounded-[12px] bg-black px-3 py-2 text-center whitespace-nowrap">
              <p className="text-[11px] uppercase tracking-[0.15em] text-[#A0A0A0] font-medium">Member Status</p>
              <p className="mt-1 text-[14px] font-semibold text-white">Premium</p>
              <button
                onClick={handleDeleteAccount}
                disabled={deleteLoading}
                className="mt-2 w-full border border-red-600 rounded-[12px] bg-black px-2 py-1 text-[12px] font-medium text-red-600 transition hover:bg-red-950/5 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {deleteLoading ? 'Deleting...' : 'Delete Account'}
              </button>
            </div>
          </div>
        </div>

        {/* First Row: Profile & Statistics */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Profile Card */}
          <div className="border border-white/15 rounded-[16px] bg-black p-5">
            <p className="text-[12px] uppercase tracking-[0.15em] text-[#A0A0A0] font-medium">Profile</p>
            <div className="mt-4 flex flex-col sm:flex-row sm:items-center gap-4">
              <div className="flex h-16 w-16 items-center justify-center rounded-full border border-white/15 bg-black text-[20px] font-semibold text-white flex-shrink-0">
                {firstLetter}
              </div>
              <div className="flex-1">
                <h2 className="text-[20px] font-semibold text-white">{displayName}</h2>
                <div className="mt-2 space-y-1 text-[13px]">
                  <p className="text-[#A0A0A0]">{user?.email || 'N/A'}</p>
                  <p className="text-[#A0A0A0]">User ID: <span className="text-white">{user?.userId || 'N/A'}</span></p>
                </div>
              </div>
            </div>
          </div>

          {/* Statistics Card */}
          <div className="border border-white/15 rounded-[16px] bg-black p-5">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-[12px] uppercase tracking-[0.15em] text-[#A0A0A0] font-medium">Statistics</p>
                <h2 className="mt-2 text-[20px] font-semibold text-white">Coding summary</h2>
              </div>
              <div className="text-[11px] uppercase tracking-[0.15em] text-[#A0A0A0] font-medium">Live</div>
            </div>
            <div className="mt-4 grid grid-cols-2 gap-3">
              {codingMetrics.map((metric) => (
                <div key={metric.label} className="border border-white/15 rounded-[12px] bg-black p-3">
                  <p className="text-[11px] uppercase tracking-[0.15em] text-[#A0A0A0] font-medium">{metric.label}</p>
                  <p className="mt-2 text-[18px] font-semibold text-white">{metric.value}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Second Row: Account Info & Activity */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Account Information Card */}
          <div className="border border-white/15 rounded-[16px] bg-black p-5">
            <p className="text-[12px] uppercase tracking-[0.15em] text-[#A0A0A0] font-medium">Account information</p>
            <h2 className="mt-2 text-[20px] font-semibold text-white">Security & identity</h2>
            <div className="mt-4 space-y-2">
              {[
                { label: 'Email', value: user?.email || 'N/A' },
                { label: 'Role', value: user?.role || 'User' },
                { label: 'Account Status', value: 'Active' },
              ].map((item) => (
                <div key={item.label} className="flex items-center justify-between py-2 border-b border-white/8 last:border-b-0">
                  <p className="text-[13px] text-[#A0A0A0]">{item.label}</p>
                  <p className="text-[13px] font-medium text-white">{item.value}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Coding Activity Card */}
          <div className="border border-white/15 rounded-[16px] bg-black p-5">
            <p className="text-[12px] uppercase tracking-[0.15em] text-[#A0A0A0] font-medium">Coding activity</p>
            <h2 className="mt-2 text-[20px] font-semibold text-white">Recent wins</h2>
            <div className="mt-4 space-y-2">
              <div className="flex items-center gap-3 py-2 border-b border-white/8">
                <Activity className="h-4 w-4 text-[#A0A0A0] flex-shrink-0" />
                <p className="text-[13px] text-white">Improved accuracy this week</p>
              </div>
              <div className="flex items-center gap-3 py-2 border-b border-white/8">
                <Activity className="h-4 w-4 text-[#A0A0A0] flex-shrink-0" />
                <p className="text-[13px] text-white">Joined new contest</p>
              </div>
              <div className="flex items-center gap-3 py-2">
                <Activity className="h-4 w-4 text-[#A0A0A0] flex-shrink-0" />
                <p className="text-[13px] text-white">Solved latest challenge</p>
              </div>
            </div>
          </div>
        </div>

        {deleteError && (
          <div className="rounded-[16px] border border-red-600 bg-red-950/10 p-4 text-sm text-red-300">
            {deleteError}
          </div>
        )}

        {/* Danger zone removed: account deletion integrated into header card */}
      </div>
    </div>
  );
}

export default SettingsPage;
