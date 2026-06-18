import { useNavigate } from "react-router-dom";
import { usePermissions } from "@/providers/PermissionProvider";

export function useActionGuard() {
  const navigate = useNavigate();
  const { can } = usePermissions();

  return (action: string) => {
    if (can(action)) {
      return true;
    }
    navigate("/forbidden");
    return false;
  };
}
