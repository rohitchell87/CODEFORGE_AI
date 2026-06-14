import api from './api';
import type { ProblemSummary, ProblemPage, ProblemTestCase } from '../types/problem';

interface RawProblemDto {
  id: number;
  title: string;
  slug?: string;
  difficulty: string;
  category: string;
  description: string;
  exampleInput?: string;
  exampleOutput?: string;
  sampleSolution?: string;
  solutionTemplate?: string;
  constraints?: string | string[];
  hints?: string | string[];
  starterCodeJava?: string;
  starterCodeCpp?: string;
  starterCodePython?: string;
  visibleTestCases?: ProblemTestCase[];
  hiddenTestCases?: ProblemTestCase[];
  acceptanceRate: number;
  submissionCount?: number;
  acceptedCount?: number;
  tags?: string[];
}

async function fetchSolvedProblemIds(): Promise<Set<number>> {
  try {
    const response = await api.get<{ data: number[] }>('/users/me/solved-problems');
    return new Set(response.data.data ?? []);
  } catch (err) {
    console.debug('Unable to fetch solved problems, rendering list without solved state.', err);
    return new Set();
  }
}

function mapDto(problem: RawProblemDto, solvedIds: Set<number>): ProblemSummary {
  const constraints = Array.isArray(problem.constraints)
    ? problem.constraints.join('\n')
    : problem.constraints ?? 'No constraints provided.';
  const hints = Array.isArray(problem.hints)
    ? problem.hints.join('\n')
    : problem.hints ?? '';

  return {
    id: problem.id,
    title: problem.title,
    difficulty: problem.difficulty || 'Medium',
    category: problem.category || 'General',
    solved: solvedIds.has(problem.id),
    acceptanceRate: problem.acceptanceRate ?? 0,
    submissionCount: problem.submissionCount ?? 0,
    acceptedCount: problem.acceptedCount ?? 0,
    description: problem.description ?? 'No description available.',
    exampleInput: problem.exampleInput ?? '',
    exampleOutput: problem.exampleOutput ?? '',
    sampleSolution: problem.sampleSolution ?? '',
    solutionTemplate: problem.solutionTemplate ?? '',
    constraints,
    hints,
    visibleTestCases: problem.visibleTestCases ?? [],
    hiddenTestCases: problem.hiddenTestCases ?? [],
    tags: problem.tags ?? [problem.category || 'General'],
    starterCodeJava: problem.starterCodeJava ?? '',
    starterCodeCpp: problem.starterCodeCpp ?? '',
    starterCodePython: problem.starterCodePython ?? '',
    slug: problem.slug ?? '',
  };
}

export async function getProblems(
  page = 0,
  size = 150,
  filters?: { difficulty?: string; tag?: string; search?: string }
): Promise<ProblemSummary[]> {
  const params: Record<string, string> = {
    page: String(page),
    size: String(size),
  };

  if (filters?.difficulty && filters.difficulty !== 'All') {
    params.difficulty = filters.difficulty;
  }
  if (filters?.tag) {
    params.tag = filters.tag;
  }
  if (filters?.search) {
    params.search = filters.search;
  }

  try {
    const [problemResponse, solvedIds] = await Promise.all([
      api.get<{ data: ProblemPage<RawProblemDto> }>('/problems', { params }),
      fetchSolvedProblemIds(),
    ]);
    return problemResponse.data.data.content.map((problem) => mapDto(problem, solvedIds));
  } catch (err) {
    console.error('Failed to fetch problems:', err);
    return [];
  }
}

export async function getProblemById(id: number): Promise<ProblemSummary> {
  const solvedIds = await fetchSolvedProblemIds();
  const response = await api.get<{ data: RawProblemDto }>(`/problems/${id}`);
  return mapDto(response.data.data, solvedIds);
}
