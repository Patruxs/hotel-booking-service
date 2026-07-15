"use client";
import { useAuth } from '@/providers/AuthProvider';
import { Navigate, useLocation } from 'react-router-dom';
import { hasManagementShellRole } from '@/providers/permissionAccess';
interface AdminRouteProps {
  children: React.ReactNode;
}

export function AdminRoute({ children }: AdminRouteProps) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (!hasManagementShellRole(user.roles.map((role) => role.name))) {
    return <Navigate to="/forbidden" replace />;
  }
  return <>{children}</>;
}

export default AdminRoute;
