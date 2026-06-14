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

api.interceptors.request.use((config) => {
  const accessToken = Cookies.get("accessToken");
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
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
