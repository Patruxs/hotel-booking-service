import { lazy, Suspense } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
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
    AdminRoomsPage,
    AdminHotelRoomsPage,
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
import { usePermission } from "@/providers/PermissionProvider";
import { OWNER_ONLY_REQUIREMENT, type PermissionRequirement } from "@/providers/permissionAccess";

const EditorPage = lazy(() => import("@/pages/EditorPages").then((module) => ({ default: module.EditorPage })));
const SimpleEditorPage = lazy(() => import("@/pages/EditorPages").then((module) => ({ default: module.SimpleEditorPage })));

function Protected({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();
  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-primary border-t-transparent" />
      </div>
    );
  }
  if (!bypassAuth && !isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <>{children}</>;
}

export function AccessRoute({
  children,
  requirement,
}: {
  children: React.ReactNode;
  requirement: PermissionRequirement;
}) {
  const { canAccess } = usePermission();
  return canAccess(requirement) ? <>{children}</> : <Navigate to="/forbidden" replace />;
}

const managementHomeRequirement = {
  requiredActions: [
    "reports.hotel.view",
    "hotels.manage",
      "hotel.members.manage",
      "inventory.manage",
        "room_types.manage",
        "rooms.view",
      "bookings.list.hotel",
      "content.manage",
      "reviews.manage",
  ],
  requiredRoles: ["ADMIN"],
} as const;

const AMENITY_MANAGEMENT_REQUIREMENT = {
  requiredRoles: ["ADMIN", "OWNER", "MANAGER"],
} as const;

const REVIEW_MANAGEMENT_REQUIREMENT = {
  requiredActions: ["reviews.manage"],
} as const;

const ADMIN_ONLY_REQUIREMENT = { requiredRoles: ["ADMIN"] } as const;

const requireAccess = (element: React.ReactNode, requirement: PermissionRequirement) => (
  <AccessRoute requirement={requirement}>{element}</AccessRoute>
);

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
          <Route index element={requireAccess(<AdminHomePage />, managementHomeRequirement)} />
            <Route path="dashboard/:hotelId" element={requireAccess(<AdminDashboardPage />, { requiredActions: ["reports.hotel.view"] })} />
            <Route path="hotels" element={requireAccess(<AdminHotelsPage />, { requiredActions: ["hotels.manage"] })} />
            <Route path="hotels/new" element={requireAccess(<AdminHotelDetailPage />, OWNER_ONLY_REQUIREMENT)} />
            <Route path="hotels/:id" element={requireAccess(<AdminHotelDetailPage />, { requiredActions: ["hotels.manage"] })} />
            <Route path="member-hotels" element={requireAccess(<AdminMemberHotelsPage />, OWNER_ONLY_REQUIREMENT)} />
          <Route path="bookings" element={requireAccess(<AdminBookingsPage />, { requiredActions: ["bookings.list.hotel"] })} />
          <Route path="bookings/:hotelId" element={requireAccess(<AdminHotelBookingsPage />, { requiredActions: ["bookings.list.hotel"] })} />
          <Route path="bookings/:hotelId/booking/:bookingId" element={requireAccess(<AdminBookingDetailPage />, { requiredActions: ["bookings.list.hotel"] })} />
            <Route path="amenities" element={requireAccess(<AdminAmenitiesPage />, AMENITY_MANAGEMENT_REQUIREMENT)} />
            <Route path="amenities/:id" element={requireAccess(<AdminAmenityDetailPage />, AMENITY_MANAGEMENT_REQUIREMENT)} />
            <Route path="users" element={requireAccess(<Navigate to="/admin/hotels" replace />, ADMIN_ONLY_REQUIREMENT)} />
            <Route path="users/roles" element={requireAccess(<Navigate to="/admin/hotels" replace />, ADMIN_ONLY_REQUIREMENT)} />
            <Route path="users/permissions" element={requireAccess(<Navigate to="/admin/hotels" replace />, ADMIN_ONLY_REQUIREMENT)} />
            <Route path="users/actions" element={requireAccess(<Navigate to="/admin/hotels" replace />, ADMIN_ONLY_REQUIREMENT)} />
            <Route path="room-types" element={requireAccess(<AdminRoomTypesPage />, OWNER_ONLY_REQUIREMENT)} />
            <Route path="room-types/:hotelId" element={requireAccess(<AdminHotelRoomTypesPage />, OWNER_ONLY_REQUIREMENT)} />
            <Route path="room-types/:hotelId/manage/:typeId" element={requireAccess(<AdminRoomTypeManagePage />, OWNER_ONLY_REQUIREMENT)} />
              <Route path="room-types/:hotelId/manage/:typeId/room/:roomId" element={requireAccess(<AdminRoomManagePage />, OWNER_ONLY_REQUIREMENT)} />
              <Route path="rooms" element={requireAccess(<AdminRoomsPage />, { requiredActions: ["rooms.view"] })} />
              <Route path="rooms/:hotelId" element={requireAccess(<AdminHotelRoomsPage />, { requiredActions: ["rooms.view"] })} />
            <Route path="inventory" element={requireAccess(<AdminInventoryPage />, OWNER_ONLY_REQUIREMENT)} />
            <Route path="news" element={requireAccess(<AdminNewsPage />, OWNER_ONLY_REQUIREMENT)} />
            <Route path="news/:newsId" element={requireAccess(<AdminNewsDetailPage />, OWNER_ONLY_REQUIREMENT)} />
            <Route path="policies/*" element={requireAccess(<Navigate to="/admin/hotels" replace />, ADMIN_ONLY_REQUIREMENT)} />
            <Route path="promotions/*" element={requireAccess(<Navigate to="/admin/hotels" replace />, ADMIN_ONLY_REQUIREMENT)} />
          <Route path="reviews" element={requireAccess(<AdminReviewsPage />, REVIEW_MANAGEMENT_REQUIREMENT)} />
          <Route path="reviews/:hotelId" element={requireAccess(<AdminHotelReviewsPage />, REVIEW_MANAGEMENT_REQUIREMENT)} />
            <Route path="contacts" element={requireAccess(<AdminContactsPage />, ADMIN_ONLY_REQUIREMENT)} />
            <Route path="contacts/:contactId" element={requireAccess(<AdminContactDetailPage />, ADMIN_ONLY_REQUIREMENT)} />
            <Route path="commissions/*" element={requireAccess(<Navigate to="/admin/hotels" replace />, ADMIN_ONLY_REQUIREMENT)} />
            <Route path="settings" element={requireAccess(<AdminSettingsPage />, ADMIN_ONLY_REQUIREMENT)} />
      </Route>

      <Route
        path="editor"
        element={
            <Navigate to="/hotels" replace />
        }
      />
      <Route
        path="simple"
        element={
            <Navigate to="/hotels" replace />
        }
      />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
