import { FormEvent, useState } from "react";
import { useSearchParams } from "react-router-dom";
import toast from "react-hot-toast";
import { createBooking } from "@/services/hotelApi";

export function BookingPage() {
  const [params] = useSearchParams();
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setSubmitting(true);
    try {
      const booking = await createBooking({
        hotelId: Number(form.get("hotelId")),
        roomId: Number(form.get("roomId")),
        checkinDate: String(form.get("checkinDate")),
        checkoutDate: String(form.get("checkoutDate")),
        adultAmount: Number(form.get("adultAmount") || 1),
        childrenAmount: Number(form.get("childrenAmount") || 0),
        roomQuantity: Number(form.get("roomQuantity") || 1),
        specialRequire: String(form.get("specialRequire") || ""),
      });
      toast.success(`Booking created${booking.bookingReference ? `: ${booking.bookingReference}` : ""}`);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Booking failed");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="section narrow">
      <p className="eyebrow">Reservation</p>
      <h1>Create booking</h1>
      <form className="form-card" onSubmit={onSubmit}>
        <label>
          Hotel ID
          <input name="hotelId" defaultValue={params.get("hotelId") ?? ""} required />
        </label>
        <label>
          Room ID
          <input name="roomId" defaultValue={params.get("roomId") ?? ""} required />
        </label>
        <label>
          Check-in
          <input name="checkinDate" type="date" required />
        </label>
        <label>
          Check-out
          <input name="checkoutDate" type="date" required />
        </label>
        <label>
          Adults
          <input name="adultAmount" type="number" min="1" defaultValue="1" />
        </label>
        <label>
          Children
          <input name="childrenAmount" type="number" min="0" defaultValue="0" />
        </label>
        <label>
          Rooms
          <input name="roomQuantity" type="number" min="1" defaultValue="1" />
        </label>
        <label>
          Special request
          <textarea name="specialRequire" />
        </label>
        <button type="submit" disabled={submitting}>{submitting ? "Creating..." : "Create booking"}</button>
      </form>
    </section>
  );
}
