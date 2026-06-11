import { useState, FormEvent } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { AlertTriangle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, loading, error } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [formError, setFormError] = useState('');

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setFormError('');

    if (!email || !password) {
      setFormError('Please enter email and password to continue.');
      return;
    }

    try {
      await login({ email, password });
      navigate(from, { replace: true });
    } catch (err) {
      setFormError((err as Error).message);
    }
  };

  return (
    <div className="relative min-h-screen overflow-hidden px-4 py-10 sm:px-6 lg:px-8">
      <motion.div
        initial={{ opacity: 0, y: 32 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.36, ease: 'easeOut' }}
        className="relative mx-auto flex max-w-7xl flex-col gap-10 lg:flex-row lg:items-center"
      >
        <section className="rounded-2xl border border-white/10 bg-black p-10">
          <div className="mb-8 space-y-4">
            <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-black px-4 py-2 text-sm text-textSecondary">
              <AlertTriangle size={16} className="text-violet-300" />
              Secure interview prep, powered by AI
            </span>
            <h1 className="text-4xl font-semibold text-white">Welcome back to CodeForge AI</h1>
            <p className="max-w-xl text-sm leading-7 text-textSecondary">
              Sign in to access your problem dashboard, track contest progress, and get real-time AI hints.
            </p>
          </div>

          <div className="grid gap-3 rounded-2xl border border-white/10 bg-black p-6 text-sm text-textSecondary">
            <div className="space-y-1 rounded-2xl border border-white/10 bg-black p-4">
              <p className="font-medium text-white">Protected access</p>
              <p className="text-xs">JWT-backed sessions keep your analytics and history secure.</p>
            </div>
            <div className="space-y-1 rounded-2xl border border-white/10 bg-black p-4">
              <p className="font-medium text-white">AI-powered hints</p>
              <p className="text-xs">Ask the assistant for explanations, debugging guidance, and optimization tips.</p>
            </div>
          </div>
        </section>

        <section className="rounded-2xl border border-white/10 bg-black p-8">
          <div className="mb-6 space-y-2">
            <p className="text-xs uppercase tracking-[0.3em] text-textSecondary">Login</p>
            <h2 className="text-3xl font-semibold text-white">Access your AI workspace</h2>
            <p className="text-sm text-textSecondary">Secure sign-in with fast session persistence.</p>
          </div>

          <form className="space-y-5" onSubmit={handleSubmit}>
            <div>
              <label className="mb-2 block text-sm font-medium text-textSecondary">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-textPrimary placeholder:text-textSecondary transition focus:border-white focus:ring-2 focus:ring-white/20"
                placeholder="you@codeforge.ai"
              />
            </div>
            <div>
              <label className="mb-2 block text-sm font-medium text-textSecondary">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-textPrimary placeholder:text-textSecondary transition focus:border-white focus:ring-2 focus:ring-white/20"
                placeholder="••••••••"
              />
            </div>

            {(formError || error) && (
              <div className="rounded-2xl bg-white/5 px-4 py-3 text-sm text-white">{formError || error}</div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-2xl bg-white px-5 py-3 text-sm font-semibold text-black transition hover:bg-white/10 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {loading ? 'Signing in…' : 'Login'}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-textSecondary">
            New here?{' '}
            <Link to="/signup" className="text-white transition hover:text-white">
              Create an account
            </Link>
          </p>
        </section>
      </motion.div>
    </div>
  );
}

export default LoginPage;
