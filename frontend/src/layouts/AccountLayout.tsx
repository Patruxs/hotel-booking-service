import { NavLink, Outlet } from "react-router-dom";

export function AccountLayout() {
  return (
    <div className="account-shell">
      <aside className="account-nav">
        <NavLink to="/me" end>
          Profile
        </NavLink>
        <NavLink to="/me/my-bookings">My bookings</NavLink>
        <NavLink to="/me/my-reviews">My reviews</NavLink>
      </aside>
      <section className="account-main">
        <Outlet />
      </section>
    </div>
  );
}
