import api from './api';
import type { ContestSummary, ContestProblem } from '../types/contest';

export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  numberOfElements: number;
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export async function getContests(page = 0, size = 10) {
  const response = await api.get<ApiResponse<Page<ContestSummary>>>(`/contests?page=${page}&size=${size}`);
  return response.data.data;
}

export async function getContest(contestId: number) {
  const response = await api.get<ApiResponse<ContestSummary>>(`/contests/${contestId}`);
  return response.data.data;
}

export async function joinContest(contestId: number) {
  const response = await api.post<ApiResponse<string>>(`/contests/${contestId}/participate`);
  return response.data;
}

export async function getContestLeaderboard(contestId: number, page = 0, size = 10) {
  const response = await api.get<ApiResponse<Page<any>>>(`/contests/${contestId}/leaderboard?page=${page}&size=${size}`);
  return response.data.data;
}
