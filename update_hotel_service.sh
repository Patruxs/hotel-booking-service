#!/bin/bash
sed -i 's/import java.util.Map;/import java.util.Map;\nimport java.util.concurrent.CompletableFuture;\nimport java.util.concurrent.Executor;\nimport java.util.concurrent.Executors;/g' src/main/java/org/example/hotelbookingservice/services/impl/HotelServiceImpl.java

cat << 'REPLACE_EOF' > /tmp/hotel_add_patch.diff
<<<<<<< SEARCH
        if (imageFile != null && !imageFile.isEmpty()) {
            List<Image> imagesToSave = new ArrayList<>();
            for (MultipartFile file : imageFile ) {
                String imageUrl = fileStorageService.uploadFile(file);
                Image image = new Image();
                image.setPath(imageUrl);
                image.setHotel(savedHotel);
                imagesToSave.add(image);
            }
            imageRepository.saveAll(imagesToSave);
            savedHotel.getImages().addAll(imagesToSave);
        }
=======
        if (imageFile != null && !imageFile.isEmpty()) {
            try (Executor executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Image>> futures = imageFile.stream()
                        .map(file -> CompletableFuture.supplyAsync(() -> {
                            String imageUrl = fileStorageService.uploadFile(file);
                            Image image = new Image();
                            image.setPath(imageUrl);
                            image.setHotel(savedHotel);
                            return image;
                        }, executor).exceptionally(ex -> {
                            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                        }))
                        .toList();

                List<Image> imagesToSave = futures.stream()
                        .map(CompletableFuture::join)
                        .toList();

                imageRepository.saveAll(imagesToSave);
                savedHotel.getImages().addAll(imagesToSave);
            }
        }
>>>>>>> REPLACE
REPLACE_EOF

cat << 'REPLACE_EOF' > /tmp/hotel_update_patch.diff
<<<<<<< SEARCH
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<Image> imagesToSave = new ArrayList<>();
            for (MultipartFile file : imageFiles ) {
                String imageUrl = fileStorageService.uploadFile(file);
                Image image = new Image();
                image.setPath(imageUrl);
                image.setHotel(existingHotel);
                imagesToSave.add(image);
            }
            imageRepository.saveAll(imagesToSave);
            existingHotel.getImages().clear();
            existingHotel.getImages().addAll(imagesToSave);
        }
=======
        if (imageFiles != null && !imageFiles.isEmpty()) {
            try (Executor executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Image>> futures = imageFiles.stream()
                        .map(file -> CompletableFuture.supplyAsync(() -> {
                            String imageUrl = fileStorageService.uploadFile(file);
                            Image image = new Image();
                            image.setPath(imageUrl);
                            image.setHotel(existingHotel);
                            return image;
                        }, executor).exceptionally(ex -> {
                            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                        }))
                        .toList();

                List<Image> imagesToSave = futures.stream()
                        .map(CompletableFuture::join)
                        .toList();

                imageRepository.saveAll(imagesToSave);
                existingHotel.getImages().clear();
                existingHotel.getImages().addAll(imagesToSave);
            }
        }
>>>>>>> REPLACE
REPLACE_EOF
