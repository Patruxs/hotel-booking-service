import { Building2, CalendarCheck, CircleDollarSign } from "lucide-react";
import { useAsync } from "@/hooks/useAsync";
import { getAdminBookings, getHotels, getRevenue } from "@/services/hotelApi";

export function AdminDashboardPage() {
  const hotels = useAsync(getHotels, []);
  const bookings = useAsync(getAdminBookings, []);
  const revenue = useAsync(getRevenue, []);
  const totalRevenue = revenue.data?.reduce((sum, item) => sum + Number(item.totalRevenue ?? 0), 0) ?? 0;

  return (
    <section>
      <p className="eyebrow">Operations</p>
      <h1>Admin dashboard</h1>
      <div className="metric-grid">
        <article>
          <Building2 size={22} />
          <span>Hotels</span>
          <strong>{hotels.data?.length ?? "-"}</strong>
        </article>
        <article>
          <CalendarCheck size={22} />
          <span>Bookings</span>
          <strong>{bookings.data?.length ?? "-"}</strong>
        </article>
        <article>
          <CircleDollarSign size={22} />
          <span>Revenue</span>
          <strong>{totalRevenue ? totalRevenue.toLocaleString() : "-"}</strong>
        </article>
      </div>
      {hotels.error || bookings.error || revenue.error ? (
        <p className="error">Some admin metrics require an authenticated ADMIN or RECEPTIONIST token.</p>
      ) : null}
    </section>
  );
}
