"use client";
import { useAuth } from '@/providers/AuthProvider';
import { useRouter } from '@/hooks/navigation';
import { useEffect } from 'react';
interface AdminRouteProps {
  children: React.ReactNode;
}
export function AdminRoute({ children }: AdminRouteProps) {
  const { user, loading } = useAuth();
  const router = useRouter();
  useEffect(() => {
      if (!loading) {
      if (!user || user.roles.length === 0) {
        router.push('/');
      }
    }
  }, [user, loading, router]);
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }
  if (!user || user.roles.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="w-16 h-16 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }
  return <>{children}</>;
}

export default AdminRoute;
