import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Clock3, Trophy } from 'lucide-react';
import { getContest, getContestLeaderboard } from '../services/contestService';
import { useAuth } from '../context/AuthContext';
import type { ContestSummary, ContestProblem } from '../types/contest';

function ContestDetailsPage() {
  const { contestId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [contest, setContest] = useState<ContestSummary | null>(null);
  const [problems, setProblems] = useState<ContestProblem[]>([]);
  const [leaderboard, setLeaderboard] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [timeStatus, setTimeStatus] = useState<string>('');
  const [timeLabel, setTimeLabel] = useState<string>('');

  const formatDuration = (durationMs: number) => {
    const totalSeconds = Math.max(0, Math.floor(durationMs / 1000));
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  const updateTimeInfo = (currentContest: ContestSummary) => {
    const now = new Date();
    const start = new Date(currentContest.startTime);
    const end = new Date(currentContest.endTime);

    if (now < start) {
      setTimeStatus('Upcoming');
      setTimeLabel(`Contest starts in ${formatDuration(start.getTime() - now.getTime())}`);
      return;
    }

    if (now <= end) {
      setTimeStatus('Running');
      setTimeLabel(`Time Remaining ${formatDuration(end.getTime() - now.getTime())}`);
      return;
    }

    setTimeStatus('Finished');
    setTimeLabel('Contest Finished');
  };

  const loadLeaderboard = async (contestIdNumber: number) => {
    try {
      const lb = await getContestLeaderboard(contestIdNumber, 0, 20);
      setLeaderboard(lb.content ?? []);
    } catch (e) {
      console.error('Failed to load leaderboard', e);
    }
  };

  useEffect(() => {
    const id = Number(contestId);
    if (!id) return;

    const load = async () => {
      try {
        const data = await getContest(id);
        setContest(data);
        setProblems(data.problems ?? []);
        updateTimeInfo(data);
        await loadLeaderboard(id);
      } catch (e) {
        setError('Failed to load contest');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [contestId]);

  useEffect(() => {
    if (!contest) return;
    updateTimeInfo(contest);
    const timer = window.setInterval(() => updateTimeInfo(contest), 1000);
    return () => window.clearInterval(timer);
  }, [contest]);

  useEffect(() => {
    const id = Number(contestId);
    if (!id) return;
    const refresh = window.setInterval(() => loadLeaderboard(id), 15000);
    return () => window.clearInterval(refresh);
  }, [contestId]);

  if (loading) return <div className="p-6">Loading contest...</div>;
  if (error || !contest) return <div className="p-6 text-rose-300">{error ?? 'Contest not found'}</div>;

  return (
    <div className="w-full">
      <div className="space-y-8 px-4 py-5">
        <section className="rounded-2xl border border-white/10 bg-black p-8">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm uppercase tracking-[0.25em] text-textSecondary">Contest</p>
              <h1 className="mt-2 text-2xl font-semibold text-textPrimary">{contest.title}</h1>
            </div>
            <div className="inline-flex items-center gap-2 rounded-2xl border border-white/10 bg-black px-4 py-3 text-sm text-textSecondary">
              <Trophy size={18} />
              {contest.participantCount ?? 0} participants
            </div>
          </div>
          <p className="mt-4 text-sm text-textSecondary">{contest.description}</p>
        </section>

        <section className="grid gap-6 xl:grid-cols-3">
          <div className="rounded-2xl border border-white/10 bg-black p-6">
            <h3 className="text-lg font-semibold text-textPrimary">Details</h3>
            <div className="mt-4 text-sm text-textSecondary space-y-2">
              <div className="flex items-center gap-2"><Clock3 size={16} /> Status: <span className="text-white">{timeStatus}</span></div>
              <div className="flex items-center gap-2"><Clock3 size={16} /> {timeLabel}</div>
              <div>Duration: <span className="text-white">{contest.durationInMinutes} minutes</span></div>
              <div>Start: <span className="text-white">{new Date(contest.startTime).toLocaleString()}</span></div>
              <div>End: <span className="text-white">{new Date(contest.endTime).toLocaleString()}</span></div>
            </div>
          </div>

          <div className="rounded-2xl border border-white/10 bg-black p-6 xl:col-span-2">
            <h3 className="text-lg font-semibold text-textPrimary">Problems</h3>
            <div className="mt-4 space-y-4">
              {problems.map((p) => (
                <div key={p.id} className="flex items-center justify-between rounded-xl border border-white/10 p-4">
                  <div>
                    <div className="font-semibold text-textPrimary">{p.title}</div>
                    <div className="text-sm text-textSecondary">{p.difficulty} • {p.points} pts</div>
                  </div>
                  <div>
                    <button
                      onClick={() => navigate(`/dashboard/problems/${p.problemId}`)}
                      className="inline-flex items-center gap-2 rounded-2xl border border-white/10 px-4 py-2 text-sm font-semibold"
                    >
                      Solve
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="rounded-2xl border border-white/10 bg-black p-6">
          <div className="flex items-center justify-between gap-4">
            <h3 className="text-lg font-semibold text-textPrimary">Leaderboard</h3>
            <div className="text-sm text-textSecondary">Refreshes every 15 seconds</div>
          </div>
          <div className="mt-4">
            {leaderboard.length === 0 ? (
              <div className="text-sm text-textSecondary">No participants yet.</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[600px] table-auto text-sm text-textSecondary">
                  <thead>
                    <tr>
                      <th className="text-left">Rank</th>
                      <th className="text-left">User</th>
                      <th className="text-center">Solved</th>
                      <th className="text-center">Score</th>
                    </tr>
                  </thead>
                  <tbody>
                    {leaderboard.map((row, idx) => {
                      const isCurrentUser = user?.userId && row.userId === user.userId;
                      return (
                        <tr
                          key={idx}
                          className={`border-t border-white/5 ${isCurrentUser ? 'bg-white/5' : ''}`}
                        >
                          <td className="py-2">{row.rank ?? idx + 1}</td>
                          <td className="py-2">{row.userFirstName} {row.userLastName}</td>
                          <td className="text-center">{row.problemsSolved ?? 0}</td>
                          <td className="text-center">{row.score ?? 0}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}

export default ContestDetailsPage;
