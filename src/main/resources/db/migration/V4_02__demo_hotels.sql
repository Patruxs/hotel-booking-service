INSERT INTO hotels (id, owner_id, name, slug, description, address, city, email, phone, status, star_rating) VALUES
    ('c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', '275bb263-ef8d-4fa0-9e92-042b9558022d', 'Grand Sapphire Resort', 'grand-sapphire-resort', 'A luxurious 5-star resort overlooking the pristine waters of the Pacific Ocean.', '123 Ocean View Drive', 'Da Nang', 'contact@grandsapphire.example.com', '+84 123 456 789', 'ACTIVE', 5.0),
    ('c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'b0291487-a05e-49b8-a096-e82fd93ce031', 'Urban Edge Hotel', 'urban-edge-hotel', 'Modern, chic boutique hotel located in the heart of the bustling city.', '456 City Center Boulevard', 'Ho Chi Minh City', 'info@urbanedge.example.com', '+84 987 654 321', 'ACTIVE', 4.0),
    ('6f3924c0-61c6-4c86-8bdc-b1f468e04468', '6e419812-a72c-4ba5-a4de-5ee56a6e6522', 'Mountain View Lodge', 'mountain-view-lodge', 'Cozy and quiet lodge nestled in the misty mountains.', '789 Pine Ridge', 'Da Lat', 'stay@mountainview.example.com', '+84 456 123 789', 'ACTIVE', 3.5);

INSERT INTO hotel_members (hotel_id, account_id) VALUES
    ('c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', '275bb263-ef8d-4fa0-9e92-042b9558022d'),
    ('c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'b0291487-a05e-49b8-a096-e82fd93ce031'),
    ('6f3924c0-61c6-4c86-8bdc-b1f468e04468', '6e419812-a72c-4ba5-a4de-5ee56a6e6522');
