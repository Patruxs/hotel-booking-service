import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AccountLayout, AdminLayout, PublicLayout } from "@/components/RouteShell";
import { AdminRoute } from "@/components/layouts/AdminRoute";
import {
  MyBookingDetailPage,
  MyBookingsPage,
  MyReviewsPage,
  ProfilePage,
} from "@/pages/AccountPages";
import {
  AdminBookingDetailPage,
  AdminBookingsPage,
  AdminCommissionDetailPage,
  AdminCommissionsPage,
  AdminContactDetailPage,
  AdminContactsPage,
  AdminDashboardPage,
  AdminHomePage,
  AdminHotelBookingsPage,
  AdminHotelDetailPage,
  AdminHotelsPage,
  AdminPromotionDetailPage,
  AdminPromotionsPage,
  AdminResourcePage,
} from "@/pages/AdminPages";
import {
  BookingPage,
  ContactPage,
  ForbiddenPage,
  ForgotPasswordPage,
  HomePage,
  HotelDetailPage,
  HotelsPage,
  LoginPage,
  NewsDetailPage,
  NewsPage,
  NotFoundPage,
  OAuthCallbackPage,
  PartnerPage,
  PaymentResultPage,
  RegisterPage,
  ResetPasswordPage,
  VerifyEmailPage,
} from "@/pages/PublicPages";
import { bypassAuth } from "@/mocks/mockAuth";
import { useAuth } from "@/providers/AuthProvider";

const EditorPage = lazy(() => import("@/pages/EditorPages").then((module) => ({ default: module.EditorPage })));
const SimpleEditorPage = lazy(() => import("@/pages/EditorPages").then((module) => ({ default: module.SimpleEditorPage })));

function Protected({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (!bypassAuth && !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="forgot-password" element={<ForgotPasswordPage />} />
        <Route path="auth/callback" element={<OAuthCallbackPage />} />
        <Route path="auth/reset" element={<ResetPasswordPage />} />
        <Route path="auth/verify-email" element={<VerifyEmailPage />} />
        <Route path="hotels" element={<HotelsPage />} />
        <Route path="hotels/:hotelId" element={<HotelDetailPage />} />
        <Route path="booking" element={<BookingPage />} />
        <Route path="payment-result" element={<PaymentResultPage />} />
        <Route path="contact" element={<ContactPage />} />
        <Route path="news" element={<NewsPage />} />
        <Route path="news/:newsId" element={<NewsDetailPage />} />
        <Route path="partner" element={<PartnerPage />} />
        <Route path="forbidden" element={<ForbiddenPage />} />
        <Route path="403" element={<ForbiddenPage />} />
        <Route
          path="me"
          element={
            <Protected>
              <AccountLayout />
            </Protected>
          }
        >
          <Route index element={<ProfilePage />} />
          <Route path="my-bookings" element={<MyBookingsPage />} />
          <Route path="my-bookings/:bookingId" element={<MyBookingDetailPage />} />
          <Route path="my-reviews" element={<MyReviewsPage />} />
        </Route>
      </Route>

      <Route
        path="admin"
        element={
          <AdminRoute>
            <AdminLayout />
          </AdminRoute>
        }
      >
        <Route index element={<AdminHomePage />} />
        <Route path="dashboard/:hotelId" element={<AdminDashboardPage />} />
        <Route path="hotels" element={<AdminHotelsPage />} />
        <Route path="hotels/:id" element={<AdminHotelDetailPage />} />
        <Route path="member-hotels" element={<AdminResourcePage title="Member hotels" kind="users" />} />
        <Route path="bookings" element={<AdminBookingsPage />} />
        <Route path="bookings/:hotelId" element={<AdminHotelBookingsPage />} />
        <Route path="bookings/:hotelId/booking/:bookingId" element={<AdminBookingDetailPage />} />
        <Route path="amenities" element={<AdminResourcePage title="Amenities" kind="amenities" />} />
        <Route path="amenities/:id" element={<AdminResourcePage title="Amenity detail" kind="amenities" />} />
        <Route path="users" element={<AdminResourcePage title="Users" kind="users" />} />
        <Route path="users/roles" element={<AdminResourcePage title="Roles" kind="roles" />} />
        <Route path="users/permissions" element={<AdminResourcePage title="Permissions" kind="permissions" />} />
        <Route path="users/actions" element={<AdminResourcePage title="Actions" kind="actions" />} />
        <Route path="room-types/:hotelId" element={<AdminResourcePage title="Room types" kind="rooms" />} />
        <Route path="room-types/:hotelId/manage/:typeId" element={<AdminResourcePage title="Room type manage" kind="rooms" />} />
        <Route path="room-types/:hotelId/manage/:typeId/room/:roomId" element={<AdminResourcePage title="Room manage" kind="rooms" />} />
        <Route path="inventory" element={<AdminResourcePage title="Inventory" kind="inventory" />} />
        <Route path="news" element={<AdminResourcePage title="Admin news" kind="news" />} />
        <Route path="news/:newsId" element={<AdminResourcePage title="Admin news detail" kind="news" />} />
        <Route path="policies" element={<AdminResourcePage title="Policies" kind="policies" />} />
        <Route path="policies/:hotelId" element={<AdminResourcePage title="Hotel policies" kind="policies" />} />
        <Route path="policies/:hotelId/policy/:policyId" element={<AdminResourcePage title="Policy detail" kind="policies" />} />
        <Route path="promotions" element={<AdminPromotionsPage />} />
        <Route path="promotions/:id" element={<AdminPromotionDetailPage />} />
        <Route path="reviews" element={<AdminResourcePage title="Reviews" kind="reviews" />} />
        <Route path="reviews/:hotelId" element={<AdminResourcePage title="Hotel reviews" kind="reviews" />} />
        <Route path="contacts" element={<AdminContactsPage />} />
        <Route path="contacts/:contactId" element={<AdminContactDetailPage />} />
        <Route path="commissions" element={<AdminCommissionsPage />} />
        <Route path="commissions/hotels" element={<AdminResourcePage title="Commission hotels" kind="hotels" />} />
        <Route path="commissions/:commissionId" element={<AdminCommissionDetailPage />} />
        <Route path="settings" element={<AdminResourcePage title="Settings" kind="settings" />} />
      </Route>

      <Route
        path="editor"
        element={
          <Suspense fallback={<div className="p-6 text-sm text-muted-foreground">Loading editor...</div>}>
            <EditorPage />
          </Suspense>
        }
      />
      <Route
        path="simple"
        element={
          <Suspense fallback={<div className="p-6 text-sm text-muted-foreground">Loading editor...</div>}>
            <SimpleEditorPage />
          </Suspense>
        }
      />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
