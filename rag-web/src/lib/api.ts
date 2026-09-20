const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export interface LoginRequest {
  entityCode: string;
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresInSeconds: number;
}

export interface ChatResponse {
  answer: string;
}

export type DocumentStatus = "PROCESSING" | "INDEXED" | "FAILED";

export interface DocumentResponse {
  id: string;
  filename: string;
  status: DocumentStatus;
  chunkCount: number | null;
  errorMessage: string | null;
  uploadedAt: string;
  indexedAt: string | null;
}

async function parseErrorMessage(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as { error?: string };
    return body.error ?? response.statusText;
  } catch {
    return response.statusText || `Request failed with status ${response.status}`;
  }
}

async function request<T>(path: string, options: RequestInit & { token?: string } = {}): Promise<T> {
  const { token, headers, ...rest } = options;
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...rest,
    headers: {
      ...(rest.body && !(rest.body instanceof FormData) ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
  });

  if (!response.ok) {
    throw new ApiError(response.status, await parseErrorMessage(response));
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export const api = {
  login: (body: LoginRequest) =>
    request<LoginResponse>("/api/auth/login", { method: "POST", body: JSON.stringify(body) }),

  chat: (token: string, entityCode: string, sessionId: string, message: string) =>
    request<ChatResponse>(`/api/entities/${encodeURIComponent(entityCode)}/chat`, {
      method: "POST",
      token,
      body: JSON.stringify({ sessionId, message }),
    }),

  listDocuments: (token: string, entityCode: string) =>
    request<DocumentResponse[]>(`/api/entities/${encodeURIComponent(entityCode)}/documents`, { token }),

  getDocument: (token: string, entityCode: string, documentId: string) =>
    request<DocumentResponse>(`/api/entities/${encodeURIComponent(entityCode)}/documents/${documentId}`, { token }),

  uploadDocument: (token: string, entityCode: string, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return request<DocumentResponse>(`/api/entities/${encodeURIComponent(entityCode)}/documents`, {
      method: "POST",
      token,
      body: formData,
    });
  },
};
