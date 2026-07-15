package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.content.BannerMutationRequest;
import org.example.hotelbookingservice.dto.request.content.CommissionPackageRequest;
import org.example.hotelbookingservice.dto.request.content.ContactCreateRequest;
import org.example.hotelbookingservice.dto.request.content.ContactUpdateRequest;
import org.example.hotelbookingservice.dto.request.content.NewsMutationRequest;
import org.example.hotelbookingservice.dto.request.content.PolicyMutationRequest;
import org.example.hotelbookingservice.dto.response.common.ListResponse;
import org.example.hotelbookingservice.dto.response.content.BannerResponse;
import org.example.hotelbookingservice.dto.response.content.CommissionAssignmentResponse;
import org.example.hotelbookingservice.dto.response.content.CommissionPackageResponse;
import org.example.hotelbookingservice.dto.response.content.ContactCreateResponse;
import org.example.hotelbookingservice.dto.response.content.ContactResponse;
import org.example.hotelbookingservice.dto.response.content.NewsResponse;
import org.example.hotelbookingservice.dto.response.content.NotificationResponse;
import org.example.hotelbookingservice.dto.response.content.PolicyResponse;
import org.example.hotelbookingservice.dto.response.content.UnreadCountResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1")
@Tag(name = "Content Management", description = "Manage news, banners, contacts, notifications, commissions, and policies")
public interface ContentApi {

    @Operation(summary = "List public news")
    @GetMapping("/news")
    ApiResponse<ListResponse<NewsResponse>> publicNews(@RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit);

    @Operation(summary = "Get public news detail")
    @GetMapping("/news/{slug}")
    ApiResponse<NewsResponse> publicNewsDetail(@PathVariable String slug);

    @Operation(summary = "List admin news")
      @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER')")
      @GetMapping("/admin/news")
    ApiResponse<ListResponse<NewsResponse>> adminNews(@RequestParam(required = false) String status, @RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication);

    @Operation(summary = "Get admin news detail")
      @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER')")
      @GetMapping("/admin/news/{id}")
    ApiResponse<NewsResponse> adminNewsDetail(@PathVariable UUID id, Authentication authentication);

    @Operation(summary = "Create news")
      @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER')")
      @PostMapping("/admin/news")
    ApiResponse<NewsResponse> createNews(@RequestBody @Valid NewsMutationRequest request, Authentication authentication);

