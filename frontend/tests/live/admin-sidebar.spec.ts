import { expect, test } from "@playwright/test";
import { filterSidebarMenu, type SidebarAccessItem } from "../../src/components/layouts/adminSidebarAccess";
import { canAccessRequirement } from "../../src/providers/permissionAccess";
import {
  ADMIN_ONLY_REQUIREMENT,
  hasManagementShellRole,
} from "../../src/providers/permissionAccess";

test.describe("admin sidebar access model", () => {
  test("matches canonical actions and normalized roles", () => {
    expect(canAccessRequirement({ requiredActions: ["reports.dashboard"] }, ["reports.dashboard"], [])).toBeTruthy();
    expect(canAccessRequirement({ requiredActions: ["dashboard.read"] }, ["reports.dashboard"], [])).toBeFalsy();
    expect(canAccessRequirement({ requiredActions: ["missing.action"] }, ["*"], [])).toBeTruthy();
    expect(canAccessRequirement({ requiredRoles: ["ADMIN"] }, [], ["ROLE_ADMIN"])).toBeTruthy();
    expect(canAccessRequirement({ requiredRoles: ["ADMIN"] }, [], ["CUSTOMER"])).toBeFalsy();
    expect(canAccessRequirement(undefined, ["*"], ["ADMIN"])).toBeFalsy();
  });

  test("admits normalized owners while keeping admin-only routes denied", () => {
    expect(hasManagementShellRole(["OWNER"])).toBeTruthy();
    expect(hasManagementShellRole([" role_owner "])).toBeTruthy();
    expect(hasManagementShellRole(["CUSTOMER"])).toBeFalsy();
    expect(canAccessRequirement(ADMIN_ONLY_REQUIREMENT, ["hotels.manage"], ["OWNER"])).toBeFalsy();
    expect(canAccessRequirement(ADMIN_ONLY_REQUIREMENT, [], ["ROLE_ADMIN"])).toBeTruthy();
    expect(canAccessRequirement({ requiredActions: ["hotels.manage"] }, ["hotels.manage"], ["OWNER"])).toBeTruthy();
    expect(canAccessRequirement({ requiredActions: ["reports.hotel.view"] }, ["reports.hotel.view"], ["OWNER"])).toBeTruthy();
  });

  test("filters inaccessible children and removes empty parents", () => {
    const menu: SidebarAccessItem[] = [
      {
        title: "Hotel",
        href: "/admin/hotels",
        requiredActions: ["hotels.manage"],
        submenu: [
          { title: "Hotels", href: "/admin/hotels", requiredActions: ["hotels.manage"] },
          { title: "Members", href: "/admin/member-hotels", requiredActions: ["hotel.members.manage"] },
        ],
      },
      {
        title: "Hidden",
        href: "/admin/hidden",
        requiredActions: ["missing.action"],
      },
    ];

    const filtered = filterSidebarMenu(menu, (requirement) =>
      canAccessRequirement(requirement, ["hotels.manage"], []),
    );

    expect(filtered).toHaveLength(1);
    expect(filtered[0].title).toBe("Hotel");
    expect(filtered[0].submenu?.map((item) => item.title)).toEqual(["Hotels"]);
  });

  test("removes an empty submenu while retaining an accessible parent", () => {
    const menu: SidebarAccessItem[] = [
      {
        title: "Users",
        href: "/admin/users",
        requiredRoles: ["ADMIN"],
        submenu: [
          { title: "Actions", href: "/admin/users/actions", requiredActions: ["missing.action"] },
        ],
      },
    ];

    const filtered = filterSidebarMenu(menu, (requirement) =>
      canAccessRequirement(requirement, [], ["ADMIN"]),
    );

    expect(filtered).toHaveLength(1);
    expect(filtered[0].submenu).toBeUndefined();
  });
});

test("live administrator sidebar remains visible after login and reload", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Email").fill("admin@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("admin123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin/);

  const menu = page.locator('[data-sidebar="menu"]');
  await expect(menu.locator('[data-sidebar="menu-item"]').first()).toBeVisible();
  await expect(menu).toContainText("Dashboard");
  await expect(menu).toContainText("Hotel");
  await expect(menu).toContainText("Room types");
  await expect(menu).toContainText("Bookings");
  await expect(menu).not.toContainText("Users");

  await page.reload();
  await expect(page).toHaveURL(/\/admin/);
  await expect(menu.locator('[data-sidebar="menu-item"]').first()).toBeVisible();
  await expect(menu).toContainText("Dashboard");
});
