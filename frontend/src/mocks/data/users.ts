import type { User } from "@/lib/types";

export const mockAdminUser: User = {
  id: "user-admin",
  name: "Mock Admin",
  email: "admin@example.com",
  phone: "+84 900 000 000",
  roles: [{ id: "role-admin", name: "ADMIN", permissions: ["*"] }],
  allowedActions: ["*"],
};

export const users: User[] = [
  mockAdminUser,
  {
    id: "user-guest",
    name: "Demo Guest",
    email: "guest@example.com",
    roles: [{ id: "role-user", name: "USER", permissions: ["booking:read", "review:create"] }],
    allowedActions: ["booking:read", "review:create"],
  },
];
