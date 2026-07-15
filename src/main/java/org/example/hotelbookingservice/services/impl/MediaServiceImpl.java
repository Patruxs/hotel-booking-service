package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.response.media.GalleryFolderResponse;
import org.example.hotelbookingservice.dto.response.media.ImageAssetResponse;
import org.example.hotelbookingservice.dto.response.media.ImageSnapshotResponse;
import org.example.hotelbookingservice.entity.FolderGallery;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.ImageGallery;
import org.example.hotelbookingservice.repository.FolderGalleryRepository;
import org.example.hotelbookingservice.repository.HotelImageRepository;
import org.example.hotelbookingservice.repository.ImageGalleryRepository;
import org.example.hotelbookingservice.repository.RoomTypeImageRepository;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.example.hotelbookingservice.services.IMediaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MediaServiceImpl extends Milestone6ServiceSupport implements IMediaService {
    private final FolderGalleryRepository folderGalleryRepository;
    private final ImageGalleryRepository imageGalleryRepository;

    public MediaServiceImpl(NamedParameterJdbcTemplate jdbcTemplate, IFileStorageService fileStorageService, UploadProperties uploadProperties) {
        super(jdbcTemplate, fileStorageService, uploadProperties);
        this.folderGalleryRepository = null;
        this.imageGalleryRepository = null;
    }

    @Autowired
    public MediaServiceImpl(NamedParameterJdbcTemplate jdbcTemplate,
                            IFileStorageService fileStorageService,
                            UploadProperties uploadProperties,
                            HotelImageRepository hotelImageRepository,
                            RoomTypeImageRepository roomTypeImageRepository,
                            FolderGalleryRepository folderGalleryRepository,
                            ImageGalleryRepository imageGalleryRepository) {
        super(jdbcTemplate, fileStorageService, uploadProperties, hotelImageRepository, roomTypeImageRepository, null, null, null);
        this.folderGalleryRepository = folderGalleryRepository;
        this.imageGalleryRepository = imageGalleryRepository;
    }

    public MediaServiceImpl(NamedParameterJdbcTemplate jdbcTemplate,
                            IFileStorageService fileStorageService,
                            UploadProperties uploadProperties,
                            HotelImageRepository hotelImageRepository,
                            RoomTypeImageRepository roomTypeImageRepository) {
        super(jdbcTemplate, fileStorageService, uploadProperties, hotelImageRepository, roomTypeImageRepository, null, null, null);
        this.folderGalleryRepository = null;
        this.imageGalleryRepository = null;
    }

    @Override
    @Transactional
    public ImageAssetResponse upload(MultipartFile file, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        return createImageAsset(file, user.accountId());
    }

    @Override
    @Transactional
    public List<ImageAssetResponse> uploadMany(List<MultipartFile> files, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (files == null || files.isEmpty()) {
            throw badRequest("At least one file is required");
        }
        List<ImageAssetResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(createImageAsset(file, user.accountId()));
        }
        return responses;
    }

    @Override
    @Transactional
    public ImageAssetResponse uploadAvatar(MultipartFile file, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ImageAssetResponse oldAvatar = currentAvatarImage(user.accountId());
        ImageAssetResponse image = createImageAsset(file, user.accountId());
        jdbcTemplate.update("""
                update accounts
                set avatar_url = :avatarUrl, updated_at = now()
                where id = :accountId
                """, new MapSqlParameterSource("avatarUrl", image.secureUrl() == null ? image.url() : image.secureUrl())
                .addValue("accountId", user.accountId()));
        cleanupProviderImage(oldAvatar);
        return image;
    }

    @Override
    @Transactional
    public void deleteAvatar(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        ImageAssetResponse oldAvatar = currentAvatarImage(user.accountId());
        jdbcTemplate.update("""
                update accounts
                set avatar_url = null, updated_at = now()
                where id = :accountId
                """, new MapSqlParameterSource("accountId", user.accountId()));
        cleanupProviderImage(oldAvatar);
    }

    @Override
    public List<ImageAssetResponse> listProviderAssets(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", user.accountId());
        String ownership = isAdmin(user) ? "" : "where owner_account_id = :accountId";
        return jdbcTemplate.query("""
                select *
                from image_assets
                """ + ownership + """
                order by created_at desc
                limit 200
                """, params, (rs, rowNum) -> mapImageAsset(rs));
    }

    @Override
    public List<GalleryFolderResponse> listGalleryFolders(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (folderGalleryRepository != null) {
            return folderGalleryRepository.findByOwnerAccountIdOrderByFolderNameAsc(user.accountId()).stream()
                    .map(this::toGalleryFolderResponse)
                    .toList();
        }
        return jdbcTemplate.query("""
                select *
                from gallery_folders
                where owner_account_id = :accountId
                order by folder_name
                """, new MapSqlParameterSource("accountId", user.accountId()), (rs, rowNum) -> mapGalleryFolder(rs));
    }

    @Override
    @Transactional
    public GalleryFolderResponse createGalleryFolder(String folderName, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        String normalized = normalizeFolderName(folderName);
        if (folderGalleryRepository != null) {
            FolderGallery folder = new FolderGallery();
            folder.setOwnerAccountId(user.accountId());
            folder.setFolderName(normalized);
            try {
                return toGalleryFolderResponse(folderGalleryRepository.save(folder));
            } catch (DataIntegrityViolationException ex) {
                throw conflict("Gallery folder name already exists");
            }
        }
        UUID id = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into gallery_folders (id, owner_account_id, folder_name)
                    values (:id, :ownerId, :folderName)
                    """, new MapSqlParameterSource("id", id)
                    .addValue("ownerId", user.accountId())
                    .addValue("folderName", normalized));
        } catch (RuntimeException ex) {
            if (ex instanceof ResponseStatusException) {
                throw ex;
            }
            throw conflict("Gallery folder name already exists");
        }
        return queryGalleryFolder(user.accountId(), normalized);
    }

    @Override
    public List<ImageAssetResponse> listGalleryImages(UUID folderId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (folderGalleryRepository != null && imageGalleryRepository != null) {
            requireFolderOwnerEntity(folderId, user.accountId());
            return imageGalleryRepository.findByFolderIdWithImageAssetOrderByCreatedAtDesc(folderId).stream()
                    .map(ImageGallery::getImageAsset)
                    .map(this::toImageAssetResponse)
                    .toList();
        }
        requireFolderOwner(folderId, user.accountId());
        return jdbcTemplate.query("""
                select ia.*
                from gallery_images gi
                join image_assets ia on ia.id = gi.image_asset_id
                where gi.folder_id = :folderId
                order by gi.created_at desc
                """, new MapSqlParameterSource("folderId", folderId), (rs, rowNum) -> mapImageAsset(rs));
    }

    @Override
    @Transactional
    public List<ImageAssetResponse> uploadGalleryImages(String folderName, List<MultipartFile> files, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        validateGalleryUploadBatch(files);
        String normalized = normalizeFolderName(folderName);
        GalleryFolderResponse folder = folderGalleryRepository == null
                ? getOrCreateGalleryFolder(normalized, user.accountId())
                : getOrCreateGalleryFolderEntity(normalized, user.accountId());
        List<ImageAssetResponse> uploaded = uploadMany(files, authentication);
        if (imageGalleryRepository != null) {
            FolderGallery folderReference = new FolderGallery();
            folderReference.setId(folder.id());
            for (ImageAssetResponse image : uploaded) {
                if (!imageGalleryRepository.existsByFolderIdAndImageAssetId(folder.id(), image.id())) {
                    imageGalleryRepository.save(newImageGallery(folderReference, image.id()));
                }
            }
            return uploaded;
        }
        for (ImageAssetResponse image : uploaded) {
            jdbcTemplate.update("""
                    insert into gallery_images (id, folder_id, image_asset_id)
                    values (:id, :folderId, :imageAssetId)
                    on conflict do nothing
                    """, new MapSqlParameterSource("id", UUID.randomUUID())
                    .addValue("folderId", folder.id())
                    .addValue("imageAssetId", image.id()));
        }
        return uploaded;
    }

    private void requireFolderOwnerEntity(UUID folderId, UUID ownerId) {
        if (!folderGalleryRepository.existsByIdAndOwnerAccountId(folderId, ownerId)) {
            throw notFound("Gallery folder not found");
        }
    }

    private GalleryFolderResponse getOrCreateGalleryFolderEntity(String folderName, UUID ownerId) {
        return folderGalleryRepository.findByOwnerAccountIdAndFolderName(ownerId, folderName)
                .map(this::toGalleryFolderResponse)
                .orElseGet(() -> createGalleryFolderEntity(folderName, ownerId));
    }

    private GalleryFolderResponse createGalleryFolderEntity(String folderName, UUID ownerId) {
        FolderGallery folder = new FolderGallery();
        folder.setOwnerAccountId(ownerId);
        folder.setFolderName(folderName);
        try {
            return toGalleryFolderResponse(folderGalleryRepository.save(folder));
        } catch (DataIntegrityViolationException ignored) {
            return folderGalleryRepository.findByOwnerAccountIdAndFolderName(ownerId, folderName)
                    .map(this::toGalleryFolderResponse)
                    .orElseThrow(() -> conflict("Gallery folder name already exists"));
        }
    }

    private ImageGallery newImageGallery(FolderGallery folder, UUID imageAssetId) {
        Image imageAsset = new Image();
        imageAsset.setId(imageAssetId);
        ImageGallery galleryImage = new ImageGallery();
        galleryImage.setFolder(folder);
        galleryImage.setImageAsset(imageAsset);
        return galleryImage;
    }

    private GalleryFolderResponse toGalleryFolderResponse(FolderGallery folder) {
        return new GalleryFolderResponse(
                folder.getId(),
                folder.getOwnerAccountId(),
                folder.getFolderName(),
                folder.getFolderName(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }

    private ImageAssetResponse toImageAssetResponse(Image image) {
        return new ImageAssetResponse(
                image.getUuid(),
                image.getOwnerAccountId(),
                image.getProvider(),
                image.getPublicId(),
                image.getPath(),
                image.getSecureUrl(),
                image.getWidth(),
                image.getHeight(),
                image.getBytes(),
                image.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public List<ImageSnapshotResponse> replaceHotelImages(UUID hotelId, List<UUID> imageIds, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        List<HotelImageSource> images = resolveHotelImages(hotelId, imageIds, user);
        if (hotelImageRepository != null) {
            hotelImageRepository.deleteByHotel_Id(hotelId);
            hotelImageRepository.flush();
            List<org.example.hotelbookingservice.entity.HotelImage> hotelImages = new ArrayList<>();
            int order = 0;
            for (HotelImageSource image : images) {
                hotelImages.add(newHotelImage(hotelId, image, order++));
            }
            hotelImageRepository.saveAll(hotelImages);
        } else {
            jdbcTemplate.update("delete from hotel_images where hotel_id = :hotelId", new MapSqlParameterSource("hotelId", hotelId));
            int order = 0;
            for (HotelImageSource image : images) {
                jdbcTemplate.update("""
                        insert into hotel_images (id, hotel_id, image_asset_id, url, sort_order)
                        values (:id, :hotelId, :imageAssetId, :url, :sortOrder)
                        """, new MapSqlParameterSource("id", UUID.randomUUID())
                        .addValue("hotelId", hotelId)
                        .addValue("imageAssetId", image.imageAssetId())
                        .addValue("url", normalizeImageUrl(image.url()))
                        .addValue("sortOrder", order++));
            }
        }
        return listHotelImages(hotelId);
    }

    @Override
    @Transactional
    public List<ImageSnapshotResponse> replaceRoomTypeImages(UUID hotelId, UUID roomTypeId, List<UUID> imageIds, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanManageHotel(hotelId, user);
        Boolean belongs = jdbcTemplate.queryForObject("""
                select exists(select 1 from room_types where id = :roomTypeId and hotel_id = :hotelId and deleted_at is null)
                """, new MapSqlParameterSource("roomTypeId", roomTypeId).addValue("hotelId", hotelId), Boolean.class);
        if (!Boolean.TRUE.equals(belongs)) {
            throw notFound("Room type not found");
        }
          List<ImageAssetResponse> images = requireOwnedImages(imageIds, user);
          if (roomTypeImageRepository != null) {
              roomTypeImageRepository.deleteByRoomType_Id(roomTypeId);
              roomTypeImageRepository.flush();
              List<org.example.hotelbookingservice.entity.RoomTypeImage> roomTypeImages = new ArrayList<>();
            int order = 0;
            for (ImageAssetResponse image : images) {
                roomTypeImages.add(newRoomTypeImage(roomTypeId, image, order++));
            }
            roomTypeImageRepository.saveAll(roomTypeImages);
        } else {
            jdbcTemplate.update("delete from room_type_images where room_type_id = :roomTypeId", new MapSqlParameterSource("roomTypeId", roomTypeId));
            int order = 0;
            for (ImageAssetResponse image : images) {
                jdbcTemplate.update("""
                        insert into room_type_images (id, room_type_id, image_asset_id, url, sort_order)
                        values (:id, :roomTypeId, :imageAssetId, :url, :sortOrder)
                        """, snapshotParams("roomTypeId", roomTypeId, image, order++));
            }
        }
        return listRoomTypeImages(roomTypeId);
    }
}
