// @ts-nocheck
export interface Permission {
  id: string;
  name: string;
  description: string;
}
export interface RolePermission {
  roleId: string;
  permissionId: string;
  permission: Permission;
}
export interface Role {
  id: string;
  name: string;
  description: string;
  permissions: RolePermission[];
}
