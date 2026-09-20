import { useCallback, useEffect, useRef, useState } from "react";
import { UploadCloud, FileText, Loader2, CheckCircle2, XCircle } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { api, ApiError, type DocumentResponse } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { cn } from "@/lib/utils";

const POLL_INTERVAL_MS = 3000;

function StatusBadge({ status }: { status: DocumentResponse["status"] }) {
  switch (status) {
    case "INDEXED":
      return (
        <Badge variant="success">
          <CheckCircle2 className="size-3" /> Indexed
        </Badge>
      );
    case "FAILED":
      return (
        <Badge variant="destructive">
          <XCircle className="size-3" /> Failed
        </Badge>
      );
    default:
      return (
        <Badge variant="warning">
          <Loader2 className="size-3 animate-spin" /> Processing
        </Badge>
      );
  }
}

export function UploadPanel() {
  const { user, logout } = useAuth();
  const [documents, setDocuments] = useState<DocumentResponse[]>([]);
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const refresh = useCallback(async () => {
    if (!user) return;
    try {
      const docs = await api.listDocuments(user.token, user.entityCode);
      setDocuments(docs.sort((a, b) => b.uploadedAt.localeCompare(a.uploadedAt)));
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        logout();
      }
    }
  }, [user, logout]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  useEffect(() => {
    const hasProcessing = documents.some((d) => d.status === "PROCESSING");
    if (!hasProcessing) return;
    const interval = setInterval(refresh, POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [documents, refresh]);

  if (!user) return null;

  async function uploadFile(file: File) {
    if (!user) return;
    setUploading(true);
    try {
      const doc = await api.uploadDocument(user.token, user.entityCode, file);
      setDocuments((prev) => [doc, ...prev]);
      toast.success(`Uploaded "${file.name}" — indexing now`);
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        logout();
        return;
      }
      const detail = error instanceof ApiError ? error.message : "Upload failed";
      toast.error(detail);
    } finally {
      setUploading(false);
    }
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setDragActive(false);
    const file = e.dataTransfer.files?.[0];
    if (file) uploadFile(file);
  }

  function handleFileInputChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (file) uploadFile(file);
    e.target.value = "";
  }

  return (
    <div className="flex h-full flex-col">
      <div className="border-b border-border px-4 py-3">
        <h2 className="text-sm font-semibold">Documents</h2>
        <p className="text-xs text-muted-foreground">Upload docx, pdf, ppt or txt files for {user.entityCode}</p>
      </div>

      <div className="p-4">
        <div
          onDragOver={(e) => {
            e.preventDefault();
            setDragActive(true);
          }}
          onDragLeave={() => setDragActive(false)}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          className={cn(
            "flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed p-6 text-center transition-colors",
            dragActive ? "border-primary bg-accent" : "border-border hover:bg-accent/50",
            uploading && "pointer-events-none opacity-60",
          )}
        >
          {uploading ? <Loader2 className="size-6 animate-spin text-muted-foreground" /> : <UploadCloud className="size-6 text-muted-foreground" />}
          <p className="text-sm font-medium">{uploading ? "Uploading..." : "Drag a file here or click to browse"}</p>
          <p className="text-xs text-muted-foreground">.docx, .pdf, .ppt, .pptx, .txt</p>
        </div>
        <input
          ref={fileInputRef}
          type="file"
          accept=".docx,.pdf,.ppt,.pptx,.txt"
          className="hidden"
          onChange={handleFileInputChange}
        />
      </div>

      <ScrollArea className="flex-1 px-4 pb-4">
        {documents.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">No documents uploaded yet.</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {documents.map((doc) => (
              <li key={doc.id} className="flex items-center gap-3 rounded-lg border border-border bg-card p-3">
                <FileText className="size-4 shrink-0 text-muted-foreground" />
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{doc.filename}</p>
                  {doc.status === "FAILED" && doc.errorMessage && (
                    <p className="truncate text-xs text-destructive">{doc.errorMessage}</p>
                  )}
                  {doc.status === "INDEXED" && doc.chunkCount != null && (
                    <p className="text-xs text-muted-foreground">{doc.chunkCount} chunks</p>
                  )}
                </div>
                <StatusBadge status={doc.status} />
              </li>
            ))}
          </ul>
        )}
      </ScrollArea>
    </div>
  );
}
