INSERT INTO bookings (
    id, account_id, hotel_id, booking_reference, status, check_in, check_out, 
    guest_name, guest_email, guest_phone, subtotal_amount, total_amount, 
    commission_package_code, commission_rate, commission_amount, 
    cancelled_at, completed_at, no_show_at
) VALUES 
-- B1: PENDING
('dbf665d3-9ef7-4512-9380-d80cf4e5e0f8', 'f560274e-6bf5-4693-82c6-170600654be3', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'BKG-0001', 'PENDING', '2026-07-20', '2026-07-22', 'John Doe', 'john@example.com', '0123456789', 1000.00, 1000.00, 'STANDARD', 0.1000, 100.00, NULL, NULL, NULL),
-- B2: CONFIRMED
('41237be8-cbfc-4af6-878c-c14e52f417fe', '79a74708-3c0c-452f-b1f1-e3ea55a3a30d', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'BKG-0002', 'CONFIRMED', '2026-07-25', '2026-07-27', 'Jane Smith', 'jane@example.com', '0123456789', 1200.00, 1200.00, 'STANDARD', 0.1000, 120.00, NULL, NULL, NULL),
-- B3: CHECKED_IN
('22168407-e85d-46bb-82ff-90b9665a54f8', '4468af85-28a8-4093-ad5e-3b721f2c048b', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', 'BKG-0003', 'CHECKED_IN', '2026-07-09', '2026-07-12', 'Michael Brown', 'michael@example.com', '0123456789', 1500.00, 1500.00, 'STANDARD', 0.1000, 150.00, NULL, NULL, NULL),
-- B4: COMPLETED
('de779bea-96d6-4f5e-b355-25cc382f24d6', 'b89efffd-fb0e-42b2-b0c7-2e42cde498f2', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'BKG-0004', 'COMPLETED', '2026-06-01', '2026-06-05', 'Emily Davis', 'emily@example.com', '0123456789', 2000.00, 2000.00, 'PREMIUM', 0.1500, 300.00, NULL, '2026-06-05 10:00:00+00', NULL),
-- B5: CANCELLED
('0675e640-6feb-4bbb-9359-0be8247e037f', 'b1e6202e-cd9f-4c29-8220-23333de26be2', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'BKG-0005', 'CANCELLED', '2026-06-10', '2026-06-12', 'William Wilson', 'william@example.com', '0123456789', 800.00, 800.00, 'STANDARD', 0.1000, 80.00, '2026-06-08 12:00:00+00', NULL, NULL),
-- B6: NO_SHOW
('b5405447-4f0d-45ef-8fa2-ecfa7afa930d', '1aaa7561-cbe3-46b3-a7e9-35cbae392e60', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', 'BKG-0006', 'NO_SHOW', '2026-06-15', '2026-06-17', 'Olivia Taylor', 'olivia@example.com', '0123456789', 900.00, 900.00, 'STANDARD', 0.1000, 90.00, NULL, NULL, '2026-06-16 12:00:00+00'),
-- B7: CONFIRMED
('d8e7bd90-1436-4a0e-9668-3e6fe18cf7d7', '76277114-361b-4acb-9b38-37ccb7f89b09', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'BKG-0007', 'CONFIRMED', '2026-08-01', '2026-08-05', 'James Anderson', 'james@example.com', '0123456789', 2500.00, 2500.00, 'PREMIUM', 0.1500, 375.00, NULL, NULL, NULL),
-- B8: COMPLETED
('3d492def-e058-40d6-93b1-dec256b656f6', '01274fe3-2471-48e4-98c0-c84172dc0cb2', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'BKG-0008', 'COMPLETED', '2026-05-01', '2026-05-03', 'Sophia Thomas', 'sophia@example.com', '0123456789', 1100.00, 1100.00, 'STANDARD', 0.1000, 110.00, NULL, '2026-05-03 09:30:00+00', NULL),
-- B9: CHECKED_IN
('c2f6c174-25ea-43f6-956d-cbdbd8a1bccf', 'fd618bf4-670b-4b7f-9e77-c9f765e7ce75', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', 'BKG-0009', 'CHECKED_IN', '2026-07-10', '2026-07-15', 'Benjamin Jackson', 'benjamin@example.com', '0123456789', 3000.00, 3000.00, 'PREMIUM', 0.1500, 450.00, NULL, NULL, NULL),
-- B10: COMPLETED
('96a1be22-0835-4c1d-8d6f-73196f3148b8', 'fdeb5dbf-0eb8-48d6-916d-6db412ca7d51', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'BKG-0010', 'COMPLETED', '2026-04-10', '2026-04-12', 'Isabella White', 'isabella@example.com', '0123456789', 1300.00, 1300.00, 'STANDARD', 0.1000, 130.00, NULL, '2026-04-12 11:00:00+00', NULL);

INSERT INTO booking_items (
    id, booking_id, room_type_id, room_type_name, quantity, unit_price, max_guests, line_total
) VALUES 
(gen_random_uuid(), 'dbf665d3-9ef7-4512-9380-d80cf4e5e0f8', '4235f559-ba9f-4fb0-9b0d-4d393a2b3fd0', 'Standard Room', 1, 500.00, 2, 1000.00),
(gen_random_uuid(), '41237be8-cbfc-4af6-878c-c14e52f417fe', '978ca5d5-b451-49ed-8212-2fb8b2f2ccc6', 'Deluxe Room', 1, 600.00, 2, 1200.00),
(gen_random_uuid(), '22168407-e85d-46bb-82ff-90b9665a54f8', '1fd200b2-6325-4116-96d4-22bf4d0aac4d', 'Suite', 1, 500.00, 2, 1500.00),
(gen_random_uuid(), 'de779bea-96d6-4f5e-b355-25cc382f24d6', '4235f559-ba9f-4fb0-9b0d-4d393a2b3fd0', 'Standard Room', 1, 500.00, 2, 2000.00),
(gen_random_uuid(), '0675e640-6feb-4bbb-9359-0be8247e037f', '978ca5d5-b451-49ed-8212-2fb8b2f2ccc6', 'Deluxe Room', 1, 400.00, 2, 800.00),
(gen_random_uuid(), 'b5405447-4f0d-45ef-8fa2-ecfa7afa930d', '1fd200b2-6325-4116-96d4-22bf4d0aac4d', 'Suite', 1, 450.00, 2, 900.00),
(gen_random_uuid(), 'd8e7bd90-1436-4a0e-9668-3e6fe18cf7d7', '4235f559-ba9f-4fb0-9b0d-4d393a2b3fd0', 'Standard Room', 1, 625.00, 2, 2500.00),
(gen_random_uuid(), '3d492def-e058-40d6-93b1-dec256b656f6', '978ca5d5-b451-49ed-8212-2fb8b2f2ccc6', 'Deluxe Room', 1, 550.00, 2, 1100.00),
(gen_random_uuid(), 'c2f6c174-25ea-43f6-956d-cbdbd8a1bccf', '1fd200b2-6325-4116-96d4-22bf4d0aac4d', 'Suite', 1, 600.00, 2, 3000.00),
(gen_random_uuid(), '96a1be22-0835-4c1d-8d6f-73196f3148b8', '4235f559-ba9f-4fb0-9b0d-4d393a2b3fd0', 'Standard Room', 1, 650.00, 2, 1300.00);

DO $$
DECLARE
    b3_checkin uuid := gen_random_uuid();
    b4_checkin uuid := gen_random_uuid();
    b8_checkin uuid := gen_random_uuid();
    b9_checkin uuid := gen_random_uuid();
    b10_checkin uuid := gen_random_uuid();
BEGIN
    INSERT INTO check_ins (id, booking_id, checked_in_at, checked_out_at) VALUES 
    (b3_checkin, '22168407-e85d-46bb-82ff-90b9665a54f8', '2026-07-09 14:00:00+00', NULL),
    (b4_checkin, 'de779bea-96d6-4f5e-b355-25cc382f24d6', '2026-06-01 15:00:00+00', '2026-06-05 10:00:00+00'),
    (b8_checkin, '3d492def-e058-40d6-93b1-dec256b656f6', '2026-05-01 14:30:00+00', '2026-05-03 09:30:00+00'),
    (b9_checkin, 'c2f6c174-25ea-43f6-956d-cbdbd8a1bccf', '2026-07-10 13:00:00+00', NULL),
    (b10_checkin, '96a1be22-0835-4c1d-8d6f-73196f3148b8', '2026-04-10 14:15:00+00', '2026-04-12 11:00:00+00');

    INSERT INTO booking_guests (id, check_in_id, full_name, is_primary, guest_order) VALUES
    (gen_random_uuid(), b3_checkin, 'Michael Brown', true, 0),
    (gen_random_uuid(), b4_checkin, 'Emily Davis', true, 0),
    (gen_random_uuid(), b8_checkin, 'Sophia Thomas', true, 0),
    (gen_random_uuid(), b9_checkin, 'Benjamin Jackson', true, 0),
    (gen_random_uuid(), b10_checkin, 'Isabella White', true, 0);
END $$;
