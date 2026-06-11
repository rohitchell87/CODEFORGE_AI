import { useState, ChangeEvent, FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuth } from '../context/AuthContext';

function SignupPage() {
  const navigate = useNavigate();
  const { signup, loading, error } = useAuth();
  const [formState, setFormState] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [formError, setFormError] = useState('');

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFormState({ ...formState, [event.target.name]: event.target.value });
  };

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
    });

    try {
      await signup(formState);
      navigate('/dashboard', { replace: true });
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
            <p className="text-xs uppercase tracking-[0.3em] text-textSecondary">Join the platform</p>
            <h1 className="text-4xl font-semibold text-white">Create your interview profile</h1>
            <p className="max-w-xl text-sm leading-7 text-textSecondary">
              Build a persistent account with access to curated problems, contest analytics, and AI-powered coding support.
            </p>
          </div>
          <div className="grid gap-3 rounded-2xl border border-white/10 bg-black p-6 text-sm text-textSecondary">
            <div className="rounded-2xl border border-white/10 bg-black p-4">
              <p className="font-medium text-white">Real interview flow</p>
              <p className="mt-2 text-xs">Save progress, submissions, and problem history across sessions.</p>
            </div>
            <div className="rounded-2xl border border-white/10 bg-black p-4">
              <p className="font-medium text-white">Secure auth</p>
              <p className="mt-2 text-xs">JWT-backed login keeps your dashboard private and reliable.</p>
            </div>
          </div>
        </section>

        <section className="rounded-2xl border border-white/10 bg-black p-8">
          <div className="mb-6 space-y-2">
            <p className="text-xs uppercase tracking-[0.3em] text-textSecondary">Create account</p>
            <h2 className="text-3xl font-semibold text-white">Start your journey</h2>
            <p className="text-sm text-textSecondary">Signup for the full CodeForge AI experience.</p>
          </div>

          <form className="space-y-5" onSubmit={handleSubmit}>
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label className="mb-2 block text-sm font-medium text-textSecondary">First name</label>
                <input
                  name="firstName"
                  value={formState.firstName}
                  onChange={handleChange}
                  required
                  className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-textPrimary placeholder:text-textSecondary transition focus:border-white focus:ring-2 focus:ring-white/20"
                  placeholder="Jane"
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-textSecondary">Last name</label>
                <input
                  name="lastName"
                  value={formState.lastName}
                  onChange={handleChange}
                  required
                  className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-textPrimary placeholder:text-textSecondary transition focus:border-white focus:ring-2 focus:ring-white/20"
                  placeholder="Doe"
                />
              </div>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-textSecondary">Email</label>
              <input
                type="email"
                name="email"
                value={formState.email}
                onChange={handleChange}
                required
                className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-textPrimary placeholder:text-textSecondary transition focus:border-white focus:ring-2 focus:ring-white/20"
                placeholder="you@codeforge.ai"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-textSecondary">Password</label>
              <input
                type="password"
                name="password"
                value={formState.password}
                onChange={handleChange}
                required
                className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-textPrimary placeholder:text-textSecondary transition focus:border-white focus:ring-2 focus:ring-white/20"
                placeholder="Create a strong password"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-textSecondary">Confirm password</label>
              <input
                type="password"
                name="confirmPassword"
                value={formState.confirmPassword}
                onChange={handleChange}
                required
                className="w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-textPrimary placeholder:text-textSecondary transition focus:border-white focus:ring-2 focus:ring-white/20"
                placeholder="Repeat your password"
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
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-textSecondary">
            Already have an account?{' '}
            <Link to="/login" className="text-white transition hover:text-white">
              Login
            </Link>
          </p>
        </section>
      </motion.div>
    </div>
  );
}

export default SignupPage;
