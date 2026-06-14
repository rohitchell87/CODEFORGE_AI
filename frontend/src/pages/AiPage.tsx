import { useMemo, useState } from 'react';
import api from '../services/api';
import ReactMarkdown from 'react-markdown';
import { MessageCircle, Sparkles, Send } from 'lucide-react';

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

const initialMessages: ChatMessage[] = [
  {
    role: 'assistant',
    content: 'Welcome to the AI assistant. Ask me about problem strategy, debugging, or complexity analysis.',
  },
];

function AiPage() {
  const [messages, setMessages] = useState<ChatMessage[]>(initialMessages);
  const [input, setInput] = useState('');
  const [typing, setTyping] = useState(false);

  const handleSend = async () => {
    if (!input.trim()) return;
    const updated: ChatMessage[] = [...messages, { role: 'user', content: input }];
    setMessages(updated);
    setInput('');
    setTyping(true);
    try {
      const payload = {
        problemTitle: 'General Question',
        problemDescription: input,
        userCode: '',
        hintType: 'DEBUG',
      };
      const resp = await api.post('/ai/hint', payload);
      const content = resp?.data?.data?.content ?? resp?.data?.data?.response ?? 'No response from AI.';
      setMessages((current) => [...current, { role: 'assistant', content }]);
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'AI request failed';
      setMessages((current) => [...current, { role: 'assistant', content: `Error: ${msg}` }]);
    } finally {
      setTyping(false);
    }
  };

  const handleQuickPrompt = async (hintType: string) => {
    if (!input.trim()) return;
    const updated: ChatMessage[] = [...messages, { role: 'user', content: input }];
    setMessages(updated);
    setInput('');
    setTyping(true);
    try {
      const payload = {
        problemTitle: 'General Question',
        problemDescription: input,
        userCode: '',
        hintType: hintType,
      };
      const resp = await api.post('/ai/hint', payload);
      const content = resp?.data?.data?.content ?? resp?.data?.data?.response ?? 'No response from AI.';
      setMessages((current) => [...current, { role: 'assistant', content }]);
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'AI request failed';
      setMessages((current) => [...current, { role: 'assistant', content: `Error: ${msg}` }]);
    } finally {
      setTyping(false);
    }
  };

  const messageRows = useMemo(
    () => messages.map((message, index) => ({ ...message, id: `${message.role}-${index}` })),
    [messages]
  );

  return (
    <div className="w-full">
      <div className="space-y-6 px-4 py-5">
        <section className="rounded-2xl border border-white/10 bg-black p-8">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-sm uppercase tracking-[0.25em] text-textSecondary">AI assistant</p>
              <h1 className="mt-3 text-3xl font-semibold text-textPrimary">Ask the coding coach</h1>
            </div>
            <div className="inline-flex items-center gap-2 rounded-2xl border border-white/10 bg-black px-4 py-3 text-sm text-textSecondary">
              <Sparkles size={18} className="text-textSecondary" />
              <span>Context-aware hints and walkthroughs</span>
            </div>
          </div>
          <p className="mt-4 max-w-2xl text-sm text-textSecondary">
            Ask about algorithm design, sample test cases, or debugging suggestions and get a polished AI response.
          </p>
        </section>

        <section className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
        <div className="space-y-4 rounded-2xl border border-white/10 bg-black p-4">
          <div className="rounded-2xl border border-white/10 bg-black p-4 max-h-[560px] overflow-y-auto">
            {messageRows.map((message) => (
              <div
                key={message.id}
                className={`mb-4 rounded-2xl p-4 ${
                  message.role === 'assistant'
                    ? 'bg-black text-textPrimary'
                    : 'bg-black text-textPrimary border border-white/10'
                }`}
              >
                <p className="text-[11px] uppercase tracking-[0.3em] text-textSecondary">
                  {message.role === 'assistant' ? 'AI coach' : 'You'}
                </p>
                <ReactMarkdown className="prose prose-invert max-w-none mt-2 text-sm leading-6">{message.content}</ReactMarkdown>
              </div>
            ))}
            {typing && <div className="animate-pulse rounded-2xl bg-black p-4 text-sm text-textSecondary">AI is typing a response...</div>}
          </div>

          <div className="rounded-2xl border border-white/10 bg-black p-4">
            <h2 className="text-lg font-semibold text-textPrimary">Need help with</h2>
            <div className="mt-4 grid gap-3 sm:grid-cols-2">
              {['Algorithm choice', 'Optimization tips', 'Debug suggestions', 'Complexity review'].map((item) => (
                <button key={item} type="button" onClick={() => handleQuickPrompt(item)} className="rounded-2xl border border-white/10 bg-black px-4 py-3 text-left text-sm text-textPrimary transition hover:bg-white/5">
                  {item}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="rounded-2xl border border-white/10 bg-black p-6">
          <div className="flex items-center justify-between gap-4">
            <div>
              <p className="text-xs uppercase tracking-[0.25em] text-textSecondary">Quick prompt</p>
              <h2 className="mt-2 text-2xl font-semibold text-textPrimary">Ask the AI</h2>
            </div>
            <MessageCircle size={22} className="text-textSecondary" />
          </div>
          <textarea
            value={input}
            onChange={(event) => setInput(event.target.value)}
            rows={7}
            className="mt-5 w-full rounded-2xl border border-white/10 bg-black px-4 py-3 text-textPrimary placeholder:text-textSecondary focus:border-white focus:ring-2 focus:ring-white/20"
            placeholder="Describe the problem or ask for a hint..."
          />
          <button
            type="button"
            onClick={handleSend}
            className="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-black px-5 py-3 text-sm font-semibold text-white transition hover:bg-white/5"
          >
            <Send size={16} />
            Send question
          </button>
        </div>
      </section>
      </div>
    </div>
  );
}

export default AiPage;
