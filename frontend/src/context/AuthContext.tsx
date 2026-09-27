import React, { createContext, useContext, useState, useCallback, useEffect } from 'react';
import { loginApi, logoutApi } from '../api/authApi';
import type { LoginRequest } from '../types/auth';

export interface AuthUser {
  userId: string;
  username: string;
  role: 'ADMIN' | 'USER';
}

interface AuthContextValue {
  user: AuthUser | null;
  accessToken: string | null;
  login: (data: LoginRequest) => Promise<AuthUser>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

/** Decode JWT payload (no verification – browser only). */
function decodeJwt(token: string): Record<string, unknown> | null {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

function buildUser(
  payload: Record<string, unknown>,
  fallbackUsername?: string
): AuthUser {
  return {
    userId: String(payload['userId'] ?? ''),
    username: String(payload['sub'] ?? fallbackUsername ?? ''),
    role: (String(payload['role'] ?? 'USER').toUpperCase() as 'ADMIN' | 'USER'),
  };
}

function loadFromStorage(): { user: AuthUser | null; token: string | null } {
  try {
    const token = localStorage.getItem('accessToken');
    if (!token) return { user: null, token: null };
    const payload = decodeJwt(token);
    if (!payload) return { user: null, token: null };
    return { user: buildUser(payload), token };
  } catch {
    return { user: null, token: null };
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const { user: storedUser, token: storedToken } = loadFromStorage();
  const [user, setUser] = useState<AuthUser | null>(storedUser);
  const [accessToken, setAccessToken] = useState<string | null>(storedToken);
  useEffect(() => {
    const sync = () => { const session = loadFromStorage(); setUser(session.user); setAccessToken(session.token); };
    window.addEventListener('auth-changed', sync);
    window.addEventListener('storage', sync);
    return () => { window.removeEventListener('auth-changed', sync); window.removeEventListener('storage', sync); };
  }, []);

  const login = useCallback(async (data: LoginRequest): Promise<AuthUser> => {
    const res = await loginApi(data);
    const { accessToken, refreshToken } = res.data.data;

    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);

    const payload = decodeJwt(accessToken);
    const authUser = payload
      ? buildUser(payload, data.username)
      : { userId: '', username: data.username, role: 'USER' as const };

    setAccessToken(accessToken);
    setUser(authUser);
    return authUser;
  }, []);

  const logout = useCallback(async () => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      try { await logoutApi(token); } catch { /* ignore */ }
    }
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setAccessToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{ user, accessToken, login, logout, isAuthenticated: !!accessToken }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// The context hook shares the provider's stable context; changes may trigger a full refresh.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
