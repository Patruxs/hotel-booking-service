insert into reviews (id, booking_id, hotel_id, account_id, rating, comment, visible, created_at, updated_at) values
    ('f793462f-f4b3-4e4d-9b0f-d4570ace2c65', 'dbf665d3-9ef7-4512-9380-d80cf4e5e0f8', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'f560274e-6bf5-4693-82c6-170600654be3', 5.0, 'Excellent stay! Highly recommended.', true, now(), now()),
    ('650e7a0b-d9f0-41f2-8f7d-ba2dad753799', '41237be8-cbfc-4af6-878c-c14e52f417fe', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', '79a74708-3c0c-452f-b1f1-e3ea55a3a30d', 4.0, 'Very good experience, friendly staff.', true, now(), now()),
    ('10ae3612-8111-4fbc-a914-76462431edf0', '22168407-e85d-46bb-82ff-90b9665a54f8', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', '4468af85-28a8-4093-ad5e-3b721f2c048b', 3.5, 'Average stay. Room could be cleaner.', true, now(), now()),
    ('ad4072a0-36f4-400e-8dc1-b20f662a792a', 'de779bea-96d6-4f5e-b355-25cc382f24d6', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'b89efffd-fb0e-42b2-b0c7-2e42cde498f2', 5.0, 'Perfect location and great amenities!', true, now(), now()),
    ('8b619f1c-51ce-43d5-bc07-bd6f84f00ba6', '0675e640-6feb-4bbb-9359-0be8247e037f', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'b1e6202e-cd9f-4c29-8220-23333de26be2', 4.5, 'Loved the breakfast and the view.', true, now(), now()),
    ('731d54df-0f6e-44ad-b59a-f420c393d758', 'b5405447-4f0d-45ef-8fa2-ecfa7afa930d', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', '1aaa7561-cbe3-46b3-a7e9-35cbae392e60', 3.0, 'It was okay. A bit noisy at night.', true, now(), now()),
    ('712a6a3c-38e3-4b07-87db-c4b4a12e33bb', 'd8e7bd90-1436-4a0e-9668-3e6fe18cf7d7', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', '76277114-361b-4acb-9b38-37ccb7f89b09', 4.0, 'Comfortable bed and quick room service.', true, now(), now()),
    ('05d0f33a-17d3-4963-89cc-b322035867d5', '3d492def-e058-40d6-93b1-dec256b656f6', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', '01274fe3-2471-48e4-98c0-c84172dc0cb2', 5.0, 'A fantastic weekend getaway.', true, now(), now()),
    ('97456d46-4616-4e78-8751-e2339fb87b97', 'c2f6c174-25ea-43f6-956d-cbdbd8a1bccf', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', 'fd618bf4-670b-4b7f-9e77-c9f765e7ce75', 4.5, 'Really enjoyed our stay. We will be back.', true, now(), now()),
    ('02437bc4-ea28-4945-9ea3-5d5478c84a05', '96a1be22-0835-4c1d-8d6f-73196f3148b8', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', 'fdeb5dbf-0eb8-48d6-916d-6db412ca7d51', 3.5, 'Good value for the price, but needs updates.', true, now(), now());

-- The remaining 5 review IDs from demo-context.json are commented out below.
-- The reviews table has a unique, non-null constraint on booking_id.
-- Since demo-context.json only provides 10 bookings, we can only safely create 10 reviews.
-- 
-- ('bafc07ca-8e79-473f-8367-5f4216c2519f', '<missing_booking_id>', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'f560274e-6bf5-4693-82c6-170600654be3', 4.0, 'Nice pool.', true, now(), now()),
-- ('45771f10-ed1f-4329-94ac-93e07736c614', '<missing_booking_id>', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', '79a74708-3c0c-452f-b1f1-e3ea55a3a30d', 5.0, 'Great service!', true, now(), now()),
-- ('05bfc049-f27e-4f22-a8c3-a860d3fae090', '<missing_booking_id>', 'c0a8ae4a-cc3e-4af0-b336-c9bebc784f32', '4468af85-28a8-4093-ad5e-3b721f2c048b', 3.0, 'Average.', true, now(), now()),
-- ('27951ad2-60e8-45a8-bcf3-ea32e01c2b85', '<missing_booking_id>', 'c1b392be-30e5-4c11-ac3c-c552a0974d8b', 'b89efffd-fb0e-42b2-b0c7-2e42cde498f2', 4.5, 'Loved it.', true, now(), now()),
-- ('20fed6e3-e08e-4db3-8058-5708111250d4', '<missing_booking_id>', '6f3924c0-61c6-4c86-8bdc-b1f468e04468', 'b1e6202e-cd9f-4c29-8220-23333de26be2', 4.0, 'Good location.', true, now(), now());
