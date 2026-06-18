import { useMocks } from "@/mocks/mockApi";

type SpringApiResponse<T> = {
  status?: number;
  message?: string;
  data?: T;
};

function unwrapResponse<T>(payload: T | SpringApiResponse<T>): T {
  if (payload && typeof payload === "object" && "data" in payload && ("status" in payload || "message" in payload)) {
    return (payload as SpringApiResponse<T>).data as T;
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
