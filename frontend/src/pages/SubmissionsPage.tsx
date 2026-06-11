import { useEffect, useState } from 'react';
import { CheckCircle2, Clock3, Activity } from 'lucide-react';
import { fetchUserSubmissions } from '../services/submissionService';
import type { SubmissionRecord } from '../types/problem';

console.log('SUBMISSIONS PAGE VERSION 2 - LATEST CODE LOADED');

function SubmissionsPage() {
  const [submissions, setSubmissions] = useState<SubmissionRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadSubmissions = async () => {
      try {
        console.log('SUBMISSIONS PAGE: Starting load...');
        const page = await fetchUserSubmissions(0, 20);
        console.log('SUBMISSIONS PAGE: Got page:', page);
        if (!page) {
          console.error('SUBMISSIONS PAGE: Page is undefined/null');
          setError('No submissions data returned from server.');
          setLoading(false);
          return;
        }
        setSubmissions(page.content || []);
        console.log('SUBMISSIONS PAGE: Set submissions:', page.content?.length ?? 0);
      } catch (err: any) {
        console.error('SUBMISSIONS PAGE: Error caught:', {
          message: err?.message,
          status: err?.response?.status,
          statusText: err?.response?.statusText,
          data: err?.response?.data,
          stack: err?.stack,
        });
        setError(`Unable to load submissions: ${err?.response?.status || err?.message || 'Unknown error'}`);
      } finally {
        setLoading(false);
      }
    };

    loadSubmissions();
  }, []);

  return (
    <div className="h-full overflow-hidden">
      <div className="h-full overflow-y-auto space-y-8 px-4 py-5">
        <section className="rounded-2xl border border-white/10 bg-black p-8">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-sm uppercase tracking-[0.3em] text-textSecondary">Submission history</p>
              <h1 className="mt-3 text-4xl font-semibold text-white">Track your code performance</h1>
            </div>
            <div className="inline-flex items-center gap-2 rounded-2xl border border-white/10 bg-black px-4 py-3 text-sm text-textSecondary">
              <Activity size={18} />
              Analyze runtime, memory, and success trends.
            </div>
          </div>
          <p className="mt-4 max-w-2xl text-sm text-textSecondary">
            Keep a record of your most recent submissions and learn from rejected attempts.
          </p>
        </section>

        <section className="overflow-hidden rounded-2xl border border-white/10 bg-black">
          <div className="grid gap-4 p-6 md:grid-cols-[1.4fr_0.8fr]">
            <div>
              <h2 className="text-xl font-semibold text-white">Recent submissions</h2>
              <p className="mt-2 text-sm text-textSecondary">Review the latest runs and evaluate your progress.</p>
            </div>
            <div className="inline-flex items-center justify-between rounded-2xl border border-white/10 bg-black p-4 text-sm text-textSecondary">
              <div className="flex items-center gap-2">
                <CheckCircle2 size={18} className="text-white" />
                Accepted rate
              </div>
              <span className="font-semibold text-white">{submissions.length ? `${Math.round((submissions.filter((item) => item.executionStatus === 'Accepted').length / submissions.length) * 100)}%` : '—'}</span>
            </div>
          </div>

          <div className="overflow-x-auto px-6 pb-6">
            {loading ? (
              <p className="py-8 text-sm text-textSecondary">Loading submissions...</p>
            ) : error ? (
              <p className="py-8 text-sm text-red-400">{error}</p>
            ) : submissions.length === 0 ? (
              <p className="py-8 text-sm text-textSecondary">No submissions yet. Submit code on a problem to see the history.</p>
            ) : (
              <table className="min-w-full border-separate border-spacing-y-3 text-left">
                <thead>
                  <tr className="text-sm uppercase tracking-[0.25em] text-textSecondary">
                    <th className="px-4 py-3">Submission</th>
                    <th className="px-4 py-3">Problem</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Runtime</th>
                    <th className="px-4 py-3">Memory</th>
                    <th className="px-4 py-3">When</th>
                  </tr>
                </thead>
                <tbody>
                  {submissions.map((submission) => (
                    <tr key={submission.id} className="rounded-2xl bg-black text-sm text-textSecondary">
                      <td className="px-4 py-4 font-medium text-white">#{submission.id}</td>
                      <td className="px-4 py-4">Problem {submission.problemId}</td>
                      <td className="px-4 py-4">
                        <span className="inline-flex rounded-full bg-white/5 px-3 py-1 text-xs font-semibold text-white">
                          {submission.executionStatus ?? 'Unknown'}
                        </span>
                      </td>
                      <td className="px-4 py-4">{submission.executionTime != null ? `${submission.executionTime.toFixed(2)} s` : 'N/A'}</td>
                      <td className="px-4 py-4">{submission.memoryUsage != null ? `${submission.memoryUsage.toFixed(0)} KB` : 'N/A'}</td>
                      <td className="px-4 py-4">{new Date(submission.submittedAt).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}

export default SubmissionsPage;
