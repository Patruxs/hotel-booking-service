import Cookies from "js-cookie";
import { createContext, useContext, useMemo, useState, type PropsWithChildren } from "react";
import { authApi } from "@/features/auth/api";
import { setAuthTokens } from "@/lib/axios";
import { bypassAuth, getMockCurrentUser } from "@/mocks/mockAuth";
import type { User } from "@/lib/types";

type AuthContextValue = {
  user: User | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (body: unknown) => Promise<void>;
  logout: () => void;
  loadUser: () => Promise<void>;
  forgotPassword: (email: string) => Promise<void>;
  resetPassword: (token: string, password: string, confirmPassword: string) => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function normalizeStoredUser(value: unknown): User | null {
  if (!value || typeof value !== "object") {
    return null;
  }

  const fallbackUser = getMockCurrentUser();
  const storedUser = value as Partial<User>;

  if (!storedUser.id || !storedUser.email) {
    return null;
  }

  return {
    ...fallbackUser,
    ...storedUser,
    name: storedUser.name ?? fallbackUser.name,
    roles: Array.isArray(storedUser.roles) ? storedUser.roles : fallbackUser.roles,
    allowedActions: Array.isArray(storedUser.allowedActions)
      ? storedUser.allowedActions
      : fallbackUser.allowedActions,
  };
}

function readStoredUser(): User | null {
  if (typeof window === "undefined") {
    return null;
  }

  const storedUser = window.localStorage.getItem("currentUser");
  if (!storedUser) {
    return null;
  }

  try {
    const parsedUser: unknown = JSON.parse(storedUser);
    const user = normalizeStoredUser(parsedUser);

    if (!user) {
      window.localStorage.removeItem("currentUser");
    }

    return user;
  } catch {
    window.localStorage.removeItem("currentUser");
    return null;
  }
}

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<User | null>(() => {
    const storedUser = readStoredUser();
    if (storedUser) {
      return storedUser;
    }
    if (bypassAuth) {
      return getMockCurrentUser();
    }
    return null;
  });

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading: false,
      isAuthenticated: Boolean(user),
      login: async (email: string, password: string) => {
        const session = await authApi.login({ email, password });
        setAuthTokens(session.accessToken);
        localStorage.setItem("currentUser", JSON.stringify(session.user));
        setUser(session.user);
      },
      register: async (body: unknown) => {
        await authApi.register(body);
      },
      logout: () => {
        Cookies.remove("accessToken");
        localStorage.removeItem("currentUser");
        setUser(null);
      },
      loadUser: async () => {
        const current = getMockCurrentUser();
        localStorage.setItem("currentUser", JSON.stringify(current));
        setUser(current);
      },
      forgotPassword: async (email: string) => {
        await authApi.forgotPassword({ email });
      },
      resetPassword: async (token: string, password: string, confirmPassword: string) => {
        await authApi.resetPassword({ token, password, confirmPassword });
      },
    }),
    [user],
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
