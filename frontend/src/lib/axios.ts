import axios from "axios";
import Cookies from "js-cookie";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";
export const AUTH_STATE_CLEARED_EVENT = "hotel-auth-state-cleared";
const USE_MOCKS = import.meta.env.VITE_USE_MOCKS === "true";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

function isCompactJwt(value: unknown): value is string {
  if (typeof value !== 'string') return false;
  const segments = value.split('.');
  return segments.length === 3 && segments.every((segment) => /^[A-Za-z0-9_-]+$/.test(segment));
}

let refreshRequest: Promise<string | null> | null = null;

api.interceptors.request.use((config) => {
  const accessToken = Cookies.get("accessToken");
    if (accessToken && (USE_MOCKS || isCompactJwt(accessToken))) {
      config.headers.Authorization = `Bearer ${accessToken}`;
  } else {
    if (accessToken) clearAuthTokens();
    if (config.headers) {
      delete config.headers.Authorization;
      delete config.headers.authorization;
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !originalRequest.url?.includes("/auth/refresh")) {
      originalRequest._retry = true;
      refreshRequest =
        refreshRequest ??
        api
          .post("/auth/refresh")
          .then((response) => {
            const accessToken = response.data?.data?.accessToken ?? response.data?.accessToken;
            if (accessToken) {
              setAuthTokens(accessToken);
              return accessToken as string;
            }
            return null;
          })
          .catch((err) => {
            console.error('[Auth] Token refresh failed', err);
            clearAuthTokens();
            return null;
          })
          .finally(() => {
            refreshRequest = null;
          });
      const accessToken = await refreshRequest;
      if (accessToken) {
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      }
      clearAuthTokens();
    }
    return Promise.reject(error);
  },
);

export function setAuthTokens(accessToken: string) {
  Cookies.set("accessToken", accessToken, { expires: 7, sameSite: "strict" });
  api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
}

export function hasAuthToken() {
  const accessToken = Cookies.get("accessToken");
  if (USE_MOCKS) return Boolean(accessToken);
  if (isCompactJwt(accessToken)) return true;
  if (accessToken) clearAuthTokens();
  return false;
}

export function clearAuthTokens() {
  Cookies.remove("accessToken");
  delete api.defaults.headers.common.Authorization;
  if (typeof window !== "undefined") {
    window.localStorage.removeItem("currentUser");
    window.dispatchEvent(new Event(AUTH_STATE_CLEARED_EVENT));
  }
}

export default api;
