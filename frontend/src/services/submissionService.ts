import api from './api';
import type { SubmissionResult, SubmissionRecord, ProblemPage } from '../types/problem';

export interface CreateSubmissionRequest {
  problemId: number;
  language: string;
  code: string;
}

export const createSubmission = async (request: CreateSubmissionRequest): Promise<SubmissionResult> => {
  const response = await api.post('/submissions', request);
  return response.data?.data;
};

export const fetchUserSubmissions = async (
  page = 0,
  size = 20,
): Promise<ProblemPage<SubmissionRecord>> => {
  const url = '/submissions/user/my-submissions';
  const fullUrl = `${api.defaults.baseURL}${url}`;
  console.log('SUBMISSIONS REQUEST:', { fullUrl, page, size });
  try {
    const response = await api.get(url, {
      params: { page, size },
    });
    console.log('SUBMISSIONS RESPONSE:', {
      status: response.status,
      data: response.data,
      submissions: response.data?.data?.content?.length,
    });
    return response.data?.data;
  } catch (error: any) {
    console.error('SUBMISSIONS FETCH ERROR:', {
      message: error?.message,
      status: error?.response?.status,
      statusText: error?.response?.statusText,
      data: error?.response?.data,
      url: error?.config?.url,
      fullUrl: error?.config?.baseURL + error?.config?.url,
    });
    throw error;
  }
};
