import api from './api';

export interface DashboardStats {
  solvedCount: number;
  totalSubmissions: number;
  streak: number;
  accuracy: number;
  contests: number;
  recentActivity: Array<{ label: string; value: number }>;
  solvedTrend: Array<{ day: string; solved: number }>;
}

const fallbackStats: DashboardStats = {
  solvedCount: 0,
  totalSubmissions: 0,
  streak: 0,
  accuracy: 0,
  contests: 0,
  recentActivity: [
    { label: 'Solved', value: 0 },
    { label: 'Accepted', value: 0 },
    { label: 'Attempts', value: 0 },
  ],
  solvedTrend: [],
};

export async function getDashboardStats() {
  try {
    const response = await api.get<{ data: DashboardStats }>('/dashboard/overview');
    return response.data.data;
  } catch {
    return fallbackStats;
  }
}
