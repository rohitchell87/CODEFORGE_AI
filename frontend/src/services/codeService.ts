import api from './api';

export interface RunRequest {
  language: string;
  code: string;
  customInput?: string;
}

export interface RunResponse {
  output: string;
  stdout?: string;
  stderr?: string;
  compileOutput?: string;
  runtime: string;
  memory: string;
  status: string;
}

export interface SubmitRequest {
  problemId: number;
  language: string;
  code: string;
}

export interface SubmitResponse {
  status: string;
  stdout?: string;
  runtime: string;
  memory: string;
  passed: number;
  total: number;
  cases: Array<{ name: string; status: string; output?: string; expected?: string }>;
}

export async function runCode(req: RunRequest): Promise<RunResponse> {
  const res = await api.post('/code/run', req);
  return res.data;
}

export async function submitCode(req: SubmitRequest): Promise<SubmitResponse> {
  console.log('Frontend submit URL:', `${api.defaults.baseURL}/code/submit`, 'payload:', req);
  const res = await api.post('/code/submit', req);
  return res.data;
}
