# Hotel Booking Frontend

This is the React + Vite + TypeScript frontend migrated from the Repomix XML source under `apps/web`.

## Run Locally

Start the Spring Boot backend from the repository root:

```bash
./mvnw spring-boot:run
```

Start the frontend:

```bash
cd frontend
npm install
npm run dev
```

The frontend reads the backend URL from:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Use `http://localhost:5173` during development so it matches the backend CORS configuration.

## Migration Notes

- Routes from the XML `apps/web/src/app` tree are mapped in `src/App.tsx`.
- API configuration lives in `src/lib/axios.ts`.
- Spring Boot endpoint adapters live in `src/services/hotelApi.ts`.
- Missing XML-era backend endpoints are listed in `MIGRATION_REPORT.md`.
