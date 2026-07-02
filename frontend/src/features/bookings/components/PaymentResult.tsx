"use client";
import { useEffect } from "react";
import { useRouter, useSearchParams } from '@/hooks/navigation';
import { format } from "date-fns";
import {
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Loader2,
  ArrowRight,
  Home,
  Receipt,
  CalendarDays,
  Building,
  CreditCard
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useMyBookingByIdQuery } from "@/features/bookings/queries";
import { formatCurrency } from "@/utils/currency";
export function PaymentResult() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const paymentStatus = searchParams.get("payment_status");
  const bookingId = searchParams.get("booking_id");
  const { data: booking, isLoading, isError } = useMyBookingByIdQuery(bookingId || "");
  const isSuccess = paymentStatus === "success";
  const requiresReview = paymentStatus === "requires_review";
  const resultStyle = isSuccess
    ? {
        card: "border-green-200 bg-green-50",
        iconWrap: "bg-green-100",
        icon: <CheckCircle2 className="w-12 h-12 text-green-600" />,
        title: "Payment Successful!",
        titleColor: "text-green-900",
        message: "Your booking has been confirmed. We've sent a confirmation email to your registered email address.",
        messageColor: "text-green-700",
        badge: (
          <Badge className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 text-base">
            <CheckCircle2 className="w-4 h-4 mr-2" />
            Confirmed
          </Badge>
        ),
      }
    : requiresReview
      ? {
          card: "border-amber-200 bg-amber-50",
          iconWrap: "bg-amber-100",
          icon: <AlertTriangle className="w-12 h-12 text-amber-600" />,
          title: "Payment Requires Review",
          titleColor: "text-amber-900",
          message: "The provider reported payment success, but this booking was not confirmed. Our team will review it before any refund or reconciliation.",
          messageColor: "text-amber-700",
          badge: (
            <Badge className="bg-amber-600 hover:bg-amber-700 text-white px-4 py-2 text-base">
              <AlertTriangle className="w-4 h-4 mr-2" />
              Requires Review
            </Badge>
          ),
        }
      : {
          card: "border-red-200 bg-red-50",
          iconWrap: "bg-red-100",
          icon: <XCircle className="w-12 h-12 text-red-600" />,
          title: "Payment Failed",
          titleColor: "text-red-900",
          message: "Unfortunately, your payment could not be processed. Please try again or contact support.",
          messageColor: "text-red-700",
          badge: (
            <Badge variant="destructive" className="px-4 py-2 text-base">
              <XCircle className="w-4 h-4 mr-2" />
              Payment Failed
            </Badge>
          ),
        };
  useEffect(() => {
    if (!bookingId) {
      router.push("/");
    }
  }, [bookingId, router]);
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="w-10 h-10 animate-spin text-primary" />
      </div>
    );
  }
  if (isError || !booking) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-4 p-4">
        <XCircle className="w-16 h-16 text-red-500" />
        <h1 className="text-2xl font-bold text-gray-900">Booking Not Found</h1>
        <p className="text-gray-500">Unable to retrieve booking information.</p>
        <Button onClick={() => router.push("/")}>
          <Home className="w-4 h-4 mr-2" />
          Go to Home
        </Button>
      </div>
    );
  }
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 py-12 px-4">
      <div className="max-w-3xl mx-auto space-y-6">
        {}
        <Card className={`border-2 ${resultStyle.card}`}>
          <CardContent className="pt-6">
            <div className="flex flex-col items-center text-center space-y-4">
              <div className={`w-20 h-20 rounded-full ${resultStyle.iconWrap} flex items-center justify-center`}>
                {resultStyle.icon}
              </div>
              <div>
                <h1 className={`text-3xl font-bold ${resultStyle.titleColor} mb-2`}>{resultStyle.title}</h1>
                <p className={resultStyle.messageColor}>{resultStyle.message}</p>
              </div>
              {resultStyle.badge}
            </div>
          </CardContent>
        </Card>
        {}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Receipt className="w-5 h-5 text-gray-500" />
              Booking Details
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-gray-500 mb-1">Booking ID</p>
                <p className="font-semibold text-gray-900">#{booking.id}</p>
              </div>
              <div>
                <p className="text-gray-500 mb-1">Status</p>
                <Badge variant={booking.status === 'CONFIRMED' ? 'default' : 'secondary'}>
                  {booking.status}
                </Badge>
              </div>
            </div>
            <Separator />
            {}
            <div>
              <div className="flex items-center gap-2 mb-2">
                <Building className="w-4 h-4 text-gray-500" />
                <p className="font-medium text-gray-900">Hotel Information</p>
              </div>
              <div className="bg-gray-50 p-3 rounded-lg">
                <p className="font-semibold text-lg">{(booking as any).hotel?.name}</p>
                <p className="text-sm text-gray-600">
                  {(booking as any).hotel?.address}, {(booking as any).hotel?.city}, {(booking as any).hotel?.country}
                </p>
              </div>
            </div>
            <Separator />
            {}
            <div>
              <div className="flex items-center gap-2 mb-2">
                <CalendarDays className="w-4 h-4 text-gray-500" />
                <p className="font-medium text-gray-900">Stay Details</p>
              </div>
              <div className="grid grid-cols-2 gap-4 bg-gray-50 p-3 rounded-lg">
                <div>
                  <p className="text-xs text-gray-500 mb-1">Check-in</p>
                  <p className="font-semibold">{format(new Date(booking.checkIn), "dd MMM yyyy")}</p>
                  <p className="text-xs text-gray-400">After 14:00</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 mb-1">Check-out</p>
                  <p className="font-semibold">{format(new Date(booking.checkOut), "dd MMM yyyy")}</p>
                  <p className="text-xs text-gray-400">Before 12:00</p>
                </div>
              </div>
              <p className="text-sm text-gray-600 mt-2">
                Duration: {Math.ceil((new Date(booking.checkOut).getTime() - new Date(booking.checkIn).getTime()) / (1000 * 60 * 60 * 24))} nights
              </p>
            </div>
            <Separator />
            {}
            <div>
              <p className="font-medium text-gray-900 mb-2">Room Details</p>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Room Type</TableHead>
                    <TableHead className="text-center">Quantity</TableHead>
                    <TableHead className="text-right">Price</TableHead>
                    <TableHead className="text-right">Total</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {booking.items.map((item: any) => (
                    <TableRow key={item.id}>
                      <TableCell className="font-medium">
                        {item.roomType?.name || item.roomTypeId}
                      </TableCell>
                      <TableCell className="text-center">{item.quantity}</TableCell>
                      <TableCell className="text-right">{formatCurrency(item.unitPrice)}</TableCell>
                      <TableCell className="text-right font-semibold">{formatCurrency(item.lineTotal)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
            <Separator />
            {}
            <div className="bg-primary/5 p-4 rounded-lg">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <CreditCard className="w-5 h-5 text-primary" />
                  <span className="font-semibold text-gray-900">Total Amount</span>
                </div>
                <span className="text-2xl font-bold text-primary">{formatCurrency(booking.totalAmount)}</span>
              </div>
            </div>
          </CardContent>
        </Card>
        {}
        <div className="flex flex-col sm:flex-row gap-3">
          <Button
            variant="outline"
            className="flex-1"
            onClick={() => router.push("/")}
          >
            <Home className="w-4 h-4 mr-2" />
            Back to Home
          </Button>
          <Button
            className="flex-1"
            onClick={() => router.push(`/me/my-bookings/${booking.id}`)}
          >
            View Booking Details
            <ArrowRight className="w-4 h-4 ml-2" />
          </Button>
        </div>
        {}
        {!isSuccess && !requiresReview && booking.status === 'PENDING' && (
          <Card className="border-orange-200 bg-orange-50">
            <CardContent className="pt-6">
              <div className="text-center space-y-3">
                <p className="text-orange-900 font-medium">Would you like to try payment again?</p>
                <Button
                  variant="default"
                  onClick={() => router.push(`/me/my-bookings/${booking.id}`)}
                  className="bg-orange-600 hover:bg-orange-700"
                >
                  <CreditCard className="w-4 h-4 mr-2" />
                  Retry Payment
                </Button>
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
