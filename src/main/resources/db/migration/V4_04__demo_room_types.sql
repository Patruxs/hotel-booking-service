-- Hotel 1 Room Types
INSERT INTO room_types (id, hotel_id, name, description, price_per_night, max_guests, number_of_bedrooms) VALUES
('4235f559-ba9f-4fb0-9b0d-4d393a2b3fd0', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'Standard Room', 'A comfortable standard room.', 2000000.00, 2, 1),
('978ca5d5-b451-49ed-8212-2fb8b2f2ccc6', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'Deluxe Room', 'A spacious deluxe room.', 4000000.00, 3, 1),
('1fd200b2-6325-4116-96d4-22bf4d0aac4d', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'Suite', 'A luxury suite.', 10000000.00, 4, 2);

-- Hotel 2 Room Types
INSERT INTO room_types (id, hotel_id, name, description, price_per_night, max_guests, number_of_bedrooms) VALUES
('2052bf5e-482f-4b07-9f53-304b7e142f26', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'Standard Room', 'A comfortable standard room.', 2400000.00, 2, 1),
('458330d1-659e-4c17-aed9-b064d5a877a2', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'Deluxe Room', 'A spacious deluxe room.', 4400000.00, 3, 1),
('81705de7-e353-44ba-804e-7d36a95ad92c', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'Suite', 'A luxury suite.', 11000000.00, 4, 2);

-- Hotel 3 Room Types
INSERT INTO room_types (id, hotel_id, name, description, price_per_night, max_guests, number_of_bedrooms) VALUES
('765eb31f-1e90-4ce3-bbe4-29236c473c16', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', 'Standard Room', 'A comfortable standard room.', 1800000.00, 2, 1),
('87108db0-282f-4668-8a23-1c1fc00921c0', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', 'Deluxe Room', 'A spacious deluxe room.', 3600000.00, 3, 1),
('ed8323b7-4e9a-416f-8657-c329b5d24e9d', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', 'Suite', 'A luxury suite.', 9000000.00, 4, 2);

-- Room Type Amenities
INSERT INTO room_type_amenities (room_type_id, amenity_id) VALUES
-- Hotel 1 Standard
('4235f559-ba9f-4fb0-9b0d-4d393a2b3fd0', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('4235f559-ba9f-4fb0-9b0d-4d393a2b3fd0', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
-- Hotel 1 Deluxe
('978ca5d5-b451-49ed-8212-2fb8b2f2ccc6', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('978ca5d5-b451-49ed-8212-2fb8b2f2ccc6', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
('978ca5d5-b451-49ed-8212-2fb8b2f2ccc6', 'ad8a7379-0293-49e1-93d5-7a90e9035426'),
-- Hotel 1 Suite
('1fd200b2-6325-4116-96d4-22bf4d0aac4d', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('1fd200b2-6325-4116-96d4-22bf4d0aac4d', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
('1fd200b2-6325-4116-96d4-22bf4d0aac4d', 'ad8a7379-0293-49e1-93d5-7a90e9035426'),
('1fd200b2-6325-4116-96d4-22bf4d0aac4d', '50a9820e-e7d7-41bc-a1dc-9ab138bb3b0e'),

-- Hotel 2 Standard
('2052bf5e-482f-4b07-9f53-304b7e142f26', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('2052bf5e-482f-4b07-9f53-304b7e142f26', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
-- Hotel 2 Deluxe
('458330d1-659e-4c17-aed9-b064d5a877a2', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('458330d1-659e-4c17-aed9-b064d5a877a2', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
('458330d1-659e-4c17-aed9-b064d5a877a2', 'ad8a7379-0293-49e1-93d5-7a90e9035426'),
-- Hotel 2 Suite
('81705de7-e353-44ba-804e-7d36a95ad92c', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('81705de7-e353-44ba-804e-7d36a95ad92c', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
('81705de7-e353-44ba-804e-7d36a95ad92c', 'ad8a7379-0293-49e1-93d5-7a90e9035426'),
('81705de7-e353-44ba-804e-7d36a95ad92c', '50a9820e-e7d7-41bc-a1dc-9ab138bb3b0e'),

-- Hotel 3 Standard
('765eb31f-1e90-4ce3-bbe4-29236c473c16', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('765eb31f-1e90-4ce3-bbe4-29236c473c16', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
-- Hotel 3 Deluxe
('87108db0-282f-4668-8a23-1c1fc00921c0', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('87108db0-282f-4668-8a23-1c1fc00921c0', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
('87108db0-282f-4668-8a23-1c1fc00921c0', 'ad8a7379-0293-49e1-93d5-7a90e9035426'),
-- Hotel 3 Suite
('ed8323b7-4e9a-416f-8657-c329b5d24e9d', '72cf4fbe-3e29-4e64-96cb-674d809a7cec'),
('ed8323b7-4e9a-416f-8657-c329b5d24e9d', '2e384d99-40d1-4c85-b40e-1fa0e70359a9'),
('ed8323b7-4e9a-416f-8657-c329b5d24e9d', 'ad8a7379-0293-49e1-93d5-7a90e9035426'),
('ed8323b7-4e9a-416f-8657-c329b5d24e9d', '50a9820e-e7d7-41bc-a1dc-9ab138bb3b0e');
