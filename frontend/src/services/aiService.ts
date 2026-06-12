import api from './api';

export interface AiHintRequest {
  problemId?: number;
  problemTitle?: string;
  problemDescription?: string;
  userCode?: string;
  hintType?: string;
  difficulty?: string;
}

export interface AiResponse {
  response: string;
  content: string;
  type: string;
  success: boolean;
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
  timestamp: string;
}

export async function requestAiHint(request: AiHintRequest): Promise<AiResponse> {
  const res = await api.post<ApiResponse<AiResponse>>('/ai/hint', request);
  return res.data.data;
}
