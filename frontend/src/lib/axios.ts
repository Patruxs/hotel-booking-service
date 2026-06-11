import axios from "axios";
import Cookies from "js-cookie";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
export const AUTH_TOKEN_KEY = "hotel_booking_token";

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(AUTH_TOKEN_KEY) || Cookies.get(AUTH_TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export function setAuthToken(token: string) {
  localStorage.setItem(AUTH_TOKEN_KEY, token);
  Cookies.set(AUTH_TOKEN_KEY, token, { sameSite: "lax" });
}

export function clearAuthToken() {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  Cookies.remove(AUTH_TOKEN_KEY);
}

export function unwrapResponse<T>(payload: ApiEnvelope<T> | T): T {
  if (payload && typeof payload === "object" && "data" in payload) {
    return (payload as ApiEnvelope<T>).data;
  }
  return payload as T;
}

export type ApiEnvelope<T> = {
  status?: number;
  message?: string;
  data: T;
};
