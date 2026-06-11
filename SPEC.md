You are a senior full-stack migration engineer.

Your task is to migrate the frontend from the provided Repomix XML file into this existing GitHub project:

https://github.com/Patruxs/hotel-booking-service.git

Important context:
- The target GitHub project is an existing Spring Boot backend project.
- Do not rewrite or replace the Spring Boot backend unless it is strictly necessary for frontend integration.
- The uploaded XML is a Repomix-packed representation of another hotel booking codebase.
- The frontend source inside the XML appears under `apps/web`.
- The XML frontend may be based on Next.js, but the target migration goal is React + Vite.
- The correct final structure should be a frontend app inside the existing backend repository.

Target structure:

hotel-booking-service/
├── src/
├── pom.xml
├── Dockerfile
├── infrastructure/
├── .github/
└── frontend/
├── package.json
├── index.html
├── vite.config.ts
├── tsconfig.json
├── src/
└── public/

Main goal:
Migrate the UI, pages, components, API client, assets, and business flows from the XML frontend into a new React + Vite + TypeScript app located at `/frontend`.

Migration rules:
1. Do not copy the XML file itself into the project.
2. Extract only the relevant frontend source from `apps/web`.
3. Convert Next.js-specific code to React + Vite equivalents.
4. Use React Router instead of Next.js App Router.
5. Replace `next/link` with `react-router-dom` `Link`.
6. Replace `next/navigation` with React Router hooks such as `useNavigate`, `useParams`, `useSearchParams`, and `useLocation`.
7. Replace `next/image` with standard `<img>` or a reusable image component.
8. Remove server components, `metadata`, `layout.tsx`, and Next.js-only conventions.
9. Convert route folders from the Next.js `app/` directory into React Router route definitions.
10. Keep reusable components, UI components, forms, validators, hooks, utility functions, constants, and feature modules where possible.
11. Preserve the original visual design as much as possible.
12. Preserve authentication, hotel listing, booking, payment result, dashboard/admin, user profile, reviews, promotions, policies, room types, rooms, inventory, news, contacts, roles, permissions, and other flows if they exist in the frontend.
13. Do not migrate the NestJS backend from the XML.
14. Do not add Prisma, NestJS, Turborepo, or Next.js dependencies to the target backend project.
15. Only add frontend dependencies inside `/frontend`.

Frontend technology requirements:
- React
- Vite
- TypeScript
- React Router
- Axios or the existing API abstraction migrated to Vite
- React Query if already used by the original frontend
- Tailwind CSS if already used by the original frontend
- shadcn/ui-style components may be preserved if present
- Zod and React Hook Form may be preserved if used
- Keep existing UI component architecture where reasonable

Environment variable requirements:
- Use `VITE_API_BASE_URL` for the backend API URL.
- Create `/frontend/.env.example`.
- Do not commit real secrets.

Example:

VITE_API_BASE_URL=http://localhost:8080

API integration requirements:
1. Inspect the Spring Boot backend project to identify actual API endpoints.
2. Compare frontend API calls from the XML with backend endpoints.
3. Update frontend API paths to match the Spring Boot backend.
4. Centralize API configuration in something like:

frontend/src/lib/axios.ts

5. The API client should use:

import.meta.env.VITE_API_BASE_URL

6. Handle authentication tokens consistently.
7. If the frontend expects endpoints that do not exist in the Spring Boot backend, do not invent fake backend logic. Instead:
    - Mark the missing endpoint clearly.
    - Add TODO comments.
    - Create a migration report listing missing backend endpoints.
    - Keep the frontend code structured so those endpoints can be connected later.

CORS requirements:
- Check whether the Spring Boot backend allows requests from the frontend dev server.
- If CORS is missing, add a minimal CORS configuration allowing:
    - http://localhost:5173
    - http://localhost:3000
- Do not weaken security unnecessarily.

Routing requirements:
Create a React Router structure that maps the old Next.js routes to Vite routes.

Examples:
- apps/web/src/app/(public)/page.tsx -> /
- apps/web/src/app/(public)/hotels/page.tsx -> /hotels
- apps/web/src/app/(public)/hotels/[hotel_id]/page.tsx -> /hotels/:hotel_id
- apps/web/src/app/(public)/booking/page.tsx -> /booking
- apps/web/src/app/(public)/login/page.tsx or auth login page -> /login
- apps/web/src/app/(dashboard)/admin/page.tsx -> /admin
- apps/web/src/app/(dashboard)/admin/hotels/page.tsx -> /admin/hotels
- Dynamic folders like `[id]`, `[hotel_id]`, `[booking_id]` must become React Router params.

Create:
- frontend/src/main.tsx
- frontend/src/App.tsx
- frontend/src/routes/index.tsx or frontend/src/router.tsx
- frontend/src/layouts/PublicLayout.tsx
- frontend/src/layouts/AdminLayout.tsx
- frontend/src/layouts/AccountLayout.tsx, if needed

Asset migration:
- Move public assets from the XML frontend public folder into `/frontend/public`.
- Keep image paths working under Vite.
- Fix imports that relied on Next.js public path behavior.

Styling migration:
- Migrate global styles from the old frontend.
- If Tailwind is used, configure:
    - tailwind.config.ts
    - postcss.config.js or postcss.config.cjs
    - src/index.css or src/globals.css
- Ensure all class names and component styles still work.

Package management:
- Add `/frontend/package.json`.
- Include scripts:

{
"dev": "vite",
"build": "tsc -b && vite build",
"preview": "vite preview",
"lint": "eslint ."
}

Root repository integration:
- Do not break Maven/Spring Boot build.
- Add frontend ignore rules to the root `.gitignore`:

frontend/node_modules/
frontend/dist/
frontend/.env
frontend/.env.local
frontend/.env.*.local

Optional but preferred:
- Add README instructions at the root or inside `/frontend/README.md` explaining how to run backend and frontend together.

Expected commands after migration:

Backend:
./mvnw spring-boot:run

Frontend:
cd frontend
npm install
npm run dev

Quality requirements:
1. The project must compile.
2. `npm run build` inside `/frontend` should pass or clearly report only backend-endpoint-related TODOs.
3. Avoid dead imports.
4. Avoid unused Next.js dependencies.
5. Do not leave `next`, `next/image`, `next/link`, or `next/navigation` imports.
6. Do not leave `process.env.NEXT_PUBLIC_*`; convert them to `import.meta.env.VITE_*`.
7. Keep TypeScript strictness reasonable.
8. Prefer small, safe, incremental changes.
9. Preserve existing backend behavior.
10. Document all assumptions.

Final output required:
After making changes, provide:
1. Summary of migrated frontend structure.
2. List of files created or changed.
3. List of Next.js APIs replaced.
4. List of backend API endpoints matched.
5. List of missing backend endpoints, if any.
6. Exact commands to run the app locally.
7. Any remaining TODOs or risks.

Do not claim the migration is complete unless:
- `/frontend` exists.
- dependencies are installed or installable.
- Vite config exists.
- React Router is configured.
- environment variables are documented.
- the frontend build has been attempted.