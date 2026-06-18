import api from "@/lib/axios";
import { mockLogin } from "@/mocks/mockAuth";
import { mockOrRequest } from "@/features/shared/apiClient";
import { toUser } from "@/features/shared/springMappers";

export type LoginSession = {
  accessToken: string;
  user: ReturnType<typeof toUser>;
};

export const authApi = {
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
  forgotPassword: (body: { email: string }) => mockOrRequest({ ok: true }, () => api.post("/auth/forgot-password", body)),
  resetPassword: (body: unknown) => mockOrRequest({ ok: true }, () => api.post("/auth/reset-password", body)),
  verifyEmail: (body: { token: string }) => mockOrRequest({ ok: true }, () => api.post("/auth/verify-email", body)),
  resend: (body: { email: string }) => mockOrRequest({ ok: true }, () => api.post("/auth/resend", body)),
  refresh: () => mockOrRequest({ accessToken: "mock-access-token" }, () => api.post("/auth/refresh")),
};
