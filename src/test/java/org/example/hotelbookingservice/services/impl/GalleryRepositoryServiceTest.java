package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.response.media.ImageAssetResponse;
import org.example.hotelbookingservice.entity.FolderGallery;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.ImageGallery;
import org.example.hotelbookingservice.repository.FolderGalleryRepository;
import org.example.hotelbookingservice.repository.HotelImageRepository;
import org.example.hotelbookingservice.repository.ImageGalleryRepository;
import org.example.hotelbookingservice.repository.RoomTypeImageRepository;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GalleryRepositoryServiceTest {
    private static final UUID OWNER_ID = UUID.fromString("78000000-0000-4000-8000-000000000001");
    private static final UUID FOLDER_ID = UUID.fromString("78000000-0000-4000-8000-000000000002");
    private static final UUID IMAGE_ID = UUID.fromString("78000000-0000-4000-8000-000000000003");

    @Mock NamedParameterJdbcTemplate jdbcTemplate;
    @Mock IFileStorageService fileStorageService;
    @Mock UploadProperties uploadProperties;
    @Mock HotelImageRepository hotelImageRepository;
    @Mock RoomTypeImageRepository roomTypeImageRepository;
    @Mock FolderGalleryRepository folderGalleryRepository;
    @Mock ImageGalleryRepository imageGalleryRepository;
    @Mock Authentication authentication;

    @Test
    void listAndCreateGalleryFolders_useFolderRepository() {
        when(folderGalleryRepository.findByOwnerAccountIdOrderByFolderNameAsc(OWNER_ID))
                .thenReturn(List.of(folder("hotel-gallery")));
        when(folderGalleryRepository.save(any(FolderGallery.class))).thenAnswer(invocation -> {
            FolderGallery saved = invocation.getArgument(0);
            saved.setId(FOLDER_ID);
            return saved;
        });
        GalleryHarness service = service(List.of(image("/gallery.png")));

        var folders = service.listGalleryFolders(authentication);
        var created = service.createGalleryFolder(" Hotel Gallery ", authentication);

        assertThat(folders).hasSize(1);
        assertThat(folders.getFirst().id()).isEqualTo(FOLDER_ID);
        assertThat(folders.getFirst().folderName()).isEqualTo("hotel-gallery");
        assertThat(created.id()).isEqualTo(FOLDER_ID);
        assertThat(created.folderName()).isEqualTo("Hotel Gallery");
    }

    @Test
    void listGalleryImages_checksFolderOwnershipAndMapsImageAssets() {
        when(folderGalleryRepository.existsByIdAndOwnerAccountId(FOLDER_ID, OWNER_ID)).thenReturn(true);
        when(imageGalleryRepository.findByFolderIdWithImageAssetOrderByCreatedAtDesc(FOLDER_ID))
                .thenReturn(List.of(galleryImage(folder("hotel-gallery"), imageAsset("/gallery.png"))));
        GalleryHarness service = service(List.of(image("/gallery.png")));

        var images = service.listGalleryImages(FOLDER_ID, authentication);

        assertThat(images).hasSize(1);
        assertThat(images.getFirst().id()).isEqualTo(IMAGE_ID);
        assertThat(images.getFirst().url()).isEqualTo("/gallery.png");
    }

    @Test
    void uploadGalleryImages_reusesUploadManyAndPersistsGalleryLinks() {
        AtomicReference<ImageGallery> savedGalleryImage = new AtomicReference<>();
        when(folderGalleryRepository.findByOwnerAccountIdAndFolderName(OWNER_ID, "hotel-gallery"))
                .thenReturn(Optional.of(folder("hotel-gallery")));
        when(imageGalleryRepository.existsByFolderIdAndImageAssetId(FOLDER_ID, IMAGE_ID)).thenReturn(false);
        when(imageGalleryRepository.save(any(ImageGallery.class))).thenAnswer(invocation -> {
            ImageGallery saved = invocation.getArgument(0);
            savedGalleryImage.set(saved);
            return saved;
        });
        GalleryHarness service = service(List.of(image("/uploaded.png")));

        var uploaded = service.uploadGalleryImages("hotel-gallery", List.of(new StubMultipartFile()), authentication);

        assertThat(uploaded).hasSize(1);
        assertThat(uploaded.getFirst().id()).isEqualTo(IMAGE_ID);
        assertThat(savedGalleryImage.get().getFolderId()).isEqualTo(FOLDER_ID);
        assertThat(savedGalleryImage.get().getImageAssetId()).isEqualTo(IMAGE_ID);
    }

    private GalleryHarness service(List<ImageAssetResponse> uploadedImages) {
        return new GalleryHarness(uploadedImages, jdbcTemplate, fileStorageService, uploadProperties,
                hotelImageRepository, roomTypeImageRepository, folderGalleryRepository, imageGalleryRepository);
    }

    private FolderGallery folder(String name) {
        FolderGallery folder = new FolderGallery();
        folder.setId(FOLDER_ID);
        folder.setOwnerAccountId(OWNER_ID);
        folder.setFolderName(name);
        folder.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        folder.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return folder;
    }

    private ImageGallery galleryImage(FolderGallery folder, Image imageAsset) {
        ImageGallery galleryImage = new ImageGallery();
        galleryImage.setFolder(folder);
        galleryImage.setImageAsset(imageAsset);
        return galleryImage;
    }

    private Image imageAsset(String url) {
        Image image = new Image();
        image.setId(IMAGE_ID);
        image.setOwnerAccountId(OWNER_ID);
        image.setProvider("LOCAL");
        image.setPublicId("local/" + IMAGE_ID);
        image.setPath(url);
        image.setSecureUrl(url);
        image.setWidth(1200);
        image.setHeight(800);
        image.setBytes(1024L);
        image.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return image;
    }

    private ImageAssetResponse image(String url) {
        return new ImageAssetResponse(IMAGE_ID, OWNER_ID, "LOCAL", "local/" + IMAGE_ID, url, url, 1200, 800, 1024L,
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private static class GalleryHarness extends MediaServiceImpl {
        private final List<ImageAssetResponse> uploadedImages;

        GalleryHarness(List<ImageAssetResponse> uploadedImages,
                       NamedParameterJdbcTemplate jdbcTemplate,
                       IFileStorageService fileStorageService,
                       UploadProperties uploadProperties,
                       HotelImageRepository hotelImageRepository,
                       RoomTypeImageRepository roomTypeImageRepository,
                       FolderGalleryRepository folderGalleryRepository,
                       ImageGalleryRepository imageGalleryRepository) {
            super(jdbcTemplate, fileStorageService, uploadProperties, hotelImageRepository, roomTypeImageRepository,
                    folderGalleryRepository, imageGalleryRepository);
            this.uploadedImages = uploadedImages;
        }

        @Override
        protected CurrentUser requireUser(Authentication authentication) {
            return new CurrentUser(OWNER_ID);
        }

        @Override
        public List<ImageAssetResponse> uploadMany(List<MultipartFile> files, Authentication authentication) {
            return uploadedImages;
        }
    }

    private static class StubMultipartFile implements MultipartFile {
        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return "gallery.png";
        }

        @Override
        public String getContentType() {
            return "image/png";
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public long getSize() {
            return 10;
        }

        @Override
        public byte[] getBytes() {
            return new byte[] {1};
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(getBytes());
        }

        @Override
        public void transferTo(java.io.File dest) {
        }
    }
}
