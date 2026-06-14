import { useEffect, useMemo, useState } from 'react';
import { Route, Routes } from 'react-router-dom';
import { Area, AreaChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import NotFoundPage from './NotFoundPage';
import AiPage from './AiPage';
import ProblemsPage from './ProblemsPage';
import SubmissionsPage from './SubmissionsPage';
import ContestsPage from './ContestsPage';
import ContestDetailsPage from './ContestDetailsPage';
import SettingsPage from './SettingsPage';
import { getDashboardStats } from '../services/dashboard';
import type { DashboardStats } from '../services/dashboard';

function DashboardHome() {
  const [stats, setStats] = useState<DashboardStats | null>(null);

  useEffect(() => {
    let mounted = true;
    getDashboardStats().then((result) => {
      if (mounted) setStats(result);
    });
    return () => {
      mounted = false;
    };
  }, []);

  const cards = useMemo(
    () => [
      { label: 'Solved', value: stats?.solvedCount.toString() ?? '-', detail: 'Accepted submissions' },
      { label: 'Streak', value: stats?.streak.toString() ?? '-', detail: 'Daily solved streak' },
      { label: 'Accuracy', value: `${stats?.accuracy ?? '-'}%`, detail: 'Average pass rate' },
      { label: 'Contests', value: stats?.contests.toString() ?? '-', detail: 'Active contests' },
    ],
    [stats]
  );

  return (
    <div className="w-full">
      <div className="space-y-6 pb-12">
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          {cards.map((card) => (
            <div key={card.label} className="rounded-2xl border border-white/10 bg-black p-4">
              <p className="text-[11px] uppercase tracking-[0.35em] text-textSecondary">{card.label}</p>
              <p className="mt-3 text-3xl font-semibold text-textPrimary">{card.value}</p>
              <p className="mt-2 text-xs text-textSecondary">{card.detail}</p>
            </div>
          ))}
        </div>

      <div className="grid gap-4 xl:grid-cols-[1.4fr_0.9fr]">
        <div className="rounded-2xl border border-white/10 bg-black p-4">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-xs uppercase tracking-[0.35em] text-textSecondary">Trend</p>
              <h2 className="mt-2 text-xl font-semibold text-textPrimary">Solved streak</h2>
            </div>
            <span className="rounded-full border border-white/10 px-3 py-1 text-[11px] uppercase tracking-[0.35em] text-textSecondary">Live</span>
          </div>

          <div className="mt-4 h-44">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={stats?.solvedTrend ?? []} margin={{ top: 4, right: 8, left: -14, bottom: 4 }}>
                <defs>
                  <linearGradient id="colorSolved" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#FFFFFF" stopOpacity={0.22} />
                    <stop offset="95%" stopColor="#FFFFFF" stopOpacity={0.05} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fill: '#FFFFFF', fontSize: 11 }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#FFFFFF', fontSize: 11 }} />
                <Tooltip contentStyle={{ backgroundColor: '#000000', border: '1px solid rgba(255,255,255,0.08)', borderRadius: 12 }} labelStyle={{ color: '#FFFFFF' }} itemStyle={{ color: '#FFFFFF' }} />
                <Area type="monotone" dataKey="solved" stroke="#FFFFFF" fill="url(#colorSolved)" strokeWidth={2} dot={false} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="space-y-3">
          <div className="rounded-2xl border border-white/10 bg-black p-4">
            <p className="text-xs uppercase tracking-[0.35em] text-textSecondary">Quality</p>
            <h3 className="mt-2 text-lg font-semibold text-textPrimary">Maintain a steady workflow</h3>
            <p className="mt-2 text-sm text-textSecondary">Review failed submissions and prioritize edge-case coverage for consistent interview readiness.</p>
          </div>
          <div className="rounded-2xl border border-white/10 bg-black p-4">
            <p className="text-xs uppercase tracking-[0.35em] text-textSecondary">Focus</p>
            <h3 className="mt-2 text-lg font-semibold text-textPrimary">Compact, developer-first layout</h3>
            <p className="mt-2 text-sm text-textSecondary">Everything in one view: metrics, contests, and coding workflow.</p>
          </div>
        </div>
      </div>

      <div className="rounded-2xl border border-white/10 bg-black p-4">
        <p className="text-xs uppercase tracking-[0.35em] text-textSecondary">Recent Activity</p>
        <div className="mt-3 space-y-2">
          {stats?.recentActivity.map((activity) => (
            <div key={activity.label} className="flex items-center justify-between rounded-xl border border-white/10 px-3 py-3 text-sm text-textSecondary">
              <div>
                <p className="font-semibold text-textPrimary">{activity.label}</p>
                <p className="text-xs text-textSecondary">Platform activity snapshot</p>
              </div>
              <span className="text-base font-semibold text-textPrimary">{activity.value}</span>
            </div>
          ))}
        </div>
      </div>
      </div>
    </div>
  );
}

function DashboardPage() {
  return (
    <div className="h-screen bg-black text-textPrimary flex flex-col">
      <Navbar />
      <div className="flex flex-1 w-full gap-4 px-4 py-4 sm:px-6 lg:px-8 xl:gap-6 overflow-hidden">
        <Sidebar />
        <main className="flex-1 min-w-0 overflow-y-auto">
          <Routes>
            <Route path="/" element={<DashboardHome />} />
            <Route path="ai" element={<AiPage />} />
            <Route path="problems" element={<ProblemsPage />} />
            <Route path="problems/:problemId" element={<ProblemsPage />} />
            <Route path="submissions" element={<SubmissionsPage />} />
            <Route path="contests" element={<ContestsPage />} />
            <Route path="contests/:contestId" element={<ContestDetailsPage />} />
            <Route path="settings" element={<SettingsPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

export default DashboardPage;
