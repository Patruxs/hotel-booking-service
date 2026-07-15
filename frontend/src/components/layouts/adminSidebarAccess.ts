import type { PermissionRequirement } from "../../providers/permissionAccess";

export type SidebarAccessItem = PermissionRequirement & {
  title: string;
  href: string;
  submenu?: readonly SidebarAccessItem[];
};

export function filterSidebarMenu<T extends SidebarAccessItem>(
  menu: readonly T[],
  canAccess: (requirement: PermissionRequirement) => boolean,
): T[] {
  return menu.reduce<T[]>((filtered, item) => {
    const submenu = item.submenu
      ? filterSidebarMenu(item.submenu, canAccess)
      : undefined;
    const hasDirectAccess = canAccess(item);

    if (!hasDirectAccess && !submenu?.length) {
      return filtered;
    }

    filtered.push({
      ...item,
      submenu: submenu?.length ? submenu : undefined,
    } as T);
    return filtered;
  }, []);
}
