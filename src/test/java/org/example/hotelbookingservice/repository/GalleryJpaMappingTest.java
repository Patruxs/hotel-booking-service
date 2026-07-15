package org.example.hotelbookingservice.repository;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GalleryJpaMappingTest {
    private static final UUID ACCOUNT_ID = UUID.fromString("77000000-0000-4000-8000-000000000001");
    private static final UUID FOLDER_ID = UUID.fromString("77000000-0000-4000-8000-000000000002");
    private static final UUID IMAGE_ASSET_ID = UUID.fromString("77000000-0000-4000-8000-000000000003");
    private static final UUID GALLERY_IMAGE_ID = UUID.fromString("77000000-0000-4000-8000-000000000004");

    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired FolderGalleryRepository folderGalleryRepository;
    @Autowired ImageGalleryRepository imageGalleryRepository;

    @Test
    void galleryEntitiesLoadAgainstFlywaySchema() {
        insertGalleryRows();
        var persistenceUnit = entityManagerFactory.getPersistenceUnitUtil();

        var folder = folderGalleryRepository.findByOwnerAccountIdAndFolderName(ACCOUNT_ID, "hotel-gallery").orElseThrow();
        var galleryImage = imageGalleryRepository.findById(GALLERY_IMAGE_ID).orElseThrow();

        assertThat(folder.getId()).isEqualTo(FOLDER_ID);
        assertThat(folder.getOwnerAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(folder.getFolderName()).isEqualTo("hotel-gallery");
        assertThat(folder.getCreatedAt()).isNotNull();
        assertThat(folder.getUpdatedAt()).isNotNull();

        assertThat(galleryImage.getFolderId()).isEqualTo(FOLDER_ID);
        assertThat(galleryImage.getImageAssetId()).isEqualTo(IMAGE_ASSET_ID);
        assertThat(galleryImage.getCreatedAt()).isNotNull();
        assertThat(persistenceUnit.isLoaded(galleryImage, "folder")).isTrue();
        assertThat(persistenceUnit.isLoaded(galleryImage, "imageAsset")).isFalse();
    }

    private void insertGalleryRows() {
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, 'gallery-mapping@example.com', 'hash', 'Gallery', 'Mapping', true)
                on conflict (id) do nothing
                """, ACCOUNT_ID);
        jdbc.update("""
                insert into image_assets (id, owner_account_id, provider, public_id, url, secure_url, width, height, bytes)
                values (?, ?, 'LOCAL', 'local/gallery-mapping', '/gallery.png', '/gallery.png', 1200, 800, 1024)
                on conflict (id) do nothing
                """, IMAGE_ASSET_ID, ACCOUNT_ID);
        jdbc.update("""
                insert into gallery_folders (id, owner_account_id, folder_name)
                values (?, ?, 'hotel-gallery')
                on conflict (id) do nothing
                """, FOLDER_ID, ACCOUNT_ID);
        jdbc.update("""
                insert into gallery_images (id, folder_id, image_asset_id)
                values (?, ?, ?)
                on conflict (id) do nothing
                """, GALLERY_IMAGE_ID, FOLDER_ID, IMAGE_ASSET_ID);
    }
}
