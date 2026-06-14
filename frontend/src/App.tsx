import { BrowserRouter } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { AppRoutes } from "@/router/routes";
import { AuthProvider } from "@/providers/AuthProvider";
import { PermissionProvider } from "@/providers/PermissionProvider";
import { QueryProvider } from "@/providers/QueryProvider";

export default function App() {
  return (
    <QueryProvider>
      <AuthProvider>
        <PermissionProvider>
          <BrowserRouter>
            <AppRoutes />
            <Toaster position="top-right" />
          </BrowserRouter>
        </PermissionProvider>
      </AuthProvider>
    </QueryProvider>
  );
}
