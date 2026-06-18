import { useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { RouteHeader } from "@/components/RouteShell";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { mockApi } from "@/mocks/mockApi";
import { useAuth } from "@/providers/AuthProvider";
import { formatCurrency } from "@/lib/utils";
import { userApi } from "@/features/user/api";
import { bookingsApi } from "@/features/bookings/api";

export function ProfilePage() {
  const { user } = useAuth();
  const { data: profile = user } = useQuery({ queryKey: ["me"], queryFn: userApi.me, enabled: Boolean(user) });
  return (
    <>
      <RouteHeader title="Profile" description="Account route converted from /me." />
      <Card>
        <CardContent className="grid gap-2 p-5">
          <div className="text-lg font-semibold">{profile?.name}</div>
          <div className="text-muted-foreground">{profile?.email}</div>
          <div className="flex gap-2">
            {profile?.roles.map((role) => <Badge key={role.id}>{role.name}</Badge>)}
          </div>
        </CardContent>
      </Card>
    </>
  );
}

export function MyBookingsPage() {
  const { data: bookings = [] } = useQuery({ queryKey: ["me", "bookings"], queryFn: bookingsApi.mine });
  return (
    <>
      <RouteHeader title="My bookings" />
      <div className="grid gap-3">
        {bookings.map((booking) => (
          <Card key={booking.id}>
            <CardContent className="flex flex-col gap-2 p-4 md:flex-row md:items-center md:justify-between">
              <div>
                <div className="font-medium">{booking.hotelName}</div>
                <div className="text-sm text-muted-foreground">{booking.checkIn} to {booking.checkOut}</div>
              </div>
              <Badge>{booking.status}</Badge>
            </CardContent>
          </Card>
        ))}
      </div>
    </>
  );
}

export function MyBookingDetailPage() {
  const { bookingId } = useParams();
  const { data: booking = mockApi.bookings.get(bookingId) } = useQuery({
    queryKey: ["me", "bookings", bookingId],
    queryFn: () => bookingsApi.myDetail(bookingId ?? ""),
    enabled: Boolean(bookingId),
  });
  return (
    <>
      <RouteHeader title={`Booking ${booking.id}`} description={booking.hotelName} />
      <Card>
        <CardContent className="grid gap-2 p-5">
          <div>Guest: {booking.guestName}</div>
          <div>Total: {formatCurrency(booking.total)}</div>
          <Badge>{booking.status}</Badge>
        </CardContent>
      </Card>
    </>
  );
}

export function MyReviewsPage() {
  return (
    <>
      <RouteHeader title="My reviews" />
      <div className="grid gap-3">
        {mockApi.reviews.list().map((review) => (
          <Card key={review.id}>
            <CardContent className="p-4">
              <div className="font-medium">{review.rating}/5</div>
              <p className="text-muted-foreground">{review.comment}</p>
            </CardContent>
          </Card>
        ))}
      </div>
    </>
  );
}
