import { Link } from "react-router-dom";
import { CalendarDays, MapPin, Search, ShieldCheck, Star, Wifi } from "lucide-react";
import { HotelCard } from "@/pages/shared/HotelCard";
import { useAsync } from "@/hooks/useAsync";
import { getHotels } from "@/services/hotelApi";

export function HomePage() {
  const { data: hotels, loading } = useAsync(getHotels, []);
  const featured = hotels?.slice(0, 3) ?? [];

  return (
    <>
      <section className="hero">
        <div className="hero-content">
          <p className="eyebrow">Hotel booking service</p>
          <h1>Find rooms that fit the trip, not the other way around.</h1>
          <p>
            Migrated from the XML frontend into a Vite app and wired to the Spring Boot hotel, room,
            booking, user, amenity, and revenue APIs.
          </p>
          <form className="search-panel" action="/hotels">
            <label>
              <MapPin size={18} />
              <input name="location" placeholder="Destination" />
            </label>
            <label>
              <CalendarDays size={18} />
              <input name="checkInDate" type="date" />
            </label>
            <label>
              <CalendarDays size={18} />
              <input name="checkOutDate" type="date" />
            </label>
            <button type="submit">
              <Search size={18} />
              Search
            </button>
          </form>
        </div>
      </section>

      <section className="section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Featured stays</p>
            <h2>Available hotels</h2>
          </div>
          <Link to="/hotels">View all</Link>
        </div>
        {loading ? <p className="muted">Loading hotels...</p> : null}
        <div className="card-grid">
          {featured.map((hotel) => (
            <HotelCard key={hotel.id} hotel={hotel} />
          ))}
        </div>
      </section>

      <section className="feature-band">
        {[
          { icon: Star, title: "Ratings ready", copy: "Uses Spring Boot hotel rating fields when available." },
          { icon: Wifi, title: "Amenity aware", copy: "Hotel and room amenities map to the existing amenity endpoints." },
          { icon: ShieldCheck, title: "Token auth", copy: "JWTs are attached to protected API calls from a central client." },
        ].map((item) => {
          const Icon = item.icon;
          return (
            <article key={item.title}>
              <Icon size={22} />
              <h3>{item.title}</h3>
              <p>{item.copy}</p>
            </article>
          );
        })}
      </section>
    </>
  );
}
