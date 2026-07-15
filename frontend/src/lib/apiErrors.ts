import { isAxiosError } from "axios";

type ErrorEnvelope = {
  message?: string;
  error?: {
    message?: string;
  };
};

export function getHttpStatus(error: unknown): number | undefined {
  if (isAxiosError(error)) {
    return error.response?.status;
  }
  return undefined;
}

export function isForbiddenError(error: unknown): boolean {
  return getHttpStatus(error) === 403;
}

export function isUnauthorizedError(error: unknown): boolean {
  return getHttpStatus(error) === 401;
}

export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (!isAxiosError(error)) {
    return fallback;
  }

  const data = error.response?.data as ErrorEnvelope | undefined;
  return data?.message ?? data?.error?.message ?? fallback;
}
