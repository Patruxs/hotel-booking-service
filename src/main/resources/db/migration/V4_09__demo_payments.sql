insert into payments (id, booking_id, provider, status, amount, currency, merchant_txn_ref, provider_transaction_no, paid_at, created_at, updated_at) values
    ('c5a5b5f2-959c-4890-a35c-ef66e1355152', 'dbf665d3-9ef7-4512-9380-d80cf4e5e0f8', 'VNPAY', 'SUCCEEDED', 500000.00, 'VND', 'TXN_001', 'VNPAY_001', now(), now(), now()),
    ('b5d3f25c-897d-411a-bd91-3c1a3b8d6d67', '41237be8-cbfc-4af6-878c-c14e52f417fe', 'VNPAY', 'PENDING', 750000.00, 'VND', 'TXN_002', null, null, now(), now()),
    ('f643867f-94ad-4e89-a5c9-9477eb37de75', '22168407-e85d-46bb-82ff-90b9665a54f8', 'VNPAY', 'SUCCEEDED', 1200000.00, 'VND', 'TXN_003', 'VNPAY_003', now(), now(), now()),
    ('7b0b8c63-6f45-42c2-b34e-cf543415c898', 'de779bea-96d6-4f5e-b355-25cc382f24d6', 'VNPAY', 'FAILED', 300000.00, 'VND', 'TXN_004', null, null, now(), now()),
    ('21c97a7d-b108-4af6-bb4d-61fbe2dbcfd9', '0675e640-6feb-4bbb-9359-0be8247e037f', 'VNPAY', 'SUCCEEDED', 450000.00, 'VND', 'TXN_005', 'VNPAY_005', now(), now(), now()),
    ('a3f5b721-c42e-4052-a5d6-d3b202419a4e', 'b5405447-4f0d-45ef-8fa2-ecfa7afa930d', 'VNPAY', 'PENDING', 600000.00, 'VND', 'TXN_006', null, null, now(), now()),
    ('3eb487a5-d843-41dc-b924-f7c807b5a1b3', 'd8e7bd90-1436-4a0e-9668-3e6fe18cf7d7', 'VNPAY', 'SUCCEEDED', 900000.00, 'VND', 'TXN_007', 'VNPAY_007', now(), now(), now()),
    ('2b64ab55-08e8-4660-8dbb-f9f31553755b', '3d492def-e058-40d6-93b1-dec256b656f6', 'VNPAY', 'CANCELED', 850000.00, 'VND', 'TXN_008', null, null, now(), now()),
    ('805c8d0a-9d36-455a-bd44-934fa12128b9', 'c2f6c174-25ea-43f6-956d-cbdbd8a1bccf', 'VNPAY', 'SUCCEEDED', 1100000.00, 'VND', 'TXN_009', 'VNPAY_009', now(), now(), now()),
    ('8d752eaf-fbcf-49b0-9831-c4d32a0c4f87', '96a1be22-0835-4c1d-8d6f-73196f3148b8', 'VNPAY', 'SUCCEEDED', 2000000.00, 'VND', 'TXN_010', 'VNPAY_010', now(), now(), now());