    @Operation(summary = "Update news")
      @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER')")
      @RequestMapping(path = "/admin/news/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    ApiResponse<NewsResponse> updateNews(@PathVariable UUID id, @RequestBody NewsMutationRequest request, Authentication authentication);

    @Operation(summary = "Delete news")
      @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER')")
      @DeleteMapping("/admin/news/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteNews(@PathVariable UUID id, Authentication authentication);

    @Operation(summary = "List public banners")
    @GetMapping("/banners")
    ApiResponse<List<BannerResponse>> publicBanners();

    @Operation(summary = "List admin banners")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/banners")
    ApiResponse<List<BannerResponse>> adminBanners(Authentication authentication);

    @Operation(summary = "Create banner")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/banners")
    ApiResponse<BannerResponse> createBanner(@RequestBody BannerMutationRequest request, Authentication authentication);

    @Operation(summary = "Update banner")
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = "/admin/banners/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    ApiResponse<BannerResponse> updateBanner(@PathVariable UUID id, @RequestBody BannerMutationRequest request, Authentication authentication);

    @Operation(summary = "Delete banner")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin/banners/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBanner(@PathVariable UUID id, Authentication authentication);

    @Operation(summary = "Create contact")
    @PostMapping({"/contacts", "/contact"})
    ApiResponse<ContactCreateResponse> createContact(@RequestBody @Valid ContactCreateRequest request, HttpServletRequest servletRequest, Authentication authentication);

    @Operation(summary = "List contacts")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/contacts")
    ApiResponse<ListResponse<ContactResponse>> contacts(@RequestParam(required = false) String status, @RequestParam(required = false) String q, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication);

    @Operation(summary = "Get contact detail")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/contacts/{id}")
    ApiResponse<ContactResponse> contactDetail(@PathVariable UUID id, Authentication authentication);

    @Operation(summary = "Update contact")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/admin/contacts/{id}")
    ApiResponse<ContactResponse> updateContact(@PathVariable UUID id, @RequestBody ContactUpdateRequest request, Authentication authentication);

    @Operation(summary = "List notifications")
    @GetMapping("/notifications")
    ApiResponse<ListResponse<NotificationResponse>> notifications(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit, Authentication authentication);

    @Operation(summary = "Get unread notification count")
    @GetMapping("/notifications/unread-count")
    ApiResponse<UnreadCountResponse> unreadCount(Authentication authentication);

    @Operation(summary = "Mark notification as read")
    @PatchMapping("/notifications/{id}/read")
    ApiResponse<NotificationResponse> markRead(@PathVariable UUID id, Authentication authentication);

    @Operation(summary = "Mark all notifications as read")
    @PatchMapping("/notifications/read-all")
    ApiResponse<Void> markAllRead(Authentication authentication);

    @Operation(summary = "Delete notification")
    @DeleteMapping("/notifications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteNotification(@PathVariable UUID id, Authentication authentication);

    @Operation(summary = "List commission packages")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping({"/admin/commission-packages", "/commission-packages"})
    ApiResponse<List<CommissionPackageResponse>> commissionPackages(Authentication authentication);

    @Operation(summary = "Get commission package detail")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping({"/admin/commission-packages/{id}", "/commission-packages/{id}"})
    ApiResponse<CommissionPackageResponse> commissionPackage(@PathVariable UUID id, Authentication authentication);

    @Operation(summary = "Create commission package")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping({"/admin/commission-packages", "/commission-packages"})
    ApiResponse<CommissionPackageResponse> createCommissionPackage(@RequestBody CommissionPackageRequest request, Authentication authentication);

    @Operation(summary = "Update commission package")
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = {"/admin/commission-packages/{id}", "/commission-packages/{id}"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    ApiResponse<CommissionPackageResponse> updateCommissionPackage(@PathVariable UUID id, @RequestBody CommissionPackageRequest request, Authentication authentication);

    @Operation(summary = "Deactivate commission package")
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(
            path = {"/admin/commission-packages/{id}", "/commission-packages/{id}", "/admin/commission-packages/{id}/deactivate", "/commission-packages/{id}/deactivate"},
            method = {RequestMethod.DELETE, RequestMethod.PATCH}
    )
    ApiResponse<CommissionPackageResponse> deactivateCommissionPackage(@PathVariable UUID id, Authentication authentication);

    @Operation(summary = "Assign commission package to hotel")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/hotels/{hotelId}/commission-package/{packageId}")
    ApiResponse<CommissionAssignmentResponse> assignCommissionPackage(@PathVariable UUID hotelId, @PathVariable UUID packageId, Authentication authentication);

    @Operation(summary = "List hotel policies")
    @GetMapping("/hotels/{hotelId}/policies")
    ApiResponse<List<PolicyResponse>> policies(@PathVariable UUID hotelId, @RequestParam(defaultValue = "false") boolean manage, Authentication authentication);

    @Operation(summary = "List admin hotel policies")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/hotels/{hotelId}/policies")
    ApiResponse<List<PolicyResponse>> adminPolicies(@PathVariable UUID hotelId, Authentication authentication);

    @Operation(summary = "Get policy detail")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/hotels/{hotelId}/policies/{policyId}")
    ApiResponse<PolicyResponse> policyDetail(@PathVariable UUID hotelId, @PathVariable UUID policyId, Authentication authentication);

    @Operation(summary = "Create policy")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/hotels/{hotelId}/policies")
    ApiResponse<PolicyResponse> createPolicy(@PathVariable UUID hotelId, @RequestBody @Valid PolicyMutationRequest request, Authentication authentication);

    @Operation(summary = "Update policy")
    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = "/admin/hotels/{hotelId}/policies/{policyId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    ApiResponse<PolicyResponse> updatePolicy(@PathVariable UUID hotelId, @PathVariable UUID policyId, @RequestBody @Valid PolicyMutationRequest request, Authentication authentication);

    @Operation(summary = "Delete policy")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin/hotels/{hotelId}/policies/{policyId}")
    ApiResponse<PolicyResponse> deletePolicy(@PathVariable UUID hotelId, @PathVariable UUID policyId, Authentication authentication);
}
