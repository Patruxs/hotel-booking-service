import { useMocks } from "@/mocks/mockApi";

type SpringApiResponse<T> = {
  status?: number;
  message?: string;
  data?: T;
};

function unwrapResponse<T>(payload: T | SpringApiResponse<T>): T {
  if (payload && typeof payload === "object" && "data" in payload && ("status" in payload || "message" in payload)) {
    const springPayload = payload as SpringApiResponse<T>;
    if (typeof springPayload.status === "number" && springPayload.status >= 400) {
      throw new Error(springPayload.message || "Spring API request failed");
    }
    return springPayload.data as T;
  }
  return payload as T;
}

export async function mockOrRequest<T>(mockValue: T, request: () => Promise<{ data: unknown }>): Promise<T> {
  if (useMocks) {
    return Promise.resolve(mockValue);
  }
  const response = await request();
  return unwrapResponse<T>(response.data as T | SpringApiResponse<T>);
}

export async function mockOnly<T>(mockValue: T): Promise<T> {
  if (!useMocks) {
    throw new Error("mockOnly adapter called while VITE_USE_MOCKS is not true");
  }
  return Promise.resolve(mockValue);
}
