-- Create demo accounts
INSERT INTO accounts (id, email, password_hash, first_name, last_name, email_verified, auth_provider) VALUES
-- 1 System Admin
('880fe780-ab42-419a-9092-4547f962e008', 'admin@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'System', 'Admin', true, 'LOCAL'),

-- 3 Hotel Owners
('275bb263-ef8d-4fa0-9e92-042b9558022d', 'owner1@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Hotel', 'Owner 1', true, 'LOCAL'),
('b0291487-a05e-49b8-a096-e82fd93ce031', 'owner2@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Hotel', 'Owner 2', true, 'LOCAL'),
('6e419812-a72c-4ba5-a4de-5ee56a6e6522', 'owner3@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Hotel', 'Owner 3', true, 'LOCAL'),

-- 10 Customers
('f560274e-6bf5-4693-82c6-170600654be3', 'customer1@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 1', true, 'LOCAL'),
('79a74708-3c0c-452f-b1f1-e3ea55a3a30d', 'customer2@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 2', true, 'LOCAL'),
('4468af85-28a8-4093-ad5e-3b721f2c048b', 'customer3@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 3', true, 'LOCAL'),
('b89efffd-fb0e-42b2-b0c7-2e42cde498f2', 'customer4@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 4', true, 'LOCAL'),
('b1e6202e-cd9f-4c29-8220-23333de26be2', 'customer5@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 5', true, 'LOCAL'),
('1aaa7561-cbe3-46b3-a7e9-35cbae392e60', 'customer6@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 6', true, 'LOCAL'),
('76277114-361b-4acb-9b38-37ccb7f89b09', 'customer7@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 7', true, 'LOCAL'),
('01274fe3-2471-48e4-98c0-c84172dc0cb2', 'customer8@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 8', true, 'LOCAL'),
('fd618bf4-670b-4b7f-9e77-c9f765e7ce75', 'customer9@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 9', true, 'LOCAL'),
('fdeb5dbf-0eb8-48d6-916d-6db412ca7d51', 'customer10@demo.local', '$2a$12$tPcwQ/wH.5w9K2o3h8a.X.1a7wHqD6l2y2m5A.2h6e5r7d7z8b3zG', 'Demo', 'Customer 10', true, 'LOCAL');

-- Assign Roles (Roles are already seeded in V1, using subqueries by name to get their IDs)
INSERT INTO account_roles (account_id, role_id) VALUES
-- Admin
('880fe780-ab42-419a-9092-4547f962e008', (SELECT id FROM roles WHERE name = 'ADMIN')),

-- Hotel Owners
('275bb263-ef8d-4fa0-9e92-042b9558022d', (SELECT id FROM roles WHERE name = 'OWNER')),
('b0291487-a05e-49b8-a096-e82fd93ce031', (SELECT id FROM roles WHERE name = 'OWNER')),
('6e419812-a72c-4ba5-a4de-5ee56a6e6522', (SELECT id FROM roles WHERE name = 'OWNER')),

-- Customers
('f560274e-6bf5-4693-82c6-170600654be3', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('79a74708-3c0c-452f-b1f1-e3ea55a3a30d', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('4468af85-28a8-4093-ad5e-3b721f2c048b', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('b89efffd-fb0e-42b2-b0c7-2e42cde498f2', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('b1e6202e-cd9f-4c29-8220-23333de26be2', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('1aaa7561-cbe3-46b3-a7e9-35cbae392e60', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('76277114-361b-4acb-9b38-37ccb7f89b09', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('01274fe3-2471-48e4-98c0-c84172dc0cb2', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('fd618bf4-670b-4b7f-9e77-c9f765e7ce75', (SELECT id FROM roles WHERE name = 'CUSTOMER')),
('fdeb5dbf-0eb8-48d6-916d-6db412ca7d51', (SELECT id FROM roles WHERE name = 'CUSTOMER'));
