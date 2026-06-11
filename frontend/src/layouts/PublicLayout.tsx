import { Hotel, LogIn, UserRound } from "lucide-react";
import { Link, NavLink, Outlet } from "react-router-dom";

const navItems = [
  { to: "/hotels", label: "Hotels" },
  { to: "/booking", label: "Book" },
  { to: "/news", label: "News" },
  { to: "/contact", label: "Contact" },
  { to: "/partner", label: "Partner" },
];

export function PublicLayout() {
  return (
    <div className="app-shell">
      <header className="topbar">
        <Link className="brand" to="/">
          <Hotel size={24} />
          <span>Stayra</span>
        </Link>
        <nav>
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="topbar-actions">
          <Link className="icon-link" to="/login" title="Login">
            <LogIn size={18} />
          </Link>
          <Link className="icon-link" to="/me" title="Account">
            <UserRound size={18} />
          </Link>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
