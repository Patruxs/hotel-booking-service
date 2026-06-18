import { useState } from "react";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { CheckCircle2, Mail, Star } from "lucide-react";
import { AppImage } from "@/components/AppImage";
import { RouteHeader } from "@/components/RouteShell";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { bookingsApi } from "@/features/bookings/api";
import { hotelsApi } from "@/features/hotels/api";
import { roomsApi } from "@/features/rooms/api";
import { mockApi } from "@/mocks/mockApi";
import { useAuth } from "@/providers/AuthProvider";
import { formatCurrency } from "@/lib/utils";
import type { Hotel } from "@/lib/types";

export function HomePage() {
  const { data: hotels = [] } = useQuery({ queryKey: ["hotels", "public"], queryFn: hotelsApi.listPublic });
  const featured = hotels.slice(0, 3);
  return (
    <section className="mx-auto max-w-7xl px-4 py-10">
      <div className="grid gap-8 md:grid-cols-[1.1fr_0.9fr] md:items-center">
        <div>
          <Badge>Mock-ready Vite migration</Badge>
          <h1 className="mt-4 text-4xl font-semibold tracking-normal md:text-5xl">Find rooms, manage hotels, and inspect every migrated route.</h1>
          <p className="mt-4 max-w-2xl text-muted-foreground">
            The old Next.js App Router surface is now represented as a React Router SPA with mock fixtures and auth bypass.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Button asChild>
              <Link to="/hotels">Browse hotels</Link>
            </Button>
            <Button asChild variant="secondary">
              <Link to="/admin">Open admin</Link>
            </Button>
          </div>
        </div>
        <div className="grid gap-3">
          {featured.map((hotel) => (
            <HotelCard key={hotel.id} hotel={hotel} />
          ))}
        </div>
      </div>
    </section>
  );
}

