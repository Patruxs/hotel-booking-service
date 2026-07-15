export type PermissionRequirement = {
  requiredActions?: readonly string[];
  requiredRoles?: readonly string[];
};

export const MANAGEMENT_SHELL_ROLES = ["ADMIN", "OWNER", "MANAGER", "RECEPTIONIST"] as const;
export const ADMIN_ONLY_REQUIREMENT = { requiredRoles: ["ADMIN"] } as const;
export const OWNER_ONLY_REQUIREMENT = { requiredRoles: ["ADMIN", "OWNER"] } as const;

export function hasManagementShellRole(roleNames: readonly string[] = []) {
  const managementRoles = new Set<string>(MANAGEMENT_SHELL_ROLES);
  return roleNames.some((roleName) => managementRoles.has(normalizeRoleName(roleName)));
}

export function normalizeRoleName(roleName: string) {
  return roleName.trim().toUpperCase().replace(/^ROLE_/, "");
}

export function canAccessRequirement(
  requirement: PermissionRequirement | undefined,
  allowedActions: readonly string[] = [],
  roleNames: readonly string[] = [],
) {
  if (!requirement) {
    return false;
  }

  const requiredActions = (requirement.requiredActions ?? [])
    .map((action) => action.trim())
    .filter(Boolean);
  const requiredRoles = (requirement.requiredRoles ?? [])
    .map(normalizeRoleName)
    .filter(Boolean);

  if (requiredActions.length === 0 && requiredRoles.length === 0) {
    return false;
  }

  const allowed = new Set(allowedActions.map((action) => action.trim()));
  const roles = new Set(roleNames.map(normalizeRoleName));

  return (
    requiredActions.some((action) => allowed.has("*") || allowed.has(action)) ||
    requiredRoles.some((role) => roles.has(role))
  );
}
