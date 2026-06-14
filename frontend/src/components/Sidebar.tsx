import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Zap, BookOpen, ClipboardList, Trophy, Settings } from 'lucide-react';

const links = [
  { label: 'Overview', to: '/dashboard', icon: LayoutDashboard },
  { label: 'AI Hints', to: '/dashboard/ai', icon: Zap },
  { label: 'Problems', to: '/dashboard/problems', icon: BookOpen },
  { label: 'Submissions', to: '/dashboard/submissions', icon: ClipboardList },
  { label: 'Contests', to: '/dashboard/contests', icon: Trophy },
];

function Sidebar() {
  return (
    <aside className="hidden h-full shrink-0 border-r border-white/10 bg-black lg:flex lg:w-56 lg:flex-col px-2 py-4 overflow-hidden flex-col">
      <div className="px-2 pb-4">
        <p className="text-xs uppercase tracking-widest text-textSecondary">Workspace</p>
      </div>

      <nav className="sidebar-nav space-y-2 px-1 overflow-y-auto flex-1">
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

      <div className="sidebar-footer flex flex-col gap-3 px-1 pt-4 border-t border-white/10 pb-6 flex-shrink-0">
        <button
          onClick={() => window.dispatchEvent(new Event('backToProblems'))}
          className={`flex items-center gap-3 rounded-xl px-3 py-2 text-sm transition border border-white/10 text-textSecondary hover:text-textPrimary hover:bg-white/5`}
        >
          <BookOpen size={16} className="flex-shrink-0" />
          <span className="truncate">Back To Problems</span>
        </button>

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
