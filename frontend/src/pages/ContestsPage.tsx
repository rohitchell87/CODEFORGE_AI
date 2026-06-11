import { Trophy, Clock3, TrendingUp, Users } from 'lucide-react';

const contests = [
  { title: 'CodeForge Sprint', date: 'May 26', duration: '2h', participants: 498, status: 'Open' },
  { title: 'AI Algorithm Cup', date: 'June 5', duration: '3h', participants: 712, status: 'Register' },
  { title: 'Night Coder Challenge', date: 'June 18', duration: '2h 30m', participants: 386, status: 'Open' },
];

function ContestsPage() {
  return (
    <div className="h-full overflow-hidden">
      <div className="h-full overflow-y-auto space-y-8 px-4 py-5">
        <section className="rounded-2xl border border-white/10 bg-black p-8">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm uppercase tracking-[0.25em] text-textSecondary">Contests</p>
            <h1 className="mt-3 text-3xl font-semibold text-textPrimary">Compete in coding events</h1>
          </div>
          <div className="inline-flex items-center gap-2 rounded-2xl border border-white/10 bg-black px-4 py-3 text-sm text-textSecondary">
            <Trophy size={18} className="text-textSecondary" />
            Upcoming contests and leaderboards.
          </div>
        </div>
        <p className="mt-4 max-w-2xl text-sm text-textSecondary">
          Join scheduled contests, track rating movement, and compare performance against the community.
        </p>
      </section>

      <section className="grid gap-6 xl:grid-cols-3">
        {contests.map((contest) => (
          <div key={contest.title} className="rounded-2xl border border-white/10 bg-black p-6">
            <div className="flex items-center justify-between gap-4">
              <h2 className="text-xl font-semibold text-textPrimary">{contest.title}</h2>
              <span className="rounded-full border border-white/10 px-3 py-1 text-[11px] uppercase tracking-[0.25em] text-textSecondary">{contest.status}</span>
            </div>
            <p className="mt-3 text-sm text-textSecondary">Starts on {contest.date}</p>
            <div className="mt-6 grid gap-3 text-sm text-textSecondary">
              <div className="flex items-center gap-2">
                <Clock3 size={16} />
                {contest.duration}
              </div>
              <div className="flex items-center gap-2">
                <Users size={16} />
                {contest.participants} participants
              </div>
              <div className="flex items-center gap-2">
                <TrendingUp size={16} />
                Performance-based leaderboard
              </div>
            </div>
            <button type="button" className="mt-6 inline-flex w-full items-center justify-center rounded-2xl bg-black border border-white/10 px-5 py-3 text-sm font-semibold text-white transition hover:bg-white/5">
              Join contest
            </button>
          </div>
        ))}
      </section>
      </div>
    </div>
  );
}

export default ContestsPage;
