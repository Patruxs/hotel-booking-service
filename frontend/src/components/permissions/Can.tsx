import type { PropsWithChildren } from "react";
import { usePermissions } from "@/providers/PermissionProvider";

type CanProps = PropsWithChildren<{
  action: string;
  fallback?: React.ReactNode;
}>;

export function Can({ action, children, fallback = null }: CanProps) {
  const { can } = usePermissions();
  return can(action) ? <>{children}</> : <>{fallback}</>;
}
