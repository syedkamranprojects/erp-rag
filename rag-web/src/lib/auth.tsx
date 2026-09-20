import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import { api } from "./api";
import { decodeJwt, isExpired } from "./jwt";

const STORAGE_KEY = "erp-rag-token";

export interface AuthUser {
  token: string;
  username: string;
  entityCode: string;
  role: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  login: (entityCode: string, username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function loadStoredUser(): AuthUser | null {
  const token = localStorage.getItem(STORAGE_KEY);
  if (!token) return null;
  const claims = decodeJwt(token);
  if (!claims || isExpired(claims)) {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
  return { token, username: claims.sub, entityCode: claims.entityCode, role: claims.role };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => loadStoredUser());

  const login = useCallback(async (entityCode: string, username: string, password: string) => {
    const response = await api.login({ entityCode, username, password });
    const claims = decodeJwt(response.token);
    if (!claims) {
      throw new Error("Received an invalid token from the server");
    }
    localStorage.setItem(STORAGE_KEY, response.token);
    setUser({ token: response.token, username: claims.sub, entityCode: claims.entityCode, role: claims.role });
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY);
    setUser(null);
  }, []);

  const value = useMemo(() => ({ user, login, logout }), [user, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
