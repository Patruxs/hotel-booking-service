import Cookies from "js-cookie";
import { mockAdminUser } from "@/mocks/data/users";

export const bypassAuth =
  import.meta.env.VITE_USE_MOCKS === "true" && import.meta.env.VITE_BYPASS_AUTH === "true";

export function getMockCurrentUser() {
  return mockAdminUser;
}

export function mockLogin() {
  Cookies.set("accessToken", "mock-access-token", { expires: 7, sameSite: "strict" });
  return { accessToken: "mock-access-token", user: mockAdminUser };
}
