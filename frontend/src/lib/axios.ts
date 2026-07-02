import axios from "axios";
import Cookies from "js-cookie";
import toast from "react-hot-toast";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

let refreshRequest: Promise<string | null> | null = null;

api.interceptors.request.use((config) => {
  const accessToken = Cookies.get("accessToken");
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
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
    if (error.response?.status === 403) {
      toast.error("You do not have permission to access this feature");
      if (window.location.pathname !== "/forbidden") {
        window.location.href = "/forbidden";
      }
    }
    return Promise.reject(error);
  },
);

export function setAuthTokens(accessToken: string) {
  Cookies.set("accessToken", accessToken, { expires: 7, sameSite: "strict" });
  api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
}

export function clearAuthTokens() {
  Cookies.remove("accessToken");
  delete api.defaults.headers.common.Authorization;
}

export default api;
