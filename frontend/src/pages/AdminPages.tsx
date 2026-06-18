import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { BarChart3, Building2, CalendarDays, Settings, type LucideIcon } from "lucide-react";
import { RouteHeader } from "@/components/RouteShell";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { mockApi } from "@/mocks/mockApi";
import { formatCurrency } from "@/lib/utils";
import { amenitiesApi } from "@/features/amenities/api";
import { bookingsApi } from "@/features/bookings/api";
import { hotelsApi } from "@/features/hotels/api";
import { roomsApi } from "@/features/rooms/api";
import { userApi } from "@/features/user/api";

function DataList({ title, items }: { title: string; items: Array<string | number> }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
      </CardHeader>
      <CardContent className="grid gap-2">
        {items.map((item) => (
          <div key={item} className="rounded-md border px-3 py-2 text-sm">
            {item}
          </div>
        ))}
      </CardContent>
    </Card>
  );
}

export function AdminHomePage() {
  const { data: hotels = [] } = useQuery({ queryKey: ["admin", "hotels"], queryFn: hotelsApi.list });
  const { data: bookings = [] } = useQuery({ queryKey: ["admin", "bookings"], queryFn: bookingsApi.list });
  const revenue = bookings.reduce((sum, booking) => sum + booking.total, 0);
  const summaryCards: Array<[string, string | number, LucideIcon]> = [
    ["Hotels", hotels.length, Building2],
    ["Bookings", bookings.length, CalendarDays],
    ["Revenue", formatCurrency(revenue), BarChart3],
    ["Backend mode", "Spring Boot", Settings],
  ];
  return (
    <>
      <RouteHeader title="Admin overview" description="Protected dashboard renders with VITE_BYPASS_AUTH=true and mock admin permissions." />
      <div className="grid gap-4 md:grid-cols-4">
        {summaryCards.map(([label, value, Icon]) => (
          <Card key={label}>
            <CardContent className="p-5">
              <Icon className="mb-3 h-5 w-5 text-primary" />
              <div className="text-sm text-muted-foreground">{label}</div>
              <div className="mt-1 text-2xl font-semibold">{value}</div>
            </CardContent>
          </Card>
        ))}
      </div>
    </>
  );
}

