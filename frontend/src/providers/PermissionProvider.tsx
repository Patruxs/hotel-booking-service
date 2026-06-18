import { createContext, useContext, useMemo, type PropsWithChildren } from "react";
import { useAuth } from "@/providers/AuthProvider";

type PermissionContextValue = {
  can: (action: string) => boolean;
  canAny: (actions: string[]) => boolean;
  canAll: (actions: string[]) => boolean;
};

const PermissionContext = createContext<PermissionContextValue | null>(null);

export function PermissionProvider({ children }: PropsWithChildren) {
  const { user } = useAuth();
  const allowed = useMemo(() => new Set(user?.allowedActions ?? []), [user]);

  const value = useMemo<PermissionContextValue>(
    () => ({
      can: (action: string) => allowed.has("*") || allowed.has(action),
      canAny: (actions: string[]) => actions.some((action) => allowed.has("*") || allowed.has(action)),
      canAll: (actions: string[]) => actions.every((action) => allowed.has("*") || allowed.has(action)),
    }),
    [allowed],
  );

  return <PermissionContext.Provider value={value}>{children}</PermissionContext.Provider>;
}

export function usePermissions() {
  const context = useContext(PermissionContext);
  if (!context) {
    throw new Error("usePermissions must be used within PermissionProvider");
  }
  return context;
}
