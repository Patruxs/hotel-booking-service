import { BarChart3, BedDouble, Building2, CalendarCheck, Settings, Users } from "lucide-react";
import { NavLink, Outlet } from "react-router-dom";

const adminItems = [
  { to: "/admin", label: "Dashboard", icon: BarChart3 },
  { to: "/admin/hotels", label: "Hotels", icon: Building2 },
  { to: "/admin/bookings", label: "Bookings", icon: CalendarCheck },
  { to: "/admin/room-types", label: "Rooms", icon: BedDouble },
  { to: "/admin/inventory", label: "Inventory", icon: BedDouble },
  { to: "/admin/users", label: "Users", icon: Users },
  { to: "/admin/settings", label: "Settings", icon: Settings },
];

export function AdminLayout() {
  return (
    <div className="admin-shell">
      <aside className="sidebar">
        <div className="sidebar-title">Stayra Admin</div>
        {adminItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink key={item.to} to={item.to} end={item.to === "/admin"}>
              <Icon size={18} />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
