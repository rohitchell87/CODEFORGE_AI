export interface ContestSummary {
  id: number;
  title: string;
  description?: string;
  startTime: string;
  endTime: string;
  durationInMinutes: number;
  status: string;
  problemCount: number;
  participantCount: number;
  problems?: ContestProblem[];
}

export interface ContestProblem {
  id: number;
  problemId: number;
  title: string;
  difficulty: string;
  category: string;
  orderIndex: number;
  points: number;
}

export interface ContestPage<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  numberOfElements: number;
}
