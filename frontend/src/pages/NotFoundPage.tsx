import { Link } from 'react-router-dom';

function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-black px-4 py-10">
      <div className="w-full max-w-xl rounded-2xl border border-white/10 bg-black p-10 text-center">
        <p className="text-sm uppercase tracking-[0.25em] text-textSecondary">404 error</p>
        <h1 className="mt-4 text-4xl font-semibold text-white">Page not found</h1>
        <p className="mt-4 text-textSecondary">The route you requested isn’t available. Return to the dashboard or login page.</p>
        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Link
            to="/dashboard"
            className="rounded-2xl bg-white px-6 py-3 text-sm font-semibold text-black transition hover:bg-white/10"
          >
            Dashboard
          </Link>
          <Link
            to="/login"
            className="rounded-2xl border border-white/10 px-6 py-3 text-sm text-textSecondary transition hover:border-white hover:text-white"
          >
            Login
          </Link>
        </div>
      </div>
    </div>
  );
}

export default NotFoundPage;
