# Hotel Booking Frontend

Vite + React + TypeScript SPA migrated from `apps/web` in `repomix-output-kinyias-hotel-booking-web.git (1).xml`.

## Stack

- Vite, React, TypeScript, React Router
- TanStack React Query, Axios, js-cookie
- Tailwind CSS v4, shadcn/ui-style primitives, Radix UI, lucide-react
- React Hook Form, Zod, React Hot Toast
- TipTap dependencies retained for `/editor` and `/simple`

## Local

```bash
npm install
npm run dev
npm run typecheck
npm run build
```

Default inspection variables:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_USE_MOCKS=true
VITE_BYPASS_AUTH=true
```

Production-like real-backend variables:

```bash
VITE_API_BASE_URL=/api/v1
VITE_USE_MOCKS=false
VITE_BYPASS_AUTH=false
```

The app uses `BrowserRouter`; Nginx/static hosting needs fallback to `index.html`.
