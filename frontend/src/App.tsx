import { Navigate, Route, Routes } from "react-router-dom";
import { AdminLayout } from "@/layouts/AdminLayout";
import { AccountLayout } from "@/layouts/AccountLayout";
import { PublicLayout } from "@/layouts/PublicLayout";
import { AdminDashboardPage } from "@/pages/admin/AdminDashboardPage";
import { AdminResourcePage } from "@/pages/admin/AdminResourcePage";
import { BookingPage } from "@/pages/BookingPage";
import { ContactPage } from "@/pages/ContactPage";
import { ForbiddenPage } from "@/pages/ForbiddenPage";
import { HomePage } from "@/pages/HomePage";
import { HotelDetailPage } from "@/pages/HotelDetailPage";
import { HotelsPage } from "@/pages/HotelsPage";
import { LoginPage } from "@/pages/LoginPage";
import { MyBookingsPage } from "@/pages/MyBookingsPage";
import { NewsPage } from "@/pages/NewsPage";
import { PaymentResultPage } from "@/pages/PaymentResultPage";
import { ProfilePage } from "@/pages/ProfilePage";
import { RegisterPage } from "@/pages/RegisterPage";
import { StaticFlowPage } from "@/pages/StaticFlowPage";

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path="hotels" element={<HotelsPage />} />
        <Route path="hotels/:hotelId" element={<HotelDetailPage />} />
        <Route path="booking" element={<BookingPage />} />
        <Route path="contact" element={<ContactPage />} />
        <Route path="partner" element={<ContactPage variant="partner" />} />
        <Route path="payment-result" element={<PaymentResultPage />} />
        <Route path="news" element={<NewsPage />} />
        <Route path="news/:newsId" element={<StaticFlowPage title="News detail" unsupported="GET /api/v1/news/{newsId}" />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="forgot-password" element={<StaticFlowPage title="Forgot password" unsupported="POST /api/v1/auth/forgot-password" />} />
        <Route path="auth/callback" element={<StaticFlowPage title="OAuth callback" unsupported="OAuth callback endpoints" />} />
        <Route path="auth/reset" element={<StaticFlowPage title="Reset password" unsupported="POST /api/v1/auth/reset-password" />} />
        <Route path="auth/verify-email" element={<StaticFlowPage title="Verify email" unsupported="POST /api/v1/auth/verify-email" />} />
        <Route path="forbidden" element={<ForbiddenPage />} />
        <Route path="editor" element={<StaticFlowPage title="Content editor" unsupported="Upload/gallery endpoints from the XML backend" />} />
        <Route path="simple" element={<HomePage />} />
      </Route>

      <Route path="me" element={<AccountLayout />}>
        <Route index element={<ProfilePage />} />
        <Route path="my-bookings" element={<MyBookingsPage />} />
        <Route path="my-bookings/:bookingId" element={<MyBookingsPage detail />} />
        <Route path="my-reviews" element={<StaticFlowPage title="My reviews" unsupported="GET /api/v1/reviews/me" />} />
      </Route>

      <Route path="admin" element={<AdminLayout />}>
        <Route index element={<AdminDashboardPage />} />
        <Route path="dashboard/:hotelId" element={<AdminDashboardPage />} />
        <Route path="hotels" element={<AdminResourcePage resource="hotels" />} />
        <Route path="hotels/:id" element={<AdminResourcePage resource="hotel detail" />} />
        <Route path="rooms" element={<AdminResourcePage resource="rooms" />} />
        <Route path="bookings" element={<AdminResourcePage resource="bookings" />} />
        <Route path="bookings/:hotelId" element={<AdminResourcePage resource="hotel bookings" />} />
        <Route path="bookings/:hotelId/booking/:bookingId" element={<AdminResourcePage resource="booking detail" />} />
        <Route path="amenities" element={<AdminResourcePage resource="amenities" />} />
        <Route path="amenities/:id" element={<AdminResourcePage resource="amenity detail" />} />
        <Route path="inventory" element={<AdminResourcePage resource="physical room inventory" />} />
        <Route path="member-hotels" element={<AdminResourcePage resource="member hotels" unsupported="GET /api/v1/hotels/member-hotels" />} />
        <Route path="users" element={<AdminResourcePage resource="users" />} />
        <Route path="users/actions" element={<AdminResourcePage resource="actions" unsupported="permission/action management endpoints" />} />
        <Route path="users/permissions" element={<AdminResourcePage resource="permissions" unsupported="permission management endpoints" />} />
        <Route path="users/roles" element={<AdminResourcePage resource="roles" unsupported="role management endpoints" />} />
        <Route path="reviews" element={<AdminResourcePage resource="reviews" unsupported="review admin endpoints" />} />
        <Route path="reviews/:hotelId" element={<AdminResourcePage resource="hotel reviews" unsupported="review admin endpoints" />} />
        <Route path="room-types" element={<AdminResourcePage resource="room types" />} />
        <Route path="room-types/:hotelId" element={<AdminResourcePage resource="hotel room types" />} />
        <Route path="room-types/:hotelId/manage/:typeId" element={<AdminResourcePage resource="room type management" />} />
        <Route path="room-types/:hotelId/manage/:typeId/room/:roomId" element={<AdminResourcePage resource="physical room detail" />} />
        <Route path="commissions" element={<AdminResourcePage resource="commissions" unsupported="commission endpoints" />} />
        <Route path="commissions/hotels" element={<AdminResourcePage resource="commission hotels" unsupported="commission endpoints" />} />
        <Route path="commissions/:commissionId" element={<AdminResourcePage resource="commission detail" unsupported="commission endpoints" />} />
        <Route path="contacts" element={<AdminResourcePage resource="contacts" unsupported="contact admin endpoints" />} />
        <Route path="contacts/:contactId" element={<AdminResourcePage resource="contact detail" unsupported="contact admin endpoints" />} />
        <Route path="news" element={<AdminResourcePage resource="news" unsupported="news endpoints" />} />
        <Route path="news/:newsId" element={<AdminResourcePage resource="news detail" unsupported="news endpoints" />} />
        <Route path="policies" element={<AdminResourcePage resource="policies" unsupported="policy endpoints" />} />
        <Route path="policies/:hotelId" element={<AdminResourcePage resource="hotel policies" unsupported="policy endpoints" />} />
        <Route path="policies/:hotelId/policy/:policyId" element={<AdminResourcePage resource="policy detail" unsupported="policy endpoints" />} />
        <Route path="promotions" element={<AdminResourcePage resource="promotions" unsupported="promotion endpoints" />} />
        <Route path="promotions/:id" element={<AdminResourcePage resource="promotion detail" unsupported="promotion endpoints" />} />
        <Route path="settings" element={<AdminResourcePage resource="settings" unsupported="settings endpoint" />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
