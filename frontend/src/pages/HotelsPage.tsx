import { FormEvent, useState } from "react";
import { HotelCard } from "@/pages/shared/HotelCard";
import { useAsync } from "@/hooks/useAsync";
import { getHotels, searchHotels } from "@/services/hotelApi";
import type { Hotel } from "@/lib/types";

export function HotelsPage() {
  const { data, error, loading } = useAsync(getHotels, []);
  const [hotels, setHotels] = useState<Hotel[] | null>(null);
  const [searching, setSearching] = useState(false);

  async function onSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const location = String(form.get("location") || "");
    const checkInDate = String(form.get("checkInDate") || "");
    const checkOutDate = String(form.get("checkOutDate") || "");
    if (!location || !checkInDate || !checkOutDate) return;
    setSearching(true);
    try {
      setHotels(await searchHotels({ location, checkInDate, checkOutDate }));
    } finally {
      setSearching(false);
    }
  }

  const visibleHotels = hotels ?? data ?? [];

  return (
    <section className="section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Browse</p>
          <h1>Hotels</h1>
        </div>
      </div>
      <form className="filter-bar" onSubmit={onSearch}>
        <input name="location" placeholder="Location" />
        <input name="checkInDate" type="date" />
        <input name="checkOutDate" type="date" />
        <button type="submit">{searching ? "Searching..." : "Search"}</button>
      </form>
      {loading ? <p className="muted">Loading hotels...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      <div className="card-grid">
        {visibleHotels.map((hotel) => (
          <HotelCard key={hotel.id} hotel={hotel} />
        ))}
      </div>
    </section>
  );
}
