import { Sparkles, LogOut, ShieldCheck } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Avatar } from "@/components/ui/avatar";
import { ThemeToggle } from "@/components/ThemeToggle";
import { ChatPanel } from "@/components/ChatPanel";
import { UploadPanel } from "@/components/UploadPanel";
import { useAuth } from "@/lib/auth";

export function AppPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  function handleLogout() {
    logout();
    navigate("/", { replace: true });
  }

  const isAdmin = user!.role === "ADMIN";

  return (
    <div className="flex h-screen flex-col bg-background">
      <header className="flex shrink-0 items-center justify-between border-b border-border px-4 py-3 sm:px-6">
        <div className="flex items-center gap-2">
          <div className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <Sparkles className="size-4" />
          </div>
          <div className="leading-tight">
            <p className="text-sm font-semibold">ERP RAG</p>
            <p className="text-xs text-muted-foreground">{user.entityCode}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <ThemeToggle />
          <div className="hidden items-center gap-2 sm:flex">
            <Avatar name={user.username} />
            <div className="leading-tight">
              <p className="text-sm font-medium">{user.username}</p>
              <Badge variant={isAdmin ? "default" : "secondary"} className="mt-0.5">
                {isAdmin && <ShieldCheck className="size-3" />}
                {user.role}
              </Badge>
            </div>
          </div>
          <Button variant="outline" size="sm" onClick={handleLogout}>
            <LogOut />
            Logout
          </Button>
        </div>
      </header>

      <main className="flex min-h-0 flex-1 flex-col md:flex-row">
        <section className="min-h-0 flex-1">
          <ChatPanel />
        </section>
        {isAdmin && (
          <aside className="min-h-0 border-t border-border md:w-96 md:border-l md:border-t-0">
            <UploadPanel />
          </aside>
        )}
      </main>
    </div>
  );
}
