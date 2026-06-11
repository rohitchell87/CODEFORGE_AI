import React, { useEffect, useMemo, useRef, useState } from 'react';
import Editor from '@monaco-editor/react';
import { getProblems } from '../services/problemService';
import { runCode, submitCode } from '../services/codeService';
import type { Language, ProblemSummary } from '../types/problem';

type ExecutionResult = {
  status: string;
  runtime: string;
  memory: string;
  output?: string;
  stdout?: string;
  passed?: number;
  total?: number;
  cases?: Array<{ name: string; status: string; output?: string; expected?: string }>;
};

const NAVBAR_HEIGHT = 60;

const CODE_TEMPLATES: Record<Language, string> = {
  Java: `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.hasNextLine() ? scanner.nextLine() : "";
        System.out.println(input);
    }
}`,
  'C++': `#include <iostream>
#include <string>

int main() {
    std::string line;
    if (std::getline(std::cin, line)) {
        std::cout << line << std::endl;
    }
    return 0;
}`,
  Python: `import sys

if __name__ == "__main__":
    data = sys.stdin.read()
    print(data, end="")`,
};

const languageMap: Record<Language, string> = {
  Java: 'java',
  'C++': 'cpp',
  Python: 'python',
};

export default function ProblemsPage() {
  const [problems, setProblems] = useState<ProblemSummary[]>([]);
  const [selected, setSelected] = useState<ProblemSummary | null>(null);
  const [search, setSearch] = useState('');
  const [difficultyFilter, setDifficultyFilter] = useState<'All' | 'Easy' | 'Medium' | 'Hard'>('All');
  const [language, setLanguage] = useState<Language>('Java');
  const [code, setCode] = useState<string>(CODE_TEMPLATES.Java);
  const [customInput, setCustomInput] = useState('');
  const [consoleText, setConsoleText] = useState('');
  const [executionResult, setExecutionResult] = useState<ExecutionResult | null>(null);
  const [executionError, setExecutionError] = useState<string | null>(null);
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [activeTab, setActiveTab] = useState<'Description' | 'Editorial' | 'Solutions' | 'Submissions'>('Description');
  const [aiOpen, setAiOpen] = useState(false);
  const autosaveRef = useRef<number | null>(null);

  // Broadcast selection state so other global components (Sidebar) can react
  useEffect(() => {
    try {
      window.dispatchEvent(new CustomEvent('problemSelectionChange', { detail: { selectedId: selected?.id ?? null } }));
    } catch (e) {
      // ignore in non-browser test environments
    }
  }, [selected]);

  // Listen for explicit "backToProblems" events from global UI (Sidebar)
  useEffect(() => {
    const handler = () => {
      setSelected(null);
    };
    window.addEventListener('backToProblems', handler as EventListener);
    return () => window.removeEventListener('backToProblems', handler as EventListener);
  }, []);

  useEffect(() => {
    getProblems(0, 150, {
      difficulty: difficultyFilter === 'All' ? undefined : difficultyFilter,
      search,
    }).then((data) => {
      setProblems(data);
      setSelected(null);
    });
  }, [difficultyFilter, search]);

  useEffect(() => {
    if (!selected) {
      setCode(CODE_TEMPLATES[language]);
      return;
    }

    const key = `code_autosave_${selected.id}_${language}`;
    const saved = localStorage.getItem(key);

    // Prefer saved autosave -> starter code from problem -> default template
    const starter =
      language === 'Java'
        ? selected.starterCodeJava || CODE_TEMPLATES.Java
        : language === 'C++'
        ? selected.starterCodeCpp || CODE_TEMPLATES['C++']
        : selected.starterCodePython || CODE_TEMPLATES.Python;

    setCode(saved ?? starter ?? CODE_TEMPLATES[language]);
  }, [selected, language]);

  useEffect(() => {
    return () => {
      if (autosaveRef.current) {
        window.clearTimeout(autosaveRef.current);
      }
    };
  }, []);

  const filteredProblems = useMemo<ProblemSummary[]>(
    () =>
      problems.filter((problem) => {
        const matchesSearch = problem.title.toLowerCase().includes(search.toLowerCase());
        const matchesDifficulty = difficultyFilter === 'All' || problem.difficulty === difficultyFilter;
        return matchesSearch && matchesDifficulty;
      }),
    [problems, search, difficultyFilter]
  );

  const saveAutosave = (newCode: string) => {
    if (!selected) return;
    const key = `code_autosave_${selected.id}_${language}`;
    if (autosaveRef.current) {
      window.clearTimeout(autosaveRef.current);
    }
    autosaveRef.current = window.setTimeout(() => {
      localStorage.setItem(key, newCode);
      autosaveRef.current = null;
    }, 600);
  };

  const handleRun = async () => {
    if (!selected) {
      setConsoleText('Select a problem before running code.');
      return;
    }

    const payload = { language: languageMap[language], code, customInput };
    console.log('Run payload:', payload);

    setIsRunning(true);
    setExecutionError(null);
    setExecutionResult(null);
    setConsoleText('Running...');

    try {
      const response = await runCode(payload);
      console.log('Run response:', response);
      setExecutionResult({
        status: response.status || 'Unknown',
        runtime: response.runtime || 'N/A',
        memory: response.memory || 'N/A',
        passed: 0,
        total: 0,
        cases: [{ name: 'Run', status: response.status || 'Unknown', output: response.output }],
      });
      setConsoleText(response.output || 'No output');
    } catch (err: any) {
      console.error('Run error:', err);
      const message = err?.response?.data?.message || err?.message || 'Failed to execute code';
      setConsoleText(message);
      setExecutionError(message);
      setExecutionResult({ status: 'Error', runtime: 'N/A', memory: 'N/A', passed: 0, total: 0, cases: [] });
    } finally {
      setIsRunning(false);
    }
  };

  const handleSubmit = async () => {
    console.log('SUBMIT BUTTON CLICKED');
    console.log('SUBMIT FUNCTION EXECUTED');

    if (!selected) {
      setConsoleText('Select a problem before submitting code.');
      return;
    }

    const payload = { problemId: selected.id, language: languageMap[language], code };
    console.log('Submit payload:', payload);

    const url = `${import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'}/code/submit`;
    console.log('SUBMIT REQUEST URL:', url);

    setIsSubmitting(true);
    setExecutionError(null);
    setExecutionResult(null);
    setConsoleText('Submitting...');

    try {
      const response = await submitCode(payload);
      console.log('Submit response:', response);
      setExecutionResult({
        status: response.status || 'Unknown',
        stdout: response.stdout,
        runtime: response.runtime || 'N/A',
        memory: response.memory || 'N/A',
        passed: response.passed,
        total: response.total,
        cases: response.cases,
      });
      setConsoleText(`Submission ${response.status}.`);
    } catch (err: any) {
      console.error('Submit error:', err);
      const message = err?.response?.data?.message || err?.message || 'Failed to submit code';
      setConsoleText(message);
      setExecutionError(message);
      setExecutionResult({ status: 'Error', runtime: 'N/A', memory: 'N/A', passed: 0, total: 0, cases: [] });
    } finally {
      setIsSubmitting(false);
    }
  };

  const toggleAi = () => setAiOpen((current) => !current);

  return (
    <div style={{ background: '#000000', height: '100%', color: '#FFFFFF', overflow: 'hidden', width: '100%', display: 'flex', flexDirection: 'column' }}>
      <main
        style={{
          height: '100%',
          display: 'grid',
          gridTemplateColumns: '1fr',
          gap: 12,
          boxSizing: 'border-box',
          overflow: 'hidden',
        }}
      >
        <section
          style={{
            display: 'grid',
            gridTemplateColumns: selected ? '1fr 1fr' : '350px 1fr',
            gap: 12,
            overflow: 'hidden',
            transition: 'grid-template-columns 240ms ease',
            minHeight: 0,
          }}
        >
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              background: '#000000',
              border: '1px solid rgba(255,255,255,0.08)',
              borderRadius: 10,
              padding: 16,
              overflow: 'hidden',
              minHeight: 0,
            }}
          >
            {!selected ? (
              <>
                <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                  <input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Search problems"
                    style={searchInputStyle}
                  />
                  <button style={smallActionButtonStyle}>Filter</button>
                </div>

                <div style={{ display: 'flex', gap: 8, marginTop: 14, flexWrap: 'wrap' }}>
                  {(['All', 'Easy', 'Medium', 'Hard'] as const).map((level) => (
                    <button
                      key={level}
                      onClick={() => setDifficultyFilter(level)}
                      style={difficultyButtonStyle(difficultyFilter === level)}
                    >
                      {level}
                    </button>
                  ))}
                </div>

                <div style={{ marginTop: 18, color: 'rgba(255,255,255,0.65)', fontSize: 12 }}>Problems</div>

                <div style={{ marginTop: 12, overflowY: 'auto', flex: 1, paddingRight: 4, minHeight: 0 }}>
                  {filteredProblems.map((problem: ProblemSummary) => {
                    const isActive = (selected as ProblemSummary | null)?.id === problem.id;
                    return (
                      <button
                        key={problem.id}
                        onClick={() => setSelected(problem)}
                        style={problemCardStyle(isActive)}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                          <span style={{ fontWeight: 600, color: '#FFFFFF' }}>{problem.title}</span>
                          <span style={{ fontSize: 11, color: 'rgba(255,255,255,0.65)' }}>{problem.acceptanceRate}%</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8, gap: 8 }}>
                          <span style={{ color: 'rgba(255,255,255,0.65)', fontSize: 12 }}>{problem.difficulty}</span>
                          <span style={{ color: '#FFFFFF', fontSize: 12 }}>
                            {problem.solved ? 'Solved' : 'Unsolved'}
                          </span>
                        </div>
                      </button>
                    );
                  })}
                </div>
              </>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>
                <div style={{ paddingBottom: 12, borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
                  <div style={{ color: 'rgba(255,255,255,0.65)', fontSize: 12 }}>{selected.difficulty} · {selected.acceptanceRate}% accepted</div>
                  <h2 style={{ margin: '10px 0 0', fontSize: 24, lineHeight: 1.1 }}>{selected.title}</h2>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginTop: 12 }}>
                    {selected.tags.map((tag, idx) => (
                      <span key={idx} style={tagBadgeStyle}>{tag}</span>
                    ))}
                  </div>
                </div>

                <div style={{ display: 'flex', gap: 10, padding: '14px 0', borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
                  {(['Description', 'Editorial', 'Solutions', 'Submissions'] as const).map((tab) => (
                    <button
                      key={tab}
                      onClick={() => setActiveTab(tab)}
                      style={tabButtonStyle(activeTab === tab)}
                    >
                      {tab}
                    </button>
                  ))}
                </div>

                <div style={{ flex: 1, overflowY: 'auto', paddingRight: 4, minHeight: 0 }}>
                  {activeTab === 'Description' && (
                    <div style={{ paddingRight: 8 }}>
                      <div style={panelHeadingStyle}>Description</div>
                      <p style={panelTextStyle}>{selected.description}</p>
                      <div style={panelHeadingStyle}>Examples</div>
                      <pre style={codeBlockStyle}>{selected.exampleInput}</pre>
                      <pre style={codeBlockStyle}>{selected.exampleOutput}</pre>
                      <div style={panelHeadingStyle}>Constraints</div>
                      <p style={panelTextStyle}>{selected.constraints}</p>
                      <div style={panelHeadingStyle}>Hints</div>
                      <p style={panelTextStyle}>{selected.sampleSolution}</p>
                    </div>
                  )}
                  {activeTab === 'Editorial' && (
                    <div style={panelTextStyle}>Editorial content is not available yet. Use the problem statement and run your code to validate your approach.</div>
                  )}
                  {activeTab === 'Solutions' && (
                    <div style={panelTextStyle}>Solutions are hidden until you submit your implementation. Focus on the editor and test output.</div>
                  )}
                  {activeTab === 'Submissions' && (
                    <div style={panelTextStyle}>No submissions yet. Run code or submit to see results.</div>
                  )}
                </div>
              </div>
            )}
          </div>

          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              background: '#000000',
              border: '1px solid rgba(255,255,255,0.08)',
              borderRadius: 10,
              overflow: 'hidden',
              minHeight: 0,
            }}
          >
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'center', padding: 16, borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
              <div style={{ display: 'flex', gap: 10, alignItems: 'center', minWidth: 0, flex: 1 }}>
                <label style={labelStyle}>Language</label>
                <select
                  value={language}
                  onChange={(event) => setLanguage(event.target.value as Language)}
                  style={selectStyle}
                >
                  <option value="Java">Java</option>
                  <option value="C++">C++</option>
                  <option value="Python">Python</option>
                </select>
                <input
                  value={customInput}
                  onChange={(event) => setCustomInput(event.target.value)}
                  placeholder="Custom test input"
                  style={inputStyle}
                />
              </div>
              <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                <button onClick={handleRun} style={runButtonStyle} disabled={isRunning || isSubmitting}>
                  {isRunning ? 'Running...' : 'Run Code'}
                </button>
                <button onClick={handleSubmit} style={submitButtonStyle} disabled={isRunning || isSubmitting}>
                  {isSubmitting ? 'Submitting...' : 'Submit'}
                </button>
              </div>
            </div>

            <div style={{ flex: 1, minHeight: 0, overflow: 'hidden' }}>
              <Editor
                height="100%"
                language={languageMap[language]}
                value={code}
                onChange={(value) => {
                  const nextCode = value ?? '';
                  setCode(nextCode);
                  saveAutosave(nextCode);
                }}
                options={{ minimap: { enabled: false }, fontSize: 14, wordWrap: 'on' }}
              />
            </div>

            <div style={{ height: 340, flexShrink: 0, maxHeight: 340, borderTop: '1px solid #1f1f1f', display: 'flex', flexDirection: 'column', overflow: 'hidden', background: '#000000' }}>
              <div style={{ padding: 16, borderBottom: '1px solid rgba(255,255,255,0.08)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ color: 'rgba(255,255,255,0.65)', fontSize: 12 }}>Execution</div>
                  <div style={{ fontWeight: 700 }}>{executionError ? 'Error' : executionResult?.status ?? 'No execution result yet'}</div>
                </div>
                <button onClick={toggleAi} style={aiButtonStyle}>AI Tips</button>
              </div>

              <div style={{ padding: '0 16px 16px', overflow: 'hidden', flex: 1, display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 10, marginBottom: 12 }}>
                  <div style={metricStyle}>
                    <div style={metricLabelStyle}>Runtime</div>
                    <div style={metricValueStyle}>{executionResult?.runtime ?? 'N/A'}</div>
                  </div>
                  <div style={metricStyle}>
                    <div style={metricLabelStyle}>Memory</div>
                    <div style={metricValueStyle}>{executionResult?.memory ?? 'N/A'}</div>
                  </div>
                  <div style={metricStyle}>
                    <div style={metricLabelStyle}>Passed</div>
                    <div style={metricValueStyle}>{executionResult ? `${executionResult.passed}/${executionResult.total}` : '0/0'}</div>
                  </div>
                </div>

                <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 10 }}>
                  <div>
                    {executionResult?.cases?.length ? (
                      executionResult.cases.map((test, index) => (
                        <div key={index} style={resultRowStyle(test.status === 'Passed')}>
                          <div>
                            <div style={{ fontWeight: 600, color: '#FFFFFF' }}>{test.name}</div>
                            <div style={{ color: 'rgba(255,255,255,0.65)', fontSize: 12 }}>{test.output}</div>
                          </div>
                          <div style={{ color: '#FFFFFF', fontWeight: 700 }}>{test.status}</div>
                        </div>
                      ))
                    ) : (
                      <div style={{ color: 'rgba(255,255,255,0.65)', fontSize: 12 }}>No test case details available.</div>
                    )}
                  </div>
                  <div style={{ background: '#000000', border: '1px solid rgba(255,255,255,0.08)', padding: 12, minHeight: 80, overflowY: 'auto' }}>
                    <div style={{ color: 'rgba(255,255,255,0.65)', marginBottom: 8, fontSize: 12 }}>Console output</div>
                    <pre style={{ margin: 0, whiteSpace: 'pre-wrap', color: '#FFFFFF', fontSize: 12 }}>{consoleText || 'Console output will appear here.'}</pre>
                  </div>
                </div>
              </div>

              {aiOpen && (
                <div style={{ borderTop: '1px solid rgba(255,255,255,0.08)', padding: 16, background: '#000000', overflowY: 'auto' }}>
                  <div style={{ fontWeight: 700, marginBottom: 10 }}>AI Assistant</div>
                  <div style={{ color: 'rgba(255,255,255,0.65)', fontSize: 12, lineHeight: 1.6 }}>
                    <p><strong>Hint:</strong> Optimize with a HashMap to achieve O(n) runtime for Two Sum.</p>
                    <p><strong>Debug:</strong> Check whether your algorithm uses the same element twice.</p>
                    <p><strong>Complexity:</strong> Aim for linear time and constant extra space when possible.</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

const logoutButtonStyle: React.CSSProperties = {
  border: '1px solid rgba(255,255,255,0.08)',
  background: 'transparent',
  color: '#FFFFFF',
  padding: '8px 12px',
  cursor: 'pointer',
};

const sidebarButtonStyle = (active: boolean): React.CSSProperties => ({
  display: 'flex',
  justifyContent: 'flex-start',
  width: '100%',
  textAlign: 'left',
  padding: '10px 12px',
  borderRadius: 12,
  border: 'none',
  background: active ? 'rgba(255,255,255,0.03)' : 'transparent',
  color: '#FFFFFF',
  cursor: 'pointer',
});

const searchInputStyle: React.CSSProperties = {
  width: '100%',
  padding: '10px 12px',
  borderRadius: 8,
  border: '1px solid rgba(255,255,255,0.08)',
  background: '#000000',
  color: '#FFFFFF',
};

const smallActionButtonStyle: React.CSSProperties = {
  background: '#000000',
  border: '1px solid rgba(255,255,255,0.08)',
  color: '#FFFFFF',
  padding: '10px 14px',
  cursor: 'pointer',
};

const difficultyButtonStyle = (active: boolean): React.CSSProperties => ({
  background: active ? 'rgba(255,255,255,0.03)' : 'transparent',
  border: '1px solid rgba(255,255,255,0.08)',
  color: '#FFFFFF',
  padding: '8px 12px',
  cursor: 'pointer',
});

const problemCardStyle = (active: boolean): React.CSSProperties => ({
  width: '100%',
  padding: 14,
  marginBottom: 10,
  borderRadius: 12,
  border: active ? '1px solid #FFFFFF' : '1px solid rgba(255,255,255,0.08)',
  background: active ? 'rgba(255,255,255,0.03)' : 'transparent',
  textAlign: 'left',
  cursor: 'pointer',
});

const tagBadgeStyle: React.CSSProperties = {
  padding: '5px 10px',
  borderRadius: 999,
  border: '1px solid rgba(255,255,255,0.08)',
  color: 'rgba(255,255,255,0.65)',
  fontSize: 12,
};

const tabButtonStyle = (active: boolean): React.CSSProperties => ({
  flex: 1,
  padding: '10px 12px',
  border: 'none',
  borderBottom: active ? '2px solid #FFFFFF' : '2px solid transparent',
  color: '#FFFFFF',
  background: 'transparent',
  cursor: 'pointer',
});

const panelHeadingStyle: React.CSSProperties = {
  color: '#FFFFFF',
  fontWeight: 700,
  marginBottom: 8,
};

const panelTextStyle: React.CSSProperties = {
  color: 'rgba(255,255,255,0.65)',
  lineHeight: 1.7,
  margin: '0 0 16px',
};

const codeBlockStyle: React.CSSProperties = {
  background: '#000000',
  border: '1px solid rgba(255,255,255,0.08)',
  borderRadius: 10,
  color: 'rgba(255,255,255,0.65)',
  padding: 12,
  whiteSpace: 'pre-wrap',
  marginBottom: 12,
};

const labelStyle: React.CSSProperties = {
  color: 'rgba(255,255,255,0.65)',
  fontSize: 12,
};

const selectStyle: React.CSSProperties = {
  background: '#000000',
  color: '#FFFFFF',
  border: '1px solid rgba(255,255,255,0.08)',
  padding: '8px 10px',
  borderRadius: 8,
};

const inputStyle: React.CSSProperties = {
  background: '#000000',
  color: '#FFFFFF',
  border: '1px solid rgba(255,255,255,0.08)',
  padding: '8px 10px',
  borderRadius: 8,
  minWidth: 190,
};

const runButtonStyle: React.CSSProperties = {
  border: 'none',
  background: '#000000',
  color: '#FFFFFF',
  padding: '10px 16px',
  borderRadius: 8,
  cursor: 'pointer',
  fontWeight: 700,
};

const submitButtonStyle: React.CSSProperties = {
  border: '1px solid rgba(255,255,255,0.08)',
  background: '#000000',
  color: '#FFFFFF',
  padding: '10px 16px',
  borderRadius: 8,
  cursor: 'pointer',
  fontWeight: 700,
};

const aiButtonStyle: React.CSSProperties = {
  border: '1px solid rgba(255,255,255,0.08)',
  background: '#000000',
  color: '#FFFFFF',
  padding: '8px 12px',
  borderRadius: 8,
  cursor: 'pointer',
};

const metricStyle: React.CSSProperties = {
  background: '#000000',
  border: '1px solid rgba(255,255,255,0.08)',
  borderRadius: 12,
  padding: 12,
  minHeight: 64,
};

const metricLabelStyle: React.CSSProperties = {
  color: 'rgba(255,255,255,0.65)',
  fontSize: 12,
  marginBottom: 6,
};

const metricValueStyle: React.CSSProperties = {
  color: '#FFFFFF',
  fontWeight: 700,
};

const resultRowStyle = (passed: boolean): React.CSSProperties => ({
  display: 'flex',
  justifyContent: 'space-between',
  gap: 10,
  padding: 12,
  borderRadius: 10,
  border: '1px solid rgba(255,255,255,0.08)',
  background: '#000000',
});
