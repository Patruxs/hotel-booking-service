package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.content.BannerMutationRequest;
import org.example.hotelbookingservice.dto.response.content.BannerResponse;
import org.example.hotelbookingservice.dto.response.content.CommissionAssignmentResponse;
import org.example.hotelbookingservice.dto.request.content.CommissionPackageRequest;
import org.example.hotelbookingservice.dto.response.content.CommissionPackageResponse;
import org.example.hotelbookingservice.dto.request.content.ContactCreateRequest;
import org.example.hotelbookingservice.dto.response.content.ContactCreateResponse;
import org.example.hotelbookingservice.dto.response.content.ContactResponse;
import org.example.hotelbookingservice.dto.request.content.ContactUpdateRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.request.content.NewsMutationRequest;
import org.example.hotelbookingservice.dto.response.content.NewsResponse;
import org.example.hotelbookingservice.dto.response.content.NotificationResponse;
import org.example.hotelbookingservice.dto.request.content.PolicyMutationRequest;
import org.example.hotelbookingservice.dto.response.content.PolicyResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface IContentService {
    NewsResponse createNews(NewsMutationRequest request, Authentication authentication);

    NewsResponse updateNews(UUID id, NewsMutationRequest request, Authentication authentication);

    void deleteNews(UUID id, Authentication authentication);

    ListResponse<NewsResponse> listNewsAdmin(String status, String q, int page, int limit, Authentication authentication);

    ListResponse<NewsResponse> listNewsPublic(String q, int page, int limit);

    NewsResponse newsDetailAdmin(UUID id, Authentication authentication);

    NewsResponse newsDetailPublic(String slug);

    BannerResponse createBanner(BannerMutationRequest request, Authentication authentication);

    BannerResponse updateBanner(UUID id, BannerMutationRequest request, Authentication authentication);

    void deleteBanner(UUID id, Authentication authentication);

    List<BannerResponse> listAdminBanners(Authentication authentication);

    List<BannerResponse> listPublicBanners();

    ContactCreateResponse createContact(ContactCreateRequest request, String ipAddress, String userAgent, Authentication authentication);

    ListResponse<ContactResponse> listContacts(String status, String q, int page, int limit, Authentication authentication);

    ContactResponse contactDetail(UUID id, Authentication authentication);

    ContactResponse updateContact(UUID id, ContactUpdateRequest request, Authentication authentication);

    ListResponse<NotificationResponse> listNotifications(int page, int limit, Authentication authentication);

    long unreadCount(Authentication authentication);

    NotificationResponse markRead(UUID id, Authentication authentication);

    void markAllRead(Authentication authentication);

    void deleteNotification(UUID id, Authentication authentication);

    List<CommissionPackageResponse> listCommissionPackages(Authentication authentication);

    CommissionPackageResponse commissionPackageDetail(UUID id, Authentication authentication);

    CommissionPackageResponse createCommissionPackage(CommissionPackageRequest request, Authentication authentication);

    CommissionPackageResponse updateCommissionPackage(UUID id, CommissionPackageRequest request, Authentication authentication);

    CommissionPackageResponse deactivateCommissionPackage(UUID id, Authentication authentication);

    CommissionAssignmentResponse assignCommissionPackage(UUID hotelId, UUID packageId, Authentication authentication);

    List<PolicyResponse> listPoliciesPublic(UUID hotelId);

    List<PolicyResponse> listPoliciesAdmin(UUID hotelId, Authentication authentication);

    PolicyResponse policyDetail(UUID hotelId, UUID policyId, Authentication authentication);

    PolicyResponse createPolicy(UUID hotelId, PolicyMutationRequest request, Authentication authentication);

    PolicyResponse updatePolicy(UUID hotelId, UUID policyId, PolicyMutationRequest request, Authentication authentication);

    PolicyResponse deletePolicy(UUID hotelId, UUID policyId, Authentication authentication);
}
