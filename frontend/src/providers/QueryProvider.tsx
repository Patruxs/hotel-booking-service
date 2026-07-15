import { QueryCache, QueryClient, QueryClientProvider } from "@tanstack/react-query";
import type { PropsWithChildren } from "react";
import { getHttpStatus, isForbiddenError } from "@/lib/apiErrors";

const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error) => {
      if (isForbiddenError(error) && typeof window !== "undefined" && window.location.pathname !== "/forbidden") {
        window.location.assign("/forbidden");
      }
    },
  }),
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000,
      retry: (failureCount, error) => {
        const status = getHttpStatus(error);
        if (status === 401 || status === 403) {
          return false;
        }
        return failureCount < 1;
      },
    },
  },
});

export function QueryProvider({ children }: PropsWithChildren) {
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}
