import { Link, NavLink, Outlet } from "react-router-dom";
import { Building2, CalendarCheck, LayoutDashboard, LogOut, Menu, UserRound } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/providers/AuthProvider";
import { cn } from "@/lib/utils";

const publicLinks = [
  ["/hotels", "Hotels"],
  ["/news", "News"],
  ["/contact", "Contact"],
  ["/partner", "Partner"],
];

const adminLinks = [
  ["/admin", "Overview"],
  ["/admin/hotels", "Hotels"],
  ["/admin/bookings", "Bookings"],
  ["/admin/inventory", "Inventory"],
  ["/admin/users", "Users"],
  ["/admin/promotions", "Promotions"],
  ["/admin/settings", "Settings"],
];

const accountLinks = [
  ["/me", "Profile"],
  ["/me/my-bookings", "My bookings"],
  ["/me/my-reviews", "My reviews"],
];

function navClass({ isActive }: { isActive: boolean }) {
  return cn("rounded-md px-3 py-2 text-sm font-medium hover:bg-muted", isActive && "bg-muted text-foreground");
}

export function PublicLayout() {
  const { user } = useAuth();
  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="sticky top-0 z-20 border-b bg-background/95 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
          <Link to="/" className="flex items-center gap-2 font-semibold">
            <Building2 className="h-5 w-5 text-primary" />
            Hotel Booking
          </Link>
          <nav className="hidden items-center gap-1 md:flex">
            {publicLinks.map(([to, label]) => (
              <NavLink key={to} to={to} className={navClass}>
                {label}
              </NavLink>
            ))}
          </nav>
          <div className="flex items-center gap-2">
            <Link to="/me" className="hidden text-sm text-muted-foreground sm:inline">
              {user?.name ?? "Account"}
            </Link>
            <Button asChild variant="secondary">
              <Link to="/login">Login</Link>
            </Button>
          </div>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
      <footer className="border-t py-8">
        <div className="mx-auto flex max-w-7xl flex-col gap-2 px-4 text-sm text-muted-foreground md:flex-row md:items-center md:justify-between">
          <span>Vite React migration shell for the old apps/web UI.</span>
          <span>Mock mode keeps public and admin flows inspectable.</span>
        </div>
      </footer>
    </div>
  );
}

export function AdminLayout() {
  const { logout } = useAuth();
  return (
    <div className="min-h-screen bg-muted/30 text-foreground lg:grid lg:grid-cols-[260px_1fr]">
      <aside className="border-r bg-background">
        <div className="flex h-16 items-center gap-2 border-b px-5 font-semibold">
          <LayoutDashboard className="h-5 w-5 text-primary" />
          Admin
        </div>
        <nav className="grid gap-1 p-3">
          {adminLinks.map(([to, label]) => (
            <NavLink key={to} to={to} end={to === "/admin"} className={navClass}>
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div>
        <header className="flex h-16 items-center justify-between border-b bg-background px-4">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Menu className="h-4 w-4" />
            Dashboard workspace
          </div>
          <Button variant="ghost" onClick={logout}>
            <LogOut className="mr-2 h-4 w-4" />
            Logout
          </Button>
        </header>
        <main className="p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export function AccountLayout() {
  return (
    <section className="mx-auto grid max-w-7xl gap-6 px-4 py-8 md:grid-cols-[240px_1fr]">
      <aside className="rounded-lg border bg-card p-3">
        <div className="mb-3 flex items-center gap-2 px-2 font-semibold">
          <UserRound className="h-4 w-4 text-primary" />
          My account
        </div>
        <nav className="grid gap-1">
          {accountLinks.map(([to, label]) => (
            <NavLink key={to} to={to} end={to === "/me"} className={navClass}>
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div>
        <Outlet />
      </div>
    </section>
  );
}

export function RouteHeader({ title, description }: { title: string; description?: string }) {
  return (
    <div className="mb-6">
      <div className="mb-2 flex items-center gap-2 text-sm text-muted-foreground">
        <CalendarCheck className="h-4 w-4" />
        Migrated route
      </div>
      <h1 className="text-2xl font-semibold tracking-normal md:text-3xl">{title}</h1>
      {description ? <p className="mt-2 max-w-3xl text-muted-foreground">{description}</p> : null}
    </div>
  );
}
