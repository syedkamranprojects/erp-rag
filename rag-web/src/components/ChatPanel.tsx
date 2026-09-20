import { useEffect, useRef, useState } from "react";
import { SendHorizonal, RotateCcw, Bot } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Avatar } from "@/components/ui/avatar";
import { api, ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { cn } from "@/lib/utils";

interface ChatMessage {
  role: "user" | "assistant";
  text: string;
}

function sessionStorageKey(entityCode: string, username: string) {
  return `erp-rag-session:${entityCode}:${username}`;
}

function newSessionId() {
  return crypto.randomUUID();
}

export function ChatPanel() {
  const { user, logout } = useAuth();
  const [sessionId, setSessionId] = useState(() =>
    user ? sessionStorage.getItem(sessionStorageKey(user.entityCode, user.username)) ?? newSessionId() : newSessionId(),
  );
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!user) return;
    sessionStorage.setItem(sessionStorageKey(user.entityCode, user.username), sessionId);
  }, [sessionId, user]);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [messages, sending]);

  if (!user) return null;

  async function handleSend(e: React.FormEvent) {
    e.preventDefault();
    const message = input.trim();
    if (!message || sending || !user) return;

    setMessages((prev) => [...prev, { role: "user", text: message }]);
    setInput("");
    setSending(true);
    try {
      const response = await api.chat(user.token, user.entityCode, sessionId, message);
      setMessages((prev) => [...prev, { role: "assistant", text: response.answer }]);
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        toast.error("Your session expired. Please log in again.");
        logout();
        return;
      }
      const detail = error instanceof ApiError ? error.message : "Something went wrong";
      toast.error(detail);
      setMessages((prev) => prev.slice(0, -1));
      setInput(message);
    } finally {
      setSending(false);
    }
  }

  function handleNewSession() {
    setMessages([]);
    setSessionId(newSessionId());
  }

  return (
    <div className="flex h-full flex-col">
      <div className="flex items-center justify-between border-b border-border px-4 py-3">
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Bot className="size-4" />
          Chat is scoped to <span className="font-medium text-foreground">{user.entityCode}</span>'s documents only
        </div>
        <Button variant="outline" size="sm" onClick={handleNewSession}>
          <RotateCcw />
          New session
        </Button>
      </div>

      <ScrollArea ref={scrollRef} className="flex-1 px-4 py-4">
        {messages.length === 0 ? (
          <div className="flex h-full flex-col items-center justify-center gap-2 py-16 text-center text-muted-foreground">
            <Bot className="size-8" />
            <p>Ask a question about your entity's uploaded documents.</p>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {messages.map((m, i) => (
              <div key={i} className={cn("flex items-start gap-3", m.role === "user" && "flex-row-reverse")}>
                <Avatar name={m.role === "user" ? user.username : "AI"} className={m.role === "assistant" ? "bg-secondary text-secondary-foreground" : undefined} />
                <div
                  className={cn(
                    "max-w-[75%] whitespace-pre-wrap rounded-2xl px-4 py-2.5 text-sm leading-relaxed shadow-sm",
                    m.role === "user" ? "bg-primary text-primary-foreground" : "bg-card border border-border",
                  )}
                >
                  {m.text}
                </div>
              </div>
            ))}
            {sending && (
              <div className="flex items-start gap-3">
                <Avatar name="AI" className="bg-secondary text-secondary-foreground" />
                <div className="flex items-center gap-1 rounded-2xl border border-border bg-card px-4 py-3">
                  <span className="size-1.5 animate-bounce rounded-full bg-muted-foreground [animation-delay:-0.3s]" />
                  <span className="size-1.5 animate-bounce rounded-full bg-muted-foreground [animation-delay:-0.15s]" />
                  <span className="size-1.5 animate-bounce rounded-full bg-muted-foreground" />
                </div>
              </div>
            )}
          </div>
        )}
      </ScrollArea>

      <form onSubmit={handleSend} className="flex items-center gap-2 border-t border-border p-4">
        <Input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask about your uploaded documents..."
          disabled={sending}
          autoFocus
        />
        <Button type="submit" disabled={sending || !input.trim()}>
          <SendHorizonal />
          Send
        </Button>
      </form>
    </div>
  );
}