function HotelCard({ hotel }: { hotel: Hotel }) {
  return (
    <Card>
      <CardContent className="grid gap-4 p-4 sm:grid-cols-[120px_1fr]">
        <AppImage src={hotel.imageUrl} alt={hotel.name} className="h-28 w-full rounded-md border object-contain p-4 sm:w-28" />
        <div>
          <div className="flex items-center justify-between gap-3">
            <h2 className="font-semibold">{hotel.name}</h2>
            <span className="flex items-center gap-1 text-sm text-muted-foreground">
              <Star className="h-4 w-4 fill-primary text-primary" />
              {hotel.rating}
            </span>
          </div>
          <p className="mt-1 text-sm text-muted-foreground">{hotel.address}</p>
          <p className="mt-3 text-sm">{formatCurrency(hotel.priceFrom)} / night</p>
          <Button asChild variant="ghost" className="mt-3 px-0">
            <Link to={`/hotels/${hotel.id}`}>View detail</Link>
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

export function HotelsPage() {
  const { data: hotels = [], isLoading, error } = useQuery({ queryKey: ["hotels", "public"], queryFn: hotelsApi.listPublic });
  return (
    <section className="mx-auto max-w-7xl px-4 py-8">
      <RouteHeader title="Hotels" description="Loaded from Spring Boot when VITE_USE_MOCKS=false." />
      {isLoading ? <p className="text-sm text-muted-foreground">Loading hotels from backend...</p> : null}
      {error ? <p className="text-sm text-destructive">Could not load hotels from backend. Check Spring Boot on localhost:8080.</p> : null}
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {hotels.map((hotel) => (
          <HotelCard key={hotel.id} hotel={hotel} />
        ))}
      </div>
    </section>
  );
}

export function HotelDetailPage() {
  const { hotelId } = useParams();
  const { data: hotel = mockApi.hotels.get(hotelId) } = useQuery({
    queryKey: ["hotels", hotelId],
    queryFn: () => hotelsApi.get(hotelId ?? ""),
    enabled: Boolean(hotelId),
  });
  const { data: rooms = [] } = useQuery({
    queryKey: ["hotels", hotelId, "rooms"],
    queryFn: () => roomsApi.list(hotelId),
    enabled: Boolean(hotelId),
  });
  return (
    <section className="mx-auto max-w-7xl px-4 py-8">
      <RouteHeader title={hotel.name} description={hotel.description} />
      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <Card>
          <CardContent className="p-4">
            <AppImage src={hotel.imageUrl} alt={hotel.name} className="h-64 w-full rounded-md border object-contain p-8" />
            <div className="mt-4 flex flex-wrap gap-2">
              {hotel.amenities.map((amenity) => (
                <Badge key={amenity}>{amenity}</Badge>
              ))}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Available rooms</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-3">
            {rooms.map((room) => (
              <div key={room.id} className="rounded-md border p-3">
                <div className="font-medium">{room.name}</div>
                <div className="text-sm text-muted-foreground">{room.capacity} guests, {room.available} available</div>
                <div className="mt-2 font-semibold">{formatCurrency(room.price)}</div>
              </div>
            ))}
            <Button asChild>
              <Link to="/booking">Continue booking</Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </section>
  );
}

export function LoginPage() {
  const { login } = useAuth();
  return <AuthForm title="Login" actionLabel="Login" onAction={login} footer={<Link to="/forgot-password">Forgot password?</Link>} />;
}

export function RegisterPage() {
  return <AuthForm title="Register" actionLabel="Create mock account" footer={<Link to="/login">Already have an account?</Link>} />;
}

export function ForgotPasswordPage() {
  return <AuthForm title="Forgot password" actionLabel="Send reset email" footer={<Link to="/login">Back to login</Link>} />;
}

function AuthForm({ title, actionLabel, onAction, footer }: { title: string; actionLabel: string; onAction?: (email: string, password: string) => Promise<void>; footer?: React.ReactNode }) {
  const [email, setEmail] = useState("admin@gmail.com");
  const [password, setPassword] = useState("admin123");
  const [message, setMessage] = useState<string | null>(null);

  async function submit() {
    if (!onAction) {
      setMessage("This flow is TODO-safe until the matching backend endpoint is available.");
      return;
    }
    try {
      await onAction(email, password);
      setMessage("Login successful. Token stored for backend calls.");
    } catch {
      setMessage("Login failed. Check backend credentials and server status.");
    }
  }

  return (
    <section className="mx-auto max-w-md px-4 py-12">
      <Card>
        <CardHeader>
          <CardTitle>{title}</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4">
          <input className="h-10 rounded-md border bg-background px-3" placeholder="email@example.com" value={email} onChange={(event) => setEmail(event.target.value)} />
          <input className="h-10 rounded-md border bg-background px-3" placeholder="Password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} />
          <Button onClick={submit}>{actionLabel}</Button>
          {message ? <div className="text-sm text-muted-foreground">{message}</div> : null}
          <div className="text-sm text-muted-foreground">{footer}</div>
        </CardContent>
      </Card>
    </section>
  );
}

export function BookingPage() {
  const { data: hotels = mockApi.hotels.list() } = useQuery({ queryKey: ["hotels", "public"], queryFn: hotelsApi.listPublic });
  const hotel = hotels[0] ?? mockApi.hotels.list()[0];
  return (
    <section className="mx-auto max-w-4xl px-4 py-8">
      <RouteHeader title="Booking" description="Booking flow placeholder keeps payment and room selection routes render-safe." />
      <Card>
        <CardContent className="grid gap-4 p-5 md:grid-cols-2">
          <HotelCard hotel={hotel} />
          <div className="grid gap-3">
            <input className="h-10 rounded-md border bg-background px-3" defaultValue="2026-06-18" />
            <input className="h-10 rounded-md border bg-background px-3" defaultValue="2026-06-21" />
            <Button asChild>
              <Link to="/payment-result?status=success">Pay with mock VNPAY</Link>
            </Button>
          </div>
        </CardContent>
      </Card>
    </section>
  );
}

export function PaymentResultPage() {
  const [params] = useSearchParams();
  const status = params.get("status") ?? "success";
  return (
    <section className="mx-auto max-w-3xl px-4 py-12">
      <Card>
        <CardContent className="p-6 text-center">
          <CheckCircle2 className="mx-auto h-12 w-12 text-primary" />
          <h1 className="mt-4 text-2xl font-semibold">Payment {status}</h1>
          <p className="mt-2 text-muted-foreground">The migrated route preserves the old payment-result entry point.</p>
        </CardContent>
      </Card>
    </section>
  );
}

export function BackendBookingsPreview() {
  const { data: bookings = [] } = useQuery({ queryKey: ["bookings", "all"], queryFn: bookingsApi.list });
  return <span>{bookings.length}</span>;
}

export function ContactPage() {
  return (
    <section className="mx-auto max-w-4xl px-4 py-8">
      <RouteHeader title="Contact" description="Public contact form backed by mock contact fixtures." />
      <Card>
        <CardContent className="grid gap-3 p-5">
          <input className="h-10 rounded-md border bg-background px-3" placeholder="Name" />
          <input className="h-10 rounded-md border bg-background px-3" placeholder="Email" />
          <textarea className="min-h-32 rounded-md border bg-background p-3" placeholder="Message" />
          <Button>
            <Mail className="mr-2 h-4 w-4" />
            Send
          </Button>
        </CardContent>
      </Card>
    </section>
  );
}

export function NewsPage() {
  return (
    <section className="mx-auto max-w-5xl px-4 py-8">
      <RouteHeader title="News" />
      <div className="grid gap-4">
        {mockApi.news.list().map((item) => (
          <Card key={item.id}>
            <CardContent className="p-5">
              <h2 className="font-semibold">{item.title}</h2>
              <p className="mt-2 text-muted-foreground">{item.excerpt}</p>
              <Button asChild variant="ghost" className="mt-3 px-0">
                <Link to={`/news/${item.id}`}>Read more</Link>
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
}

export function NewsDetailPage() {
  const { newsId } = useParams();
  const item = mockApi.news.get(newsId);
  return (
    <section className="mx-auto max-w-3xl px-4 py-8">
      <RouteHeader title={item.title} description={item.publishedAt} />
      <p className="leading-7 text-muted-foreground">{item.content}</p>
    </section>
  );
}

export function PartnerPage() {
  return <StaticPage title="Partner" description="Partner onboarding entry point from the old public route." />;
}

export function OAuthCallbackPage() {
  return <StaticPage title="OAuth callback" description="TODO-safe Google OAuth placeholder until Spring Boot OAuth endpoints are ready." />;
}

export function ResetPasswordPage() {
  return <StaticPage title="Reset password" description="Password reset route migrated from Next auth pages." />;
}

export function VerifyEmailPage() {
  return <StaticPage title="Verify email" description="Email verification route migrated from Next auth pages." />;
}

export function ForbiddenPage() {
  return <StaticPage title="Forbidden" description="You do not have permission to access this feature." />;
}

export function NotFoundPage() {
  return <StaticPage title="Not found" description="No migrated route matched this URL." />;
}

export function StaticPage({ title, description }: { title: string; description?: string }) {
  return (
    <section className="mx-auto max-w-5xl px-4 py-8">
      <RouteHeader title={title} description={description} />
    </section>
  );
}