export function AdminHotelsPage() {
  const { data: hotels = [] } = useQuery({ queryKey: ["admin", "hotels"], queryFn: hotelsApi.list });
  return (
    <>
      <RouteHeader title="Admin hotels" />
      <div className="grid gap-3">
        {hotels.map((hotel) => (
          <Card key={hotel.id}>
            <CardContent className="flex flex-col gap-3 p-4 md:flex-row md:items-center md:justify-between">
              <div>
                <div className="font-semibold">{hotel.name}</div>
                <div className="text-sm text-muted-foreground">{hotel.city}</div>
              </div>
              <div className="flex gap-2">
                <Button asChild variant="secondary">
                  <Link to={`/admin/dashboard/${hotel.id}`}>Dashboard</Link>
                </Button>
                <Button asChild variant="ghost">
                  <Link to={`/admin/hotels/${hotel.id}`}>Edit</Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </>
  );
}

export function AdminHotelDetailPage() {
  const { id } = useParams();
  const { data: hotel = mockApi.hotels.get(id) } = useQuery({
    queryKey: ["admin", "hotels", id],
    queryFn: () => hotelsApi.get(id ?? ""),
    enabled: Boolean(id),
  });
  return <Detail title={`Hotel: ${hotel.name}`} rows={[hotel.address, hotel.description, `From ${formatCurrency(hotel.priceFrom)}`]} />;
}

export function AdminDashboardPage() {
  const { hotelId } = useParams();
  const { data: hotel = mockApi.hotels.get(hotelId) } = useQuery({
    queryKey: ["admin", "dashboard", hotelId],
    queryFn: () => hotelsApi.get(hotelId ?? ""),
    enabled: Boolean(hotelId),
  });
  const { data: rooms = [] } = useQuery({
    queryKey: ["admin", "dashboard", hotelId, "rooms"],
    queryFn: () => roomsApi.list(hotelId),
    enabled: Boolean(hotelId),
  });
  return <Detail title={`${hotel.name} dashboard`} rows={[`Rooms: ${rooms.length}`, `Rating: ${hotel.rating}`, `Minimum price: ${formatCurrency(hotel.priceFrom)}`]} />;
}

export function AdminBookingsPage() {
  const { data: bookings = [] } = useQuery({ queryKey: ["admin", "bookings"], queryFn: bookingsApi.list });
  return (
    <>
      <RouteHeader title="Admin bookings" />
      <div className="grid gap-3">
        {bookings.map((booking) => (
          <Card key={booking.id}>
            <CardContent className="flex flex-col gap-3 p-4 md:flex-row md:items-center md:justify-between">
              <div>
                <div className="font-semibold">{booking.hotelName}</div>
                <div className="text-sm text-muted-foreground">{booking.guestName}</div>
              </div>
              <Button asChild variant="secondary">
                <Link to={`/admin/bookings/${booking.hotelId}/booking/${booking.id}`}>Open</Link>
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </>
  );
}

export function AdminHotelBookingsPage() {
  const { hotelId } = useParams();
  const { data: bookings = [] } = useQuery({
    queryKey: ["admin", "bookings", hotelId],
    queryFn: () => bookingsApi.listByHotel(hotelId ?? ""),
    enabled: Boolean(hotelId),
  });
  return <Detail title={`Bookings for ${hotelId}`} rows={bookings.map((booking) => booking.id)} />;
}

export function AdminBookingDetailPage() {
  const { bookingId } = useParams();
  const { data: booking = mockApi.bookings.get(bookingId) } = useQuery({
    queryKey: ["admin", "booking", bookingId],
    queryFn: () => bookingsApi.getByHotel("", bookingId ?? ""),
    enabled: Boolean(bookingId),
  });
  return <Detail title={`Booking ${booking.id}`} rows={[booking.guestName, booking.status, formatCurrency(booking.total)]} />;
}

export function AdminPromotionsPage() {
  return <Detail title="Promotions" rows={mockApi.promotions.list().map((promotion) => `${promotion.code} - ${promotion.discountPercent}%`)} />;
}

export function AdminPromotionDetailPage() {
  const { id } = useParams();
  const promotion = mockApi.promotions.get(id);
  return <Detail title={promotion.title} rows={[promotion.code, `${promotion.discountPercent}%`, promotion.active ? "Active" : "Inactive"]} />;
}

export function AdminContactsPage() {
  return <Detail title="Contacts" rows={mockApi.contacts.list().map((contact) => `${contact.name} - ${contact.status}`)} />;
}

export function AdminContactDetailPage() {
  const { contactId } = useParams();
  const contact = mockApi.contacts.get(contactId);
  return <Detail title={contact.subject} rows={[contact.name, contact.email, contact.status]} />;
}

export function AdminCommissionsPage() {
  return <Detail title="Commissions" rows={mockApi.commissions.list().map((commission) => `${commission.name} - ${commission.rate}%`)} />;
}

export function AdminCommissionDetailPage() {
  const { commissionId } = useParams();
  const commission = mockApi.commissions.get(commissionId);
  return <Detail title={commission.name} rows={[`${commission.rate}%`, `${commission.hotelCount} hotels`]} />;
}

export function AdminResourcePage({ title, kind }: { title: string; kind: string }) {
  const { data: users = [] } = useQuery({ queryKey: ["admin", "users"], queryFn: userApi.list, enabled: kind === "users" });
  const { data: amenities = [] } = useQuery({ queryKey: ["admin", "amenities"], queryFn: amenitiesApi.list, enabled: kind === "amenities" });
  const { data: rooms = [] } = useQuery({ queryKey: ["admin", "rooms"], queryFn: () => roomsApi.list(), enabled: kind === "rooms" });
  const data: Record<string, Array<string | number>> = {
    amenities: (amenities as Array<string | { id?: string | number; name?: string }>).map((amenity) =>
      typeof amenity === "string" ? amenity : `${amenity.name ?? amenity.id}`,
    ),
    users: users.map((user) => user.email),
    roles: mockApi.roles.list(),
    permissions: mockApi.permissions.list(),
    actions: mockApi.actions.list(),
    inventory: mockApi.inventory.list().map((item) => `${item.hotelId} ${item.date}: ${item.available}`),
    news: mockApi.news.list().map((item) => item.title),
    policies: mockApi.policies.list().map((item) => item.title),
    reviews: mockApi.reviews.list().map((item) => `${item.userName}: ${item.rating}/5`),
    rooms: rooms.map((room) => room.name),
    banners: mockApi.banners.list(),
  };
  return (
    <>
      <RouteHeader title={title} description={`Mock-backed admin module: ${kind}.`} />
      <DataList title={title} items={data[kind] ?? ["TODO-safe migrated page"]} />
    </>
  );
}

function Detail({ title, rows }: { title: string; rows: Array<string | number> }) {
  return (
    <>
      <RouteHeader title={title} />
      <Card>
        <CardContent className="grid gap-3 p-5">
          {rows.length ? rows.map((row) => <div key={row} className="rounded-md border p-3">{row}</div>) : <Badge>No mock rows</Badge>}
        </CardContent>
      </Card>
    </>
  );
}
