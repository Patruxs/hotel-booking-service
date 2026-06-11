import { Link } from "react-router-dom";
import { MapPin, Star } from "lucide-react";
import type { Hotel } from "@/lib/types";

export function HotelCard({ hotel }: { hotel: Hotel }) {
  return (
    <article className="hotel-card">
      <img src={hotel.coverImage || hotel.images?.[0] || "/globe.svg"} alt={hotel.name} />
      <div>
        <div className="card-meta">
          <span><MapPin size={14} /> {hotel.location}</span>
          <span><Star size={14} /> {hotel.starRating ?? "N/A"}</span>
        </div>
        <h3>{hotel.name}</h3>
        <p>{hotel.description || "A hotel from the Spring Boot catalog."}</p>
        <div className="card-footer">
          <strong>{hotel.minPrice ? `${hotel.minPrice.toLocaleString()} VND` : "View rooms"}</strong>
          <Link to={`/hotels/${hotel.id}`}>Details</Link>
        </div>
      </div>
    </article>
  );
}
