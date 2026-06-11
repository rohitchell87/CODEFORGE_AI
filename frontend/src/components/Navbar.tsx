import { Link, useNavigate } from 'react-router-dom';
import { LogOut, Code2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-40 border-b border-white/10 bg-black">
      <div className="flex w-full items-center justify-between gap-4 px-4 py-3 sm:px-6 lg:px-8">
        <Link to="/dashboard" className="inline-flex items-center gap-2 text-sm font-semibold uppercase tracking-[0.22em] text-textPrimary">
          <Code2 size={18} className="text-white" />
          CodeForge AI
        </Link>

        <div className="flex items-center gap-3 text-sm">
          {user ? (
            <>
              <div className="hidden items-center gap-2 rounded-full px-3 py-1 text-textSecondary sm:flex">
                <span className="inline-flex h-7 w-7 items-center justify-center rounded-full bg-black text-xs font-semibold text-textPrimary">
                  {user.firstName?.charAt(0) ?? 'A'}
                </span>
                <span>{user.firstName || 'Coder'}</span>
              </div>
              <button
                type="button"
                onClick={onLogout}
                className="inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-semibold text-textSecondary transition hover:text-textPrimary hover:bg-white/5"
              >
                <LogOut size={14} />
                Logout
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="rounded-full px-3 py-1.5 text-xs font-semibold text-white transition hover:text-white"
            >
              Login
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}

export default Navbar;
