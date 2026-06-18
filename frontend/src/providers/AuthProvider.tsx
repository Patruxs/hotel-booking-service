import Cookies from "js-cookie";
import { createContext, useContext, useMemo, useState, type PropsWithChildren } from "react";
import { authApi } from "@/features/auth/api";
import { setAuthTokens } from "@/lib/axios";
import { bypassAuth, getMockCurrentUser } from "@/mocks/mockAuth";
import type { User } from "@/lib/types";

type AuthContextValue = {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<User | null>(() => {
    const storedUser = localStorage.getItem("currentUser");
    if (storedUser) {
      return JSON.parse(storedUser) as User;
    }
    if (bypassAuth) {
      return getMockCurrentUser();
    }
    return null;
  });

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      login: async (email: string, password: string) => {
        const session = await authApi.login({ email, password });
        setAuthTokens(session.accessToken);
        localStorage.setItem("currentUser", JSON.stringify(session.user));
        setUser(session.user);
      },
      logout: () => {
        Cookies.remove("accessToken");
        localStorage.removeItem("currentUser");
        setUser(null);
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
