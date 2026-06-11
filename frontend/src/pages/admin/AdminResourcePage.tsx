import { Link, useParams } from "react-router-dom";
import { useAsync } from "@/hooks/useAsync";
import { getAdminBookings, getAmenities, getHotels } from "@/services/hotelApi";

export function AdminResourcePage({ resource, unsupported }: { resource: string; unsupported?: string }) {
  const params = useParams();
  const normalized = resource.toLowerCase();
  const shouldLoadHotels = normalized.includes("hotel");
  const shouldLoadBookings = normalized.includes("booking");
  const shouldLoadAmenities = normalized.includes("amenit");
  const hotels = useAsync(() => (shouldLoadHotels ? getHotels() : Promise.resolve([])), [resource]);
  const bookings = useAsync(() => (shouldLoadBookings ? getAdminBookings() : Promise.resolve([])), [resource]);
  const amenities = useAsync(() => (shouldLoadAmenities ? getAmenities() : Promise.resolve([])), [resource]);

  return (
    <section>
      <p className="eyebrow">Admin</p>
      <h1>{titleCase(resource)}</h1>
      {unsupported ? <p className="todo">TODO: Missing backend support for {unsupported}.</p> : null}
      {Object.keys(params).length ? <p className="muted">Route params: {JSON.stringify(params)}</p> : null}
      {shouldLoadHotels ? (
        <div className="stack">
          {hotels.data?.map((hotel) => (
            <article className="list-card" key={hotel.id}>
              <div>
                <h3>{hotel.name}</h3>
                <p>{hotel.location}</p>
              </div>
              <Link to={`/admin/hotels/${hotel.id}`}>Open</Link>
            </article>
          ))}
        </div>
      ) : null}
      {shouldLoadBookings ? (
        <div className="stack">
          {bookings.data?.map((booking) => (
            <article className="list-card" key={booking.id}>
              <div>
                <h3>{booking.bookingReference || `Booking ${booking.id}`}</h3>
                <p>{booking.customerName || booking.customerEmail || "Guest"}</p>
              </div>
              <strong>{booking.status}</strong>
            </article>
          ))}
        </div>
      ) : null}
      {shouldLoadAmenities ? (
        <div className="stack">
          {amenities.data?.map((amenity) => (
            <article className="list-card" key={amenity.id}>
              <div>
                <h3>{amenity.name}</h3>
                <p>{amenity.type || "Amenity"}</p>
              </div>
            </article>
          ))}
        </div>
      ) : null}
      {!shouldLoadHotels && !shouldLoadBookings && !shouldLoadAmenities && !unsupported ? (
        <p className="muted">This route is preserved from the XML frontend and is ready for deeper UI migration.</p>
      ) : null}
    </section>
  );
}

function titleCase(value: string) {
  return value.replace(/\b\w/g, (char) => char.toUpperCase());
}
