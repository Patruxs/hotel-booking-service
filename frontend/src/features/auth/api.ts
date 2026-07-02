import api from "@/lib/axios";
import { mockLogin } from "@/mocks/mockAuth";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toUser } from "@/features/shared/springMappers";
import type { User } from "@/lib/types";
import { setAuthTokens } from "@/lib/axios";

export type LoginSession = {
  accessToken: string;
  user: User;
};

type TokenResponse = {
  accessToken: string;
  jti: string;
  tokenType: string;
};

export const authApi: any = {
  login: async (body: { email: string; password: string }): Promise<LoginSession> => {
    const session = await mockOrRequest(mockLogin(), () => api.post("/auth/login", body));
    if ("user" in session) {
      return session as LoginSession;
    }
    const raw = session as unknown as TokenResponse | { token: string; role?: string };
    const accessToken = "accessToken" in raw ? raw.accessToken : raw.token;
    setAuthTokens(accessToken);
    return {
      accessToken,
      user: await authApi.me(accessToken, body.email, "role" in raw ? raw.role : undefined),
    };
  },
  logout: () => mockOrRequest({ ok: true }, () => api.post("/auth/logout")),
  register: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/auth/register", body)),
  forgotPassword: (body: { email: string }) => mockOrRequest({ ok: true }, () => api.post("/auth/forgot-password", body)),
  resetPassword: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/auth/reset-password", body)),
  verifyEmail: (body: { token: string }) => mockOrRequest({ ok: true }, () => api.post("/auth/verify-email", body)),
  resend: (body: { email: string }) => mockOrRequest({ ok: true }, () => api.post("/auth/resend", body)),
  refresh: () => mockOrRequest({ accessToken: "mock-access-token", jti: "mock", tokenType: "Bearer" }, () => api.post("/auth/refresh")),
  me: (_accessToken?: string, fallbackEmail?: string, fallbackRole?: string): Promise<User> =>
    mockOrRequest<User>(toUser({ email: fallbackEmail }, fallbackRole), () => api.get("/users/me")),
};

export const login = (body: { email: string; password: string }) => authApi.login(body);
export const logout = () => authApi.logout();
export const register = (body: unknown) => authApi.register(body);
export const forgotPassword = (email: string) => authApi.forgotPassword({ email });
export const resetPassword = (token: string, password: string, confirmPassword?: string) =>
  authApi.resetPassword({ token, password, confirmPassword });
export const verifyEmail = (token: string) => authApi.verifyEmail({ token });
export const resendVerificationEmail = (email: string) => authApi.resend({ email });
export const refreshToken = () => authApi.refresh();
export const getCurrentUser = () => authApi.me();
