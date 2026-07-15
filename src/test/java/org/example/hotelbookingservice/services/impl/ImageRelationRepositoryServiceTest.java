package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.response.media.ImageAssetResponse;
import org.example.hotelbookingservice.entity.BannerImage;
import org.example.hotelbookingservice.entity.HotelImage;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.NewsImage;
import org.example.hotelbookingservice.entity.ReviewImage;
import org.example.hotelbookingservice.entity.RoomTypeImage;
import org.example.hotelbookingservice.repository.BannerImageRepository;
import org.example.hotelbookingservice.repository.HotelImageRepository;
import org.example.hotelbookingservice.repository.NewsImageRepository;
import org.example.hotelbookingservice.repository.ReviewImageRepository;
import org.example.hotelbookingservice.repository.RoomTypeImageRepository;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageRelationRepositoryServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("75000000-0000-4000-8000-000000000001");
    private static final UUID HOTEL_ID = UUID.fromString("75000000-0000-4000-8000-000000000002");
    private static final UUID ROOM_TYPE_ID = UUID.fromString("75000000-0000-4000-8000-000000000003");
    private static final UUID REVIEW_ID = UUID.fromString("75000000-0000-4000-8000-000000000004");
    private static final UUID NEWS_ID = UUID.fromString("75000000-0000-4000-8000-000000000005");
    private static final UUID BANNER_ID = UUID.fromString("75000000-0000-4000-8000-000000000006");
    private static final UUID IMAGE_ID = UUID.fromString("75000000-0000-4000-8000-000000000007");
    private static final UUID HOTEL_IMAGE_RELATION_ID = UUID.fromString("75000000-0000-4000-8000-000000000008");

    @Mock NamedParameterJdbcTemplate jdbcTemplate;
    @Mock IFileStorageService fileStorageService;
    @Mock UploadProperties uploadProperties;
    @Mock HotelImageRepository hotelImageRepository;
    @Mock RoomTypeImageRepository roomTypeImageRepository;
    @Mock ReviewImageRepository reviewImageRepository;
    @Mock NewsImageRepository newsImageRepository;
    @Mock BannerImageRepository bannerImageRepository;
    @Mock Authentication authentication;

    @Test
    void replaceHotelImages_usesRepositoryAndPreservesSnapshotResponse() {
        AtomicReference<List<HotelImage>> savedImages = new AtomicReference<>(List.of());
        when(hotelImageRepository.saveAll(any())).thenAnswer(invocation -> {
            List<HotelImage> images = iterableToList(invocation.getArgument(0));
            savedImages.set(images);
            return images;
        });
        when(hotelImageRepository.findByHotel_IdOrderBySortOrderAsc(HOTEL_ID)).thenAnswer(invocation -> savedImages.get());
        MediaHarness service = new MediaHarness(List.of(image("/hotel.png")), jdbcTemplate, fileStorageService,
                uploadProperties, hotelImageRepository, roomTypeImageRepository);

        var snapshots = service.replaceHotelImages(HOTEL_ID, List.of(IMAGE_ID), authentication);

          verify(hotelImageRepository).deleteByHotel_Id(HOTEL_ID);
          verify(hotelImageRepository).flush();
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.getFirst().imageAssetId()).isEqualTo(IMAGE_ID);
        assertThat(snapshots.getFirst().url()).isEqualTo("/hotel.png");
        assertThat(snapshots.getFirst().sortOrder()).isZero();
    }

    @Test
    void replaceHotelImages_resolvesExistingHotelImageRelationIds() {
        Image existingAsset = new Image();
        existingAsset.setId(IMAGE_ID);
        HotelImage existingImage = new HotelImage();
        existingImage.setId(HOTEL_IMAGE_RELATION_ID);
        existingImage.setImageAsset(existingAsset);
        existingImage.setUrl("/existing-hotel.png");
        existingImage.setSortOrder(0);

        AtomicReference<List<HotelImage>> savedImages = new AtomicReference<>(List.of(existingImage));
        when(hotelImageRepository.findByHotel_IdOrderBySortOrderAsc(HOTEL_ID))
                .thenAnswer(invocation -> savedImages.get());
        when(hotelImageRepository.saveAll(any())).thenAnswer(invocation -> {
            List<HotelImage> images = iterableToList(invocation.getArgument(0));
            savedImages.set(images);
            return images;
        });

        MediaHarness service = new MediaHarness(List.of(), jdbcTemplate, fileStorageService,
                uploadProperties, hotelImageRepository, roomTypeImageRepository);

        var snapshots = service.replaceHotelImages(HOTEL_ID, List.of(HOTEL_IMAGE_RELATION_ID), authentication);

        verify(hotelImageRepository).deleteByHotel_Id(HOTEL_ID);
        verify(hotelImageRepository).flush();
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.getFirst().imageAssetId()).isEqualTo(IMAGE_ID);
        assertThat(snapshots.getFirst().url()).isEqualTo("/existing-hotel.png");
    }

    @Test
    void replaceRoomTypeImages_usesRepositoryAndPreservesSnapshotResponse() {
        AtomicReference<List<RoomTypeImage>> savedImages = new AtomicReference<>(List.of());
        when(jdbcTemplate.queryForObject(any(String.class), any(SqlParameterSource.class), eq(Boolean.class))).thenReturn(true);
        when(roomTypeImageRepository.saveAll(any())).thenAnswer(invocation -> {
            List<RoomTypeImage> images = iterableToList(invocation.getArgument(0));
            savedImages.set(images);
            return images;
        });
        when(roomTypeImageRepository.findByRoomType_IdOrderBySortOrderAsc(ROOM_TYPE_ID)).thenAnswer(invocation -> savedImages.get());
        MediaHarness service = new MediaHarness(List.of(image("/room.png")), jdbcTemplate, fileStorageService,
                uploadProperties, hotelImageRepository, roomTypeImageRepository);

        var snapshots = service.replaceRoomTypeImages(HOTEL_ID, ROOM_TYPE_ID, List.of(IMAGE_ID), authentication);

          verify(roomTypeImageRepository).deleteByRoomType_Id(ROOM_TYPE_ID);
          verify(roomTypeImageRepository).flush();
        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.getFirst().imageAssetId()).isEqualTo(IMAGE_ID);
        assertThat(snapshots.getFirst().url()).isEqualTo("/room.png");
    }

    @Test
    void reviewImages_saveAndListThroughRepository() {
        AtomicReference<List<ReviewImage>> savedImages = new AtomicReference<>(List.of());
        when(reviewImageRepository.saveAll(any())).thenAnswer(invocation -> {
            List<ReviewImage> images = iterableToList(invocation.getArgument(0));
            savedImages.set(images);
            return images;
        });
        when(reviewImageRepository.findByReview_IdOrderBySortOrderAsc(REVIEW_ID)).thenAnswer(invocation -> savedImages.get());
        ReviewHarness service = new ReviewHarness(List.of(image("/review.png")), jdbcTemplate, fileStorageService,
                uploadProperties, reviewImageRepository);

        service.replace(REVIEW_ID);
        var snapshots = service.list(REVIEW_ID);

        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.getFirst().imageAssetId()).isEqualTo(IMAGE_ID);
        assertThat(snapshots.getFirst().url()).isEqualTo("/review.png");
    }

    @Test
    void newsImages_replaceAndListThroughRepository() {
        AtomicReference<List<NewsImage>> savedImages = new AtomicReference<>(List.of());
        when(uploadProperties.maxImageCount()).thenReturn(12);
          when(newsImageRepository.findByNews_IdAndIdIn(eq(NEWS_ID), anyCollection())).thenReturn(List.of());
          when(newsImageRepository.saveAll(any())).thenAnswer(invocation -> {
              List<NewsImage> images = iterableToList(invocation.getArgument(0));
              assertThat(images).allSatisfy(image ->
                      assertThat(image.getId()).as("JPA should generate new relation IDs").isNull());
              images.forEach(image -> image.setId(UUID.randomUUID()));
              savedImages.set(images);
              return images;
          });
        when(newsImageRepository.findByNews_IdOrderBySortOrderAsc(NEWS_ID)).thenAnswer(invocation -> savedImages.get());
        ContentHarness service = new ContentHarness(List.of(image("/news.png")), jdbcTemplate, fileStorageService,
                uploadProperties, newsImageRepository, bannerImageRepository);

        service.replaceNews(NEWS_ID, List.of(IMAGE_ID));
        var images = service.listNews(NEWS_ID);

          verify(newsImageRepository).deleteByNews_Id(NEWS_ID);
          verify(newsImageRepository).flush();
        assertThat(images).hasSize(1);
        assertThat(images.getFirst().id()).isNotNull();
        assertThat(images.getFirst().newsId()).isEqualTo(NEWS_ID);
        assertThat(images.getFirst().url()).isEqualTo("/news.png");
    }

    @Test
    void bannerImages_replaceAndListThroughRepository() {
        AtomicReference<List<BannerImage>> savedImages = new AtomicReference<>(List.of());
        when(bannerImageRepository.saveAll(any())).thenAnswer(invocation -> {
            List<BannerImage> images = iterableToList(invocation.getArgument(0));
            savedImages.set(images);
            return images;
        });
        when(bannerImageRepository.findByBanner_IdOrderBySortOrderAsc(BANNER_ID)).thenAnswer(invocation -> savedImages.get());
        ContentHarness service = new ContentHarness(List.of(image("/banner.png")), jdbcTemplate, fileStorageService,
                uploadProperties, newsImageRepository, bannerImageRepository);

        service.replaceBanner(BANNER_ID, IMAGE_ID, "/banner.png");
        var images = service.listBanner(BANNER_ID);

          verify(bannerImageRepository).deleteByBanner_Id(BANNER_ID);
          verify(bannerImageRepository).flush();
        assertThat(images).hasSize(1);
        assertThat(images.getFirst().bannerId()).isEqualTo(BANNER_ID);
        assertThat(images.getFirst().url()).isEqualTo("/banner.png");
    }

    private ImageAssetResponse image(String url) {
        return new ImageAssetResponse(IMAGE_ID, OWNER_ID, "LOCAL", "local/" + IMAGE_ID, url, url, 1200, 800, 1024L, Instant.now());
    }

    private static <T> List<T> iterableToList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    private static class MediaHarness extends MediaServiceImpl {
        private final List<ImageAssetResponse> images;

        MediaHarness(List<ImageAssetResponse> images,
                     NamedParameterJdbcTemplate jdbcTemplate,
                     IFileStorageService fileStorageService,
                     UploadProperties uploadProperties,
                     HotelImageRepository hotelImageRepository,
                     RoomTypeImageRepository roomTypeImageRepository) {
            super(jdbcTemplate, fileStorageService, uploadProperties, hotelImageRepository, roomTypeImageRepository);
            this.images = images;
        }

        @Override
        protected CurrentUser requireUser(Authentication authentication) {
            return new CurrentUser(OWNER_ID);
        }

        @Override
        protected void requireCanManageHotel(UUID hotelId, CurrentUser user) {
        }

        @Override
        protected List<ImageAssetResponse> requireOwnedImages(List<UUID> imageIds, CurrentUser user) {
            return images;
        }
    }

    private static class ReviewHarness extends ReviewOperationsServiceImpl {
        private final List<ImageAssetResponse> images;

        ReviewHarness(List<ImageAssetResponse> images,
                      NamedParameterJdbcTemplate jdbcTemplate,
                      IFileStorageService fileStorageService,
                      UploadProperties uploadProperties,
                      ReviewImageRepository reviewImageRepository) {
            super(jdbcTemplate, fileStorageService, uploadProperties, reviewImageRepository);
            this.images = images;
        }

        void replace(UUID reviewId) {
            insertReviewImages(reviewId, List.of(IMAGE_ID), new CurrentUser(OWNER_ID));
        }

        List<org.example.hotelbookingservice.dto.response.media.ImageSnapshotResponse> list(UUID reviewId) {
            return listReviewImages(reviewId);
        }

        @Override
        protected List<ImageAssetResponse> requireOwnedImages(List<UUID> imageIds, CurrentUser user) {
            return images;
        }
    }

    private static class ContentHarness extends ContentServiceImpl {
        private final List<ImageAssetResponse> images;

        ContentHarness(List<ImageAssetResponse> images,
                       NamedParameterJdbcTemplate jdbcTemplate,
                       IFileStorageService fileStorageService,
                       UploadProperties uploadProperties,
                       NewsImageRepository newsImageRepository,
                       BannerImageRepository bannerImageRepository) {
            super(jdbcTemplate, fileStorageService, uploadProperties, null, null, null, null,
                    newsImageRepository, bannerImageRepository, null, null);
            this.images = images;
        }

        void replaceNews(UUID newsId, List<UUID> imageIds) {
            replaceNewsImages(newsId, imageIds, new CurrentUser(OWNER_ID));
        }

        List<org.example.hotelbookingservice.dto.response.content.NewsImageResponse> listNews(UUID newsId) {
            return listNewsImages(newsId);
        }

        void replaceBanner(UUID bannerId, UUID imageAssetId, String url) {
            replaceBannerImages(bannerId, List.of(new BannerImageSource(imageAssetId, url)));
        }

        List<org.example.hotelbookingservice.dto.response.content.BannerImageResponse> listBanner(UUID bannerId) {
            return listBannerImages(bannerId, null);
        }

        @Override
        protected List<ImageAssetResponse> requireOwnedImages(List<UUID> imageIds, CurrentUser user) {
            return images;
        }
    }
}
