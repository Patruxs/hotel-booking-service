import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { CalendarCheck, LayoutDashboard, LogOut, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import Header from "@/components/layouts/Header";
import Footer from "@/components/layouts/Footer";
import AdminHeader from "@/components/layouts/AdminHeader";
import AdminSidebar from "@/components/layouts/AdminSidebar";
import { AccountSidebar } from "@/components/layouts/AccountSidebar";
import { SidebarProvider } from "@/components/ui/sidebar";
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



function navClass({ isActive }: { isActive: boolean }) {
  return cn("rounded-md px-3 py-2 text-sm font-medium hover:bg-muted", isActive && "bg-muted text-foreground");
}

export function PublicLayout() {
  const location = useLocation();
  const isHome = location.pathname === "/";
  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col">
      <Header />
      <main className={cn("flex-1", isHome ? "pt-24 md:pt-0" : "pt-24")}>
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}

export function AdminLayout() {
  const { logout } = useAuth();
  return (
    <SidebarProvider>
      <AdminSidebar />
      <div className="min-h-screen flex-1 bg-muted/30 text-foreground">
        <AdminHeader />
        <main className="p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </SidebarProvider>
  );
}

export function AccountLayout() {
  return (
    <section className="mx-auto grid max-w-7xl gap-6 px-4 py-8 md:grid-cols-[240px_1fr]">
      <AccountSidebar />
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
