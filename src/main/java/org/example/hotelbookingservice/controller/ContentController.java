package org.example.hotelbookingservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.api.ContentApi;
import org.example.hotelbookingservice.dto.common.ApiResponse;
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
import org.example.hotelbookingservice.dto.response.content.UnreadCountResponse;
import org.example.hotelbookingservice.services.IContentService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ContentController implements ContentApi {
    private final IContentService contentService;

    @Override
    public ApiResponse<ListResponse<NewsResponse>> publicNews(@RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<ListResponse<NewsResponse>>builder().status(200).message("Success").data(contentService.listNewsPublic(q, page, limit)).build();
    }

    @Override
    public ApiResponse<NewsResponse> publicNewsDetail(@PathVariable String slug) {
        return ApiResponse.<NewsResponse>builder().status(200).message("Success").data(contentService.newsDetailPublic(slug)).build();
    }

    @Override
    public ApiResponse<ListResponse<NewsResponse>> adminNews(@RequestParam(required = false) String status, @RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return ApiResponse.<ListResponse<NewsResponse>>builder().status(200).message("Success").data(contentService.listNewsAdmin(status, q, page, limit, authentication)).build();
    }

    @Override
    public ApiResponse<NewsResponse> adminNewsDetail(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.<NewsResponse>builder().status(200).message("Success").data(contentService.newsDetailAdmin(id, authentication)).build();
    }

    @Override
    public ApiResponse<NewsResponse> createNews(@RequestBody @Valid NewsMutationRequest request, Authentication authentication) {
        return ApiResponse.<NewsResponse>builder().status(201).message("News created").data(contentService.createNews(request, authentication)).build();
    }

    @Override
    public ApiResponse<NewsResponse> updateNews(@PathVariable UUID id, @RequestBody NewsMutationRequest request, Authentication authentication) {
        return ApiResponse.<NewsResponse>builder().status(200).message("News updated").data(contentService.updateNews(id, request, authentication)).build();
    }

    @Override
    public void deleteNews(@PathVariable UUID id, Authentication authentication) {
        contentService.deleteNews(id, authentication);
    }

    @Override
    public ApiResponse<List<BannerResponse>> publicBanners() {
        return ApiResponse.<List<BannerResponse>>builder().status(200).message("Success").data(contentService.listPublicBanners()).build();
    }

    @Override
    public ApiResponse<List<BannerResponse>> adminBanners(Authentication authentication) {
        return ApiResponse.<List<BannerResponse>>builder().status(200).message("Success").data(contentService.listAdminBanners(authentication)).build();
    }

    @Override
    public ApiResponse<BannerResponse> createBanner(@RequestBody BannerMutationRequest request, Authentication authentication) {
        return ApiResponse.<BannerResponse>builder().status(201).message("Banner created").data(contentService.createBanner(request, authentication)).build();
    }

    @Override
    public ApiResponse<BannerResponse> updateBanner(@PathVariable UUID id, @RequestBody BannerMutationRequest request, Authentication authentication) {
        return ApiResponse.<BannerResponse>builder().status(200).message("Banner updated").data(contentService.updateBanner(id, request, authentication)).build();
    }

    @Override
    public void deleteBanner(@PathVariable UUID id, Authentication authentication) {
        contentService.deleteBanner(id, authentication);
    }

    @Override
    public ApiResponse<ContactCreateResponse> createContact(@RequestBody @Valid ContactCreateRequest request, HttpServletRequest servletRequest, Authentication authentication) {
        return ApiResponse.<ContactCreateResponse>builder().status(201).message("Contact submitted").data(contentService.createContact(request, servletRequest.getRemoteAddr(), servletRequest.getHeader(HttpHeaders.USER_AGENT), authentication)).build();
    }

    @Override
    public ApiResponse<ListResponse<ContactResponse>> contacts(@RequestParam(required = false) String status, @RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return ApiResponse.<ListResponse<ContactResponse>>builder().status(200).message("Success").data(contentService.listContacts(status, q, page, limit, authentication)).build();
    }

    @Override
    public ApiResponse<ContactResponse> contactDetail(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.<ContactResponse>builder().status(200).message("Success").data(contentService.contactDetail(id, authentication)).build();
    }

    @Override
    public ApiResponse<ContactResponse> updateContact(@PathVariable UUID id, @RequestBody ContactUpdateRequest request, Authentication authentication) {
        return ApiResponse.<ContactResponse>builder().status(200).message("Contact updated").data(contentService.updateContact(id, request, authentication)).build();
    }

    @Override
    public ApiResponse<ListResponse<NotificationResponse>> notifications(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication) {
        return ApiResponse.<ListResponse<NotificationResponse>>builder().status(200).message("Success").data(contentService.listNotifications(page, limit, authentication)).build();
    }

    @Override
    public ApiResponse<UnreadCountResponse> unreadCount(Authentication authentication) {
        return ApiResponse.<UnreadCountResponse>builder().status(200).message("Success").data(new UnreadCountResponse(contentService.unreadCount(authentication))).build();
    }

    @Override
    public ApiResponse<NotificationResponse> markRead(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.<NotificationResponse>builder().status(200).message("Notification read").data(contentService.markRead(id, authentication)).build();
    }

    @Override
    public ApiResponse<Void> markAllRead(Authentication authentication) {
        contentService.markAllRead(authentication);
        return ApiResponse.<Void>builder().status(200).message("Notifications read").build();
    }

    @Override
    public void deleteNotification(@PathVariable UUID id, Authentication authentication) {
        contentService.deleteNotification(id, authentication);
    }

    @Override
    public ApiResponse<List<CommissionPackageResponse>> commissionPackages(Authentication authentication) {
        return ApiResponse.<List<CommissionPackageResponse>>builder().status(200).message("Success").data(contentService.listCommissionPackages(authentication)).build();
    }

    @Override
    public ApiResponse<CommissionPackageResponse> commissionPackage(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.<CommissionPackageResponse>builder().status(200).message("Success").data(contentService.commissionPackageDetail(id, authentication)).build();
    }

    @Override
    public ApiResponse<CommissionPackageResponse> createCommissionPackage(@RequestBody CommissionPackageRequest request, Authentication authentication) {
        return ApiResponse.<CommissionPackageResponse>builder().status(201).message("Commission package created").data(contentService.createCommissionPackage(request, authentication)).build();
    }

    @Override
    public ApiResponse<CommissionPackageResponse> updateCommissionPackage(@PathVariable UUID id, @RequestBody CommissionPackageRequest request, Authentication authentication) {
        return ApiResponse.<CommissionPackageResponse>builder().status(200).message("Commission package updated").data(contentService.updateCommissionPackage(id, request, authentication)).build();
    }

    @Override
    public ApiResponse<CommissionPackageResponse> deactivateCommissionPackage(@PathVariable UUID id, Authentication authentication) {
        return ApiResponse.<CommissionPackageResponse>builder().status(200).message("Commission package deactivated").data(contentService.deactivateCommissionPackage(id, authentication)).build();
    }

    @Override
    public ApiResponse<CommissionAssignmentResponse> assignCommissionPackage(@PathVariable UUID hotelId, @PathVariable UUID packageId, Authentication authentication) {
        return ApiResponse.<CommissionAssignmentResponse>builder().status(200).message("Commission package assigned").data(contentService.assignCommissionPackage(hotelId, packageId, authentication)).build();
    }

    @Override
    public ApiResponse<List<PolicyResponse>> policies(@PathVariable UUID hotelId, @RequestParam(defaultValue = "false") boolean manage, Authentication authentication) {
        List<PolicyResponse> data = manage ? contentService.listPoliciesAdmin(hotelId, authentication) : contentService.listPoliciesPublic(hotelId);
        return ApiResponse.<List<PolicyResponse>>builder().status(200).message("Success").data(data).build();
    }

    @Override
    public ApiResponse<List<PolicyResponse>> adminPolicies(@PathVariable UUID hotelId, Authentication authentication) {
        return ApiResponse.<List<PolicyResponse>>builder().status(200).message("Success").data(contentService.listPoliciesAdmin(hotelId, authentication)).build();
    }

    @Override
    public ApiResponse<PolicyResponse> policyDetail(@PathVariable UUID hotelId, @PathVariable UUID policyId, Authentication authentication) {
        return ApiResponse.<PolicyResponse>builder().status(200).message("Success").data(contentService.policyDetail(hotelId, policyId, authentication)).build();
    }

    @Override
    public ApiResponse<PolicyResponse> createPolicy(@PathVariable UUID hotelId, @RequestBody @Valid PolicyMutationRequest request, Authentication authentication) {
        return ApiResponse.<PolicyResponse>builder().status(201).message("Policy created").data(contentService.createPolicy(hotelId, request, authentication)).build();
    }

    @Override
    public ApiResponse<PolicyResponse> updatePolicy(@PathVariable UUID hotelId, @PathVariable UUID policyId, @RequestBody @Valid PolicyMutationRequest request, Authentication authentication) {
        return ApiResponse.<PolicyResponse>builder().status(200).message("Policy updated").data(contentService.updatePolicy(hotelId, policyId, request, authentication)).build();
    }

    @Override
    public ApiResponse<PolicyResponse> deletePolicy(@PathVariable UUID hotelId, @PathVariable UUID policyId, Authentication authentication) {
        return ApiResponse.<PolicyResponse>builder().status(200).message("Policy deleted").data(contentService.deletePolicy(hotelId, policyId, authentication)).build();
    }
}
