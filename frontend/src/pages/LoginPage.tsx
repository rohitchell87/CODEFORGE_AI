import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuth } from '../context/AuthContext';
import { resetPassword, startPasswordReset, verifySecurityAnswer } from '../services/auth';
import { PasswordResetStartRequest } from '../types/auth';

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, loading, error, clearError } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [formError, setFormError] = useState('');
  const [forgotOpen, setForgotOpen] = useState(false);
  const [forgotStep, setForgotStep] = useState<'start' | 'verify' | 'reset'>('start');
  const [forgotEmail, setForgotEmail] = useState('');
  const [forgotQuestion, setForgotQuestion] = useState('');
  const [forgotAnswer, setForgotAnswer] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [forgotError, setForgotError] = useState('');
  const [forgotSuccess, setForgotSuccess] = useState('');
  const [forgotLoading, setForgotLoading] = useState(false);

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/dashboard';

  useEffect(() => {
    clearError();
    setFormError('');
  }, [clearError]);

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
    } catch (err: any) {
      const status = err?.response?.status;
      
      if (status === 404) {
        setFormError('Account does not exist. Please create an account first.');
      } else if (status === 401) {
        setFormError('Invalid email or password.');
      } else if (status === 500) {
        setFormError('An internal server error occurred.');
      } else if (!err?.response) {
        setFormError('Unable to connect to the server.');
      } else {
        const responseData = err?.response?.data;
        const message = responseData?.message || responseData?.error || 'Unable to login. Please check your credentials.';
        setFormError(message);
      }
    }
  };

  const resetForgotState = () => {
    setForgotStep('start');
    setForgotEmail('');
    setForgotQuestion('');
    setForgotAnswer('');
    setNewPassword('');
    setConfirmNewPassword('');
    setForgotError('');
    setForgotSuccess('');
    setForgotLoading(false);
  };

  const handleForgotSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setForgotError('');
    setForgotSuccess('');
    setForgotLoading(true);

    try {
      if (forgotStep === 'start') {
        if (!forgotEmail) {
          setForgotError('Please enter your registered email address.');
          return;
        }

        const response = await startPasswordReset({ email: forgotEmail } as PasswordResetStartRequest);
        setForgotQuestion(response.securityQuestion);
        setForgotStep('verify');
        setForgotSuccess('Answer the security question to continue.');
      } else if (forgotStep === 'verify') {
        if (!forgotAnswer) {
          setForgotError('Please enter your security answer.');
          return;
        }

        await verifySecurityAnswer({ email: forgotEmail, securityAnswer: forgotAnswer });
        setForgotStep('reset');
        setForgotSuccess('Security answer confirmed. Set a new password below.');
      } else {
        if (!newPassword || !confirmNewPassword) {
          setForgotError('Please enter and confirm your new password.');
          return;
        }

        if (newPassword !== confirmNewPassword) {
          setForgotError('Passwords do not match.');
          return;
        }

        await resetPassword({
          email: forgotEmail,
          securityAnswer: forgotAnswer,
          newPassword,
          confirmPassword: confirmNewPassword,
        });

        setForgotOpen(false);
        setFormError('Password reset successful. Please sign in with your new password.');
        resetForgotState();
      }
    } catch (err: any) {
      const message = err?.response?.data?.message || err?.message || 'Failed to reset password';
      setForgotError(message);
    } finally {
      setForgotLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-black px-4 py-10 sm:px-6 lg:px-8">
      <div className="mx-auto grid min-h-[760px] max-w-[1260px] overflow-hidden rounded-[30px] border border-white/10 bg-black lg:grid-cols-[50%_50%]">
        <motion.section
          initial={{ opacity: 0, x: -24 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.32, ease: 'easeOut' }}
          className="flex flex-col gap-8 border-r border-white/10 bg-black px-8 py-10 lg:px-12 lg:py-12"
        >
          <div className="max-w-xl space-y-6">
            <p className="text-xs uppercase tracking-[0.35em] text-white/60">CodeForge AI</p>
            <h1 className="text-4xl font-semibold tracking-tight text-white">Welcome back to CodeForge AI</h1>
            <p className="max-w-lg text-sm leading-7 text-white/70">
              Sign in to continue solving problems and competing in intelligent contests with secure, high-performance code execution.
            </p>
          </div>

          <div className="grid gap-4">
            <div className="rounded-[24px] border border-white/10 bg-[#111111] p-4">
              <p className="text-sm font-semibold text-white">Persistent Learning Path</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Keep submissions, stats, and problem history neatly organized.</p>
            </div>
            <div className="rounded-[24px] border border-white/10 bg-[#111111] p-4">
              <p className="text-sm font-semibold text-white">Secure Authentication</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Recover your account with a security question and protected credentials.</p>
            </div>
            <div className="rounded-[24px] border border-white/10 bg-[#111111] p-4">
              <p className="text-sm font-semibold text-white">Contests and Analytics</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Join competitions and measure your growth with clear performance metrics.</p>
            </div>
            <div className="rounded-[24px] border border-white/10 bg-[#111111] p-4">
              <p className="text-sm font-semibold text-white">AI-Powered Hints</p>
              <p className="mt-2 text-xs leading-5 text-white/60">Get intelligent hints to debug faster and optimize your approach.</p>
            </div>
          </div>

          <div className="mt-auto flex flex-col gap-4 text-sm text-white/70">
            <p className="font-medium text-white">Premium, clean, and focused.</p>
            <p>CodeForge AI gives you a polished coding workspace designed for serious practice and contest readiness.</p>
          </div>
        </motion.section>

        <motion.section
          initial={{ opacity: 0, x: 24 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.32, ease: 'easeOut' }}
          className="flex flex-col justify-center bg-black px-8 py-10 lg:px-12 lg:py-12"
        >
          <div className="mx-auto w-full max-w-md space-y-6">
            <div className="space-y-3">
              <p className="text-xs uppercase tracking-[0.35em] text-white/60">Login</p>
              <h2 className="text-3xl font-semibold text-white">Access your workspace</h2>
              <p className="text-sm leading-6 text-white/70">
                Enter your credentials to access challenges, analytics, and AI support.
              </p>
            </div>

            <form className="space-y-4" onSubmit={handleSubmit}>
              <div>
                <label className="mb-2 block text-sm font-medium text-white/70">Email</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
                    setFormError('');
                  }}
                  required
                  className="w-full rounded-3xl border border-white/10 bg-[#111111] px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                  placeholder="you@codeforge.ai"
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-white/70">Password</label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    setFormError('');
                  }}
                  required
                  className="w-full rounded-3xl border border-white/10 bg-[#111111] px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                  placeholder="••••••••"
                />
              </div>

              {(formError || error) && (
                <div className="rounded-3xl border border-white/10 bg-white/5 px-4 py-3 text-sm text-white">
                  {formError || error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full rounded-3xl bg-white px-5 py-3 text-sm font-semibold text-black transition hover:bg-white/90 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {loading ? 'Signing in…' : 'Login'}
              </button>
            </form>

            <div className="flex items-center justify-between text-sm text-white/60">
              <button
                type="button"
                onClick={() => {
                  setForgotOpen(true);
                  resetForgotState();
                }}
                className="font-medium text-white/80 transition hover:text-white"
              >
                Forgot Password?
              </button>
              <Link to="/signup" className="font-medium text-white/80 transition hover:text-white">
                Create account
              </Link>
            </div>
          </div>
        </motion.section>
      </div>
      {forgotOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 px-4 py-6">
          <div className="w-full max-w-md overflow-hidden rounded-[24px] border border-white/10 bg-black p-6">
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-[0.35em] text-white/60">Password recovery</p>
                <h3 className="mt-3 text-2xl font-semibold text-white">Reset your password</h3>
              </div>
              <button
                type="button"
                onClick={() => {
                  setForgotOpen(false);
                  resetForgotState();
                }}
                className="text-sm font-medium text-white/70 transition hover:text-white"
              >
                Close
              </button>
            </div>

            <p className="mt-4 text-sm leading-6 text-white/70">
              Enter your registered email and follow the recovery steps.
            </p>

            <form className="mt-6 space-y-4" onSubmit={handleForgotSubmit}>
              <div>
                <label className="mb-2 block text-sm font-medium text-white/70">Email</label>
                <input
                  type="email"
                  value={forgotEmail}
                  onChange={(e) => setForgotEmail(e.target.value)}
                  required
                  className="w-full rounded-3xl border border-white/10 bg-[#111111] px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                  placeholder="you@codeforge.ai"
                />
              </div>

              {forgotStep !== 'start' && (
                <div>
                  <label className="mb-2 block text-sm font-medium text-white/70">Security question</label>
                  <div className="rounded-3xl border border-white/10 bg-[#111111] px-4 py-3 text-sm text-white/70">
                    {forgotQuestion}
                  </div>
                </div>
              )}

              {forgotStep !== 'start' && (
                <div>
                  <label className="mb-2 block text-sm font-medium text-white/70">Answer</label>
                  <input
                    type="text"
                    value={forgotAnswer}
                    onChange={(e) => setForgotAnswer(e.target.value)}
                    required
                    className="w-full rounded-3xl border border-white/10 bg-[#111111] px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                    placeholder="Your answer"
                  />
                </div>
              )}

              {forgotStep === 'reset' && (
                <>
                  <div>
                    <label className="mb-2 block text-sm font-medium text-white/70">New password</label>
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                      className="w-full rounded-3xl border border-white/10 bg-[#111111] px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                      placeholder="New password"
                    />
                  </div>
                  <div>
                    <label className="mb-2 block text-sm font-medium text-white/70">Confirm password</label>
                    <input
                      type="password"
                      value={confirmNewPassword}
                      onChange={(e) => setConfirmNewPassword(e.target.value)}
                      required
                      className="w-full rounded-3xl border border-white/10 bg-[#111111] px-4 py-3 text-white placeholder:text-white/40 transition focus:border-white focus:ring-0"
                      placeholder="Confirm new password"
                    />
                  </div>
                </>
              )}

              {(forgotError || forgotSuccess) && (
                <div className="rounded-3xl border border-white/10 bg-[#111111] px-4 py-3 text-sm text-white/80">
                  {forgotError || forgotSuccess}
                </div>
              )}

              <div className="flex flex-col gap-3">
                <button
                  type="submit"
                  disabled={forgotLoading}
                  className="w-full rounded-3xl bg-white px-5 py-3 text-sm font-semibold text-black transition hover:bg-white/90 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {forgotStep === 'start'
                    ? forgotLoading
                      ? 'Loading…'
                      : 'Continue'
                    : forgotStep === 'verify'
                    ? forgotLoading
                      ? 'Verifying…'
                      : 'Verify answer'
                    : forgotLoading
                    ? 'Resetting…'
                    : 'Reset password'}
                </button>
                {forgotStep !== 'start' && (
                  <button
                    type="button"
                    onClick={() => setForgotStep('start')}
                    className="w-full rounded-3xl border border-white/10 bg-[#111111] px-5 py-3 text-sm font-medium text-white/70 transition hover:text-white"
                  >
                    Back to email
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default LoginPage;
