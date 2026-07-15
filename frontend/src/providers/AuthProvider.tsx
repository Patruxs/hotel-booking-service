import { createContext, useCallback, useContext, useEffect, useMemo, useState, type PropsWithChildren } from "react";
import { authApi } from "@/features/auth/api";
import { AUTH_STATE_CLEARED_EVENT, clearAuthTokens, hasAuthToken, setAuthTokens } from "@/lib/axios";
import { bypassAuth, getMockCurrentUser } from "@/mocks/mockAuth";
import type { User } from "@/lib/types";

type AuthContextValue = {
  user: User | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<User>;
  register: (body: unknown) => Promise<void>;
  logout: () => void;
  loadUser: () => Promise<void>;
  forgotPassword: (email: string) => Promise<void>;
  resetPassword: (token: string, password: string, confirmPassword: string) => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<User | null>(() => (bypassAuth ? getMockCurrentUser() : null));
  const [loading, setLoading] = useState(!bypassAuth);

  const clearLocalAuth = useCallback(() => {
    clearAuthTokens();
    setUser(null);
  }, []);

  const loadUser = useCallback(async () => {
    if (bypassAuth) {
      setUser(getMockCurrentUser());
      setLoading(false);
      return;
    }

      try {
        setLoading(true);
        if (!hasAuthToken()) {
          setUser(null);
          return;
        }
        const current = await authApi.me();
        setUser(current);
    } catch (error) {
      clearLocalAuth();
      throw error;
    } finally {
      setLoading(false);
    }
  }, [clearLocalAuth]);

  useEffect(() => {
    const handleAuthCleared = () => {
      setUser(null);
      setLoading(false);
    };

    window.addEventListener(AUTH_STATE_CLEARED_EVENT, handleAuthCleared);
    void loadUser().catch(() => undefined);

    return () => window.removeEventListener(AUTH_STATE_CLEARED_EVENT, handleAuthCleared);
  }, [loadUser]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      isAuthenticated: Boolean(user),
      login: async (email: string, password: string) => {
        setLoading(true);
        try {
          const session = await authApi.login({ email, password });
          setAuthTokens(session.accessToken);
          setUser(session.user);
          return session.user;
        } finally {
          setLoading(false);
        }
      },
      register: async (body: unknown) => {
        await authApi.register(body);
      },
      logout: () => {
        void authApi.logout().finally(clearLocalAuth);
      },
      loadUser,
      forgotPassword: async (email: string) => {
        await authApi.forgotPassword({ email });
      },
      resetPassword: async (token: string, password: string, confirmPassword: string) => {
        await authApi.resetPassword({ token, password, confirmPassword });
      },
    }),
    [clearLocalAuth, loadUser, loading, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
