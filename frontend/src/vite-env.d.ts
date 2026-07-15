/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string;
  readonly VITE_USE_MOCKS?: string;
  readonly VITE_BYPASS_AUTH?: string;
  readonly VITE_VNPAY_ENABLED?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
