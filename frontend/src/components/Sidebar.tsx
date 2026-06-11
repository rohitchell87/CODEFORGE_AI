import { NavLink } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { LayoutDashboard, Zap, BookOpen, ClipboardList, Trophy, Settings } from 'lucide-react';

const links = [
  { label: 'Overview', to: '/dashboard', icon: LayoutDashboard },
  { label: 'AI Hints', to: '/dashboard/ai', icon: Zap },
  { label: 'Problems', to: '/dashboard/problems', icon: BookOpen },
  { label: 'Submissions', to: '/dashboard/submissions', icon: ClipboardList },
  { label: 'Contests', to: '/dashboard/contests', icon: Trophy },
];

function Sidebar() {
  const [showBack, setShowBack] = useState(false);

  useEffect(() => {
    const listener = (e: any) => {
      const id = e?.detail?.selectedId ?? null;
      setShowBack(!!id);
    };
    window.addEventListener('problemSelectionChange', listener as EventListener);
    return () => window.removeEventListener('problemSelectionChange', listener as EventListener);
  }, []);

  return (
    <aside className="hidden h-full shrink-0 border-r border-white/10 bg-black lg:flex lg:w-56 lg:flex-col px-2 py-4 overflow-hidden">
      <div>
        <div className="px-2 pb-4">
          <p className="text-xs uppercase tracking-widest text-textSecondary">Workspace</p>
        </div>

        <nav className="space-y-2 px-1">
          {links.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-3 py-2 text-sm transition ${
                    isActive
                      ? 'border-l-4 border-white/10 bg-white/3 text-textPrimary'
                      : 'text-textSecondary hover:text-textPrimary hover:bg-white/5'
                  }`
                }
              >
                <Icon size={16} className="flex-shrink-0" />
                <span className="truncate">{item.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </div>

      <div className="mt-auto flex flex-col gap-3 px-1">
        {showBack && (
          <button
            onClick={() => window.dispatchEvent(new Event('backToProblems'))}
            className={`flex items-center gap-3 rounded-xl px-3 py-2 text-sm transition border border-white/10 text-textSecondary hover:text-textPrimary hover:bg-white/5`}
          >
            <BookOpen size={16} className="flex-shrink-0" />
            <span className="truncate">Back To Problems</span>
          </button>
        )}

        <NavLink
          to="/dashboard/settings"
          className={({ isActive }) =>
            `flex items-center gap-3 rounded-xl px-3 py-2 text-sm transition border border-white/10 ${
              isActive
                ? 'bg-white/3 text-textPrimary'
                : 'text-textSecondary hover:text-textPrimary hover:bg-white/5'
            }`
          }
        >
          <Settings size={16} className="flex-shrink-0" />
          <span className="truncate">Settings</span>
        </NavLink>
      </div>
    </aside>
  );
}

export default Sidebar;
