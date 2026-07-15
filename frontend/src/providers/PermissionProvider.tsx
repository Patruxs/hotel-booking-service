import { createContext, useContext, useMemo, type PropsWithChildren } from "react";
import { useAuth } from "@/providers/AuthProvider";
import {
  canAccessRequirement,
  type PermissionRequirement,
} from "@/providers/permissionAccess";

type PermissionContextValue = {
  can: (action: string) => boolean;
  canAny: (actions: string[]) => boolean;
  canAll: (actions: string[]) => boolean;
  canAccess: (requirement: PermissionRequirement) => boolean;
};

const PermissionContext = createContext<PermissionContextValue | null>(null);

export function PermissionProvider({ children }: PropsWithChildren) {
  const { user } = useAuth();
  const allowedActions = useMemo(() => user?.allowedActions ?? [], [user]);
  const roleNames = useMemo(() => user?.roles?.map((role) => role.name) ?? [], [user]);

  const value = useMemo<PermissionContextValue>(
    () => ({
      can: (action: string) => canAccessRequirement({ requiredActions: [action] }, allowedActions, roleNames),
      canAny: (actions: string[]) => actions.some((action) => canAccessRequirement({ requiredActions: [action] }, allowedActions, roleNames)),
      canAll: (actions: string[]) => actions.every((action) => canAccessRequirement({ requiredActions: [action] }, allowedActions, roleNames)),
      canAccess: (requirement: PermissionRequirement) => canAccessRequirement(requirement, allowedActions, roleNames),
    }),
    [allowedActions, roleNames],
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

export const usePermission = usePermissions;
