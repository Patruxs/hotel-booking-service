import api from "@/lib/axios";
import { mockLogin } from "@/mocks/mockAuth";
import { mockOnly, mockOrRequest } from "@/features/shared/apiClient";
import { toUser } from "@/features/shared/springMappers";

export type LoginSession = {
  accessToken: string;
  user: ReturnType<typeof toUser>;
};

export const authApi: any = {
  login: async (body: { email: string; password: string }): Promise<LoginSession> => {
    const session = await mockOrRequest(mockLogin(), () => api.post("/auth/login", body));
    if ("accessToken" in session) {
      return session;
    }
    const raw = session as unknown as { token: string; role?: string };
    return {
      accessToken: raw.token,
      user: toUser({ email: body.email }, raw.role),
    };
  },
  logout: () => mockOrRequest({ ok: true }, () => api.post("/auth/logout")),
  register: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/auth/register", body)),
  forgotPassword: (_body: { email: string }) => mockOnly({ ok: true }),
  resetPassword: (_body: unknown) => mockOnly({ ok: true }),
  verifyEmail: (_body: { token: string }) => mockOnly({ ok: true }),
  resend: (_body: { email: string }) => mockOnly({ ok: true }),
  refresh: () => mockOnly({ accessToken: "mock-access-token" }),
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
