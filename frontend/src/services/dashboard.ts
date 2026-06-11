import api from './api';

export interface DashboardStats {
  solvedCount: number;
  streak: number;
  accuracy: number;
  contests: number;
  recentActivity: Array<{ label: string; value: number }>;
  solvedTrend: Array<{ day: string; solved: number }>;
}

const fallbackStats: DashboardStats = {
  solvedCount: 42,
  streak: 7,
  accuracy: 88,
  contests: 4,
  recentActivity: [
    { label: 'Solved', value: 42 },
    { label: 'Accepted', value: 37 },
    { label: 'Attempts', value: 52 },
  ],
  solvedTrend: [
    { day: 'Mon', solved: 5 },
    { day: 'Tue', solved: 7 },
    { day: 'Wed', solved: 6 },
    { day: 'Thu', solved: 10 },
    { day: 'Fri', solved: 4 },
    { day: 'Sat', solved: 5 },
    { day: 'Sun', solved: 5 },
  ],
};

export async function getDashboardStats() {
  try {
    const response = await api.get<{ data: DashboardStats }>('/dashboard/overview');
    return response.data.data;
  } catch {
    return fallbackStats;
  }
}
