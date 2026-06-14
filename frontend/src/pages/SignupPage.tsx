import { useState, useEffect, ChangeEvent, FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuth } from '../context/AuthContext';

function SignupPage() {
  const navigate = useNavigate();
  const { signup, loading, error, clearError } = useAuth();
  const [formState, setFormState] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    securityQuestion: 'What is your favourite book?',
    securityAnswer: '',
  });
  const [formError, setFormError] = useState('');

  const handleChange = (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormState({ ...formState, [event.target.name]: event.target.value });
  };

  useEffect(() => {
    clearError();
    setFormError('');
  }, [clearError]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFormError('');

    if (formState.password !== formState.confirmPassword) {
      setFormError('Passwords do not match.');
      return;
    }

    console.log('Signup form payload', {
      firstName: formState.firstName,
      lastName: formState.lastName,
      email: formState.email,
      password: formState.password,
      confirmPassword: formState.confirmPassword,
      securityAnswer: formState.securityAnswer,
    });

    try {
      await signup(formState);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setFormError((err as Error).message);
    }
  };

  return (
    <div className="min-h-screen bg-black px-4 py-10 sm:px-6 lg:px-8 overflow-y-auto">
      <motion.div
        initial={{ opacity: 0, y: 32 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.36, ease: 'easeOut' }}
        className="mx-auto flex max-w-[1260px] flex-col min-h-0 rounded-[28px] border border-white/10 bg-black lg:flex-row lg:items-stretch"
      >
        <section className="flex-1 min-h-0 border-r border-white/10 bg-black px-10 py-10 sm:px-12 lg:px-14 lg:py-14 overflow-y-auto">
          <div className="mb-8 max-w-xl space-y-4">
            <p className="text-xs uppercase tracking-[0.3em] text-white/60">Join CodeForge AI</p>
            <h1 className="text-4xl font-semibold tracking-tight text-white">Persistent learning path</h1>
            <p className="max-w-lg text-sm leading-7 text-white/70">
              Save your progress, track contest analytics, and keep a sharp record of every solution.
            </p>
          </div>

          <div className="grid gap-4 text-sm text-white/70">
            <div className="rounded-[24px] border border-white/10 bg-black p-4">
              <p className="font-semibold text-white">Persistent learning path</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Keep submissions, stats, and problem history neatly organized.</p>
            </div>
            <div className="rounded-[24px] border border-white/10 bg-black p-4">
              <p className="font-semibold text-white">Secure authentication</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Recover your account with a security question and protected credentials.</p>
            </div>
            <div className="rounded-[24px] border border-white/10 bg-black p-4">
              <p className="font-semibold text-white">Contests and analytics</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Join competitions and measure your growth with clear performance metrics.</p>
            </div>
            <div className="rounded-[24px] border border-white/10 bg-black p-4">
              <p className="font-semibold text-white">AI-Powered Hints</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Get intelligent hints to debug faster and optimize your approach.</p>
            </div>
            <div className="rounded-[24px] border border-white/10 bg-black p-4">
              <p className="font-semibold text-white">Smart progress tracking</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Visualize your journey with detailed progress and streak insights.</p>
            </div>
            <div className="rounded-[24px] border border-white/10 bg-black p-4">
              <p className="font-semibold text-white">Cross-device sync</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Access your problems, solutions, and stats seamlessly across devices.</p>
            </div>
          </div>
        </section>

        <section className="flex-1 min-h-0 bg-black px-10 py-10 sm:px-12 lg:px-14 lg:py-14 overflow-y-auto">
          <div className="mx-auto max-w-xl">
            <div className="mb-7 space-y-2">
              <p className="text-xs uppercase tracking-[0.3em] text-white/60">Create account</p>
              <h2 className="text-3xl font-semibold text-white">Start your journey</h2>
              <p className="text-sm leading-6 text-white/70">Signup for the full CodeForge AI experience.</p>
            </div>

            <form className="space-y-5" onSubmit={handleSubmit}>
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="mb-2 block text-sm font-medium text-white/70">First name</label>
                  <input
                    name="firstName"
                    value={formState.firstName}
                    onChange={handleChange}
                    required
                    className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                    placeholder="Jane"
                  />
                </div>
                <div>
                  <label className="mb-2 block text-sm font-medium text-white/70">Last name</label>
                  <input
                    name="lastName"
                    value={formState.lastName}
                    onChange={handleChange}
                    required
                    className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                    placeholder="Doe"
                  />
                </div>
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-white/70">Email</label>
                <input
                  type="email"
                  name="email"
                  value={formState.email}
                  onChange={handleChange}
                  required
                  className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                  placeholder="you@codeforge.ai"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-white/70">Password</label>
                <input
                  type="password"
                  name="password"
                  value={formState.password}
                  onChange={handleChange}
                  required
                  className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                  placeholder="Create a strong password"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-white/70">Confirm password</label>
                <input
                  type="password"
                  name="confirmPassword"
                  value={formState.confirmPassword}
                  onChange={handleChange}
                  required
                  className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                  placeholder="Repeat your password"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-white/70">Security question</label>
                <select
                  name="securityQuestion"
                  value={formState.securityQuestion}
                  onChange={handleChange}
                  required
                  className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-white transition focus:border-white focus:ring-0"
                >
                  <option>What is your favourite book?</option>
                  <option>What was your first pet's name?</option>
                  <option>What was the name of your first school?</option>
                  <option>What is your mother's maiden name?</option>
                </select>
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium text-white/70">Security answer</label>
                <input
                  type="text"
                  name="securityAnswer"
                  value={formState.securityAnswer}
                  onChange={handleChange}
                  required
                  className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                  placeholder="Your answer"
                />
                <p className="mt-2 text-xs text-white/60">This answer is required to recover your password.</p>
              </div>

              {(formError || error) && (
                <div className="rounded-2xl border border-white/10 bg-white/5 px-4 py-3 text-sm text-white">{formError || error}</div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full rounded-2xl bg-white px-5 py-3 text-sm font-semibold text-black transition hover:bg-white/90 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {loading ? 'Creating account…' : 'Create account'}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-white/60">
              Already have an account?{' '}
              <Link to="/login" className="text-white transition hover:text-white/90">
                Login
              </Link>
            </p>
          </div>
        </section>
      </motion.div>
    </div>
  );
}

export default SignupPage;
