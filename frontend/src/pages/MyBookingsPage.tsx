import { useParams } from "react-router-dom";
import { useAsync } from "@/hooks/useAsync";
import { getUserBookings } from "@/services/hotelApi";

export function MyBookingsPage({ detail = false }: { detail?: boolean }) {
  const { bookingId } = useParams();
  const { data, error, loading } = useAsync(getUserBookings, []);
  const bookings = data ?? [];
  const visible = detail ? bookings.filter((booking) => String(booking.id) === bookingId) : bookings;

  return (
    <div>
      <p className="eyebrow">Reservations</p>
      <h1>{detail ? "Booking detail" : "My bookings"}</h1>
      {loading ? <p className="muted">Loading bookings...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      <div className="stack">
        {visible.map((booking) => (
          <article className="list-card" key={booking.id}>
            <div>
              <h3>{booking.bookingReference || `Booking ${booking.id}`}</h3>
              <p>{booking.hotel?.name || booking.customerName || "Hotel booking"}</p>
            </div>
            <div className="list-card-actions">
              <span>{booking.checkinDate} to {booking.checkoutDate}</span>
              <strong>{booking.status}</strong>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
