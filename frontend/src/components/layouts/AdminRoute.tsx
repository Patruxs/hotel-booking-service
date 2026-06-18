import { Navigate } from "react-router-dom";
import { bypassAuth } from "@/mocks/mockAuth";
import { useAuth } from "@/providers/AuthProvider";

export function AdminRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, user } = useAuth();
  const isAdmin = user?.roles.some((role) => role.name === "ADMIN");

  if (!bypassAuth && (!isAuthenticated || !isAdmin)) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
