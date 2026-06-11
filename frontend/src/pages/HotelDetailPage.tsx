import { Link, useParams } from "react-router-dom";
import { BedDouble, Star } from "lucide-react";
import { useAsync } from "@/hooks/useAsync";
import { getHotel, getHotelRooms } from "@/services/hotelApi";

export function HotelDetailPage() {
  const { hotelId = "" } = useParams();
  const hotelState = useAsync(() => getHotel(hotelId), [hotelId]);
  const roomsState = useAsync(() => getHotelRooms(hotelId), [hotelId]);
  const hotel = hotelState.data;
  const rooms = roomsState.data ?? [];

  if (hotelState.loading) return <section className="section">Loading hotel...</section>;
  if (!hotel) return <section className="section">Hotel not found.</section>;

  return (
    <section className="section detail-layout">
      <div>
        <img className="detail-image" src={hotel.coverImage || hotel.images?.[0] || "/window.svg"} alt={hotel.name} />
      </div>
      <div>
        <p className="eyebrow">{hotel.location}</p>
        <h1>{hotel.name}</h1>
        <p>{hotel.description || "No description has been provided for this hotel yet."}</p>
        <div className="stat-row">
          <span>
            <Star size={16} /> {hotel.starRating ?? "N/A"} stars
          </span>
          <span>
            <BedDouble size={16} /> {rooms.length} room types
          </span>
        </div>
        <h2>Rooms</h2>
        <div className="stack">
          {rooms.map((room) => (
            <article className="list-card" key={room.id}>
              <div>
                <h3>{room.name || room.type || `Room ${room.id}`}</h3>
                <p>{room.description || "Room details are managed by the Spring Boot room API."}</p>
              </div>
              <div className="list-card-actions">
                <strong>{room.price ? `${room.price.toLocaleString()} VND` : "Contact"}</strong>
                <Link to={`/booking?hotelId=${hotel.id}&roomId=${room.id}`}>Book</Link>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
