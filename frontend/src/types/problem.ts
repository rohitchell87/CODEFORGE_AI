export type Language = 'Java' | 'C++' | 'Python';

export interface ProblemTestCase {
  input: string;
  expectedOutput: string;
  explanation?: string;
}

export interface ProblemExample {
  input: string;
  output: string;
  explanation?: string;
}

export interface ProblemSummary {
  id: number;
  title: string;
  description: string;
  difficulty: 'Easy' | 'Medium' | 'Hard' | string;
  category: string;
  solved: boolean;
  acceptanceRate: number;
  exampleInput: string;
  exampleOutput: string;
  examples?: ProblemExample[];
  sampleSolution: string;
  constraints: string;
  hints?: string;
  visibleTestCases?: ProblemTestCase[];
  hiddenTestCases?: ProblemTestCase[];
  solutionTemplate?: string;
  orderIndex?: number;
  tags: string[];
  starterCodeJava?: string;
  starterCodeCpp?: string;
  starterCodePython?: string;
  slug?: string;
  submissionCount?: number;
  acceptedCount?: number;
}

export interface ProblemPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface SubmissionResult {
  id: string;
  status: 'Accepted' | 'Wrong Answer' | 'Runtime Error' | 'Compilation Error' | 'Timed Out';
  runtime: string;
  memory: string;
  testOutput: string;
  submittedAt: string;
}

export interface SubmissionRecord {
  id: number;
  problemId: number;
  executionTime?: number;
  memoryUsage?: number;
  executionStatus?: string;
  output?: string;
  submittedAt: string;
}

export interface SubmissionDto {
  id: number;
  problemId: number;
  problemTitle?: string;
  userId?: number;
  language?: string;
  executionStatus?: string;
  isAccepted?: boolean;
  passedTests?: number;
  totalTests?: number;
  executionTime?: number;
  memoryUsage?: number;
  output?: string;
  submittedAt: string;
}
