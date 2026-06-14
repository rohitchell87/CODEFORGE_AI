import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trophy, Clock3, TrendingUp, Users } from 'lucide-react';
import { getContests, joinContest } from '../services/contestService';
import type { ContestSummary } from '../types/contest';

function ContestsPage() {
  const [contests, setContests] = useState<ContestSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [joiningId, setJoiningId] = useState<number | null>(null);

  useEffect(() => {
    const fetchContests = async () => {
      try {
        const page = await getContests(0, 12);
        setContests(page.content);
      } catch (e) {
        setError('Unable to load contests at this time. Please try again later.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchContests();
  }, []);

  const navigate = useNavigate();

  const handleJoin = async (contestId: number) => {
    setJoiningId(contestId);
    try {
      await joinContest(contestId);
      // navigate to contest details page after successful join
      navigate(`/dashboard/contests/${contestId}`);
    } catch (e) {
      console.error('Failed to join contest', e);
      window.alert('Could not join the contest. Please sign in or try again later.');
    } finally {
      setJoiningId(null);
    }
  };

  return (
    <div className="w-full">
      <div className="space-y-8 px-4 py-5">
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
            Join scheduled contests, track progress, and compare your score with others in realtime.
          </p>
        </section>

        {isLoading ? (
          <div className="rounded-2xl border border-white/10 bg-black p-8 text-center text-textSecondary">Loading contests...</div>
        ) : error ? (
          <div className="rounded-2xl border border-rose-500/40 bg-black p-8 text-center text-rose-300">{error}</div>
        ) : contests.length === 0 ? (
          <div className="rounded-2xl border border-white/10 bg-black p-8 text-center text-textSecondary">No practice contests are available yet.</div>
        ) : (
          <section className="grid gap-6 xl:grid-cols-3">
            {contests.map((contest) => (
              <div key={contest.id} className="rounded-2xl border border-white/10 bg-black p-6">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <h2 className="text-xl font-semibold text-textPrimary">{contest.title}</h2>
                    <p className="mt-2 text-sm text-textSecondary line-clamp-3">{contest.description}</p>
                  </div>
                  <span className="rounded-full border border-white/10 px-3 py-1 text-[11px] uppercase tracking-[0.25em] text-textSecondary">
                    {contest.status}
                  </span>
                </div>
                <p className="mt-3 text-sm text-textSecondary">
                  Starts {new Date(contest.startTime).toLocaleString()}
                </p>
                <div className="mt-6 grid gap-3 text-sm text-textSecondary">
                  <div className="flex items-center gap-2">
                    <Clock3 size={16} />
                    {contest.durationInMinutes} minutes
                  </div>
                  <div className="flex items-center gap-2">
                    <Users size={16} />
                    {contest.participantCount ?? 0} participants
                  </div>
                  <div className="flex items-center gap-2">
                    <TrendingUp size={16} />
                    {contest.problemCount} problems
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => handleJoin(contest.id)}
                  disabled={joiningId === contest.id}
                  className="mt-6 inline-flex w-full items-center justify-center rounded-2xl bg-black border border-white/10 px-5 py-3 text-sm font-semibold text-white transition hover:bg-white/5 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {joiningId === contest.id ? 'Joining...' : 'Join contest'}
                </button>
              </div>
            ))}
          </section>
        )}
      </div>
    </div>
  );
}

export default ContestsPage;
