import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AccountLayout, AdminLayout, PublicLayout } from "@/components/RouteShell";
import { AdminRoute } from "@/components/layouts/AdminRoute";
import {
  MyBookingDetailPage,
  MyBookingsPage,
  MyReviewsPage,
  ProfilePage,
} from "@/pages/kinyias/AccountPages";
import {
  AdminActionsPage,
  AdminAmenityDetailPage,
  AdminAmenitiesPage,
  AdminBookingDetailPage,
  AdminBookingsPage,
  AdminCommissionDetailPage,
  AdminCommissionHotelsPage,
  AdminCommissionsPage,
  AdminContactDetailPage,
  AdminContactsPage,
  AdminDashboardPage,
  AdminHomePage,
  AdminHotelBookingsPage,
  AdminHotelDetailPage,
  AdminHotelPoliciesPage,
  AdminHotelReviewsPage,
  AdminHotelRoomTypesPage,
  AdminHotelsPage,
  AdminInventoryPage,
  AdminMemberHotelsPage,
  AdminNewsDetailPage,
  AdminNewsPage,
  AdminPermissionsPage,
  AdminPoliciesPage,
  AdminPolicyDetailPage,
  AdminPromotionDetailPage,
  AdminPromotionsPage,
  AdminReviewsPage,
  AdminRolesPage,
  AdminRoomManagePage,
  AdminRoomTypeManagePage,
  AdminRoomTypesPage,
  AdminSettingsPage,
  AdminUsersPage,
} from "@/pages/kinyias/AdminPages";
import {
  BookingPage,
  ContactPage,
  ForgotPasswordPage,
  HomePage,
  HotelDetailPage,
  HotelsPage,
  LoginPage,
  NewsDetailPage,
  NewsPage,
  OAuthCallbackPage,
  PartnerPage,
  PaymentResultPage,
  RegisterPage,
  ResetPasswordPage,
  VerifyEmailPage,
} from "@/pages/kinyias/PublicPages";
import { ForbiddenPage, NotFoundPage } from "@/pages/ErrorPages";
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
        <Route path="member-hotels" element={<AdminMemberHotelsPage />} />
        <Route path="bookings" element={<AdminBookingsPage />} />
        <Route path="bookings/:hotelId" element={<AdminHotelBookingsPage />} />
        <Route path="bookings/:hotelId/booking/:bookingId" element={<AdminBookingDetailPage />} />
        <Route path="amenities" element={<AdminAmenitiesPage />} />
        <Route path="amenities/:id" element={<AdminAmenityDetailPage />} />
        <Route path="users" element={<AdminUsersPage />} />
        <Route path="users/roles" element={<AdminRolesPage />} />
        <Route path="users/permissions" element={<AdminPermissionsPage />} />
        <Route path="users/actions" element={<AdminActionsPage />} />
        <Route path="room-types" element={<AdminRoomTypesPage />} />
        <Route path="room-types/:hotelId" element={<AdminHotelRoomTypesPage />} />
        <Route path="room-types/:hotelId/manage/:typeId" element={<AdminRoomTypeManagePage />} />
        <Route path="room-types/:hotelId/manage/:typeId/room/:roomId" element={<AdminRoomManagePage />} />
        <Route path="inventory" element={<AdminInventoryPage />} />
        <Route path="news" element={<AdminNewsPage />} />
        <Route path="news/:newsId" element={<AdminNewsDetailPage />} />
        <Route path="policies" element={<AdminPoliciesPage />} />
        <Route path="policies/:hotelId" element={<AdminHotelPoliciesPage />} />
        <Route path="policies/:hotelId/policy/:policyId" element={<AdminPolicyDetailPage />} />
        <Route path="promotions" element={<AdminPromotionsPage />} />
        <Route path="promotions/:id" element={<AdminPromotionDetailPage />} />
        <Route path="reviews" element={<AdminReviewsPage />} />
        <Route path="reviews/:hotelId" element={<AdminHotelReviewsPage />} />
        <Route path="contacts" element={<AdminContactsPage />} />
        <Route path="contacts/:contactId" element={<AdminContactDetailPage />} />
        <Route path="commissions" element={<AdminCommissionsPage />} />
        <Route path="commissions/hotels" element={<AdminCommissionHotelsPage />} />
        <Route path="commissions/:commissionId" element={<AdminCommissionDetailPage />} />
        <Route path="settings" element={<AdminSettingsPage />} />
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
