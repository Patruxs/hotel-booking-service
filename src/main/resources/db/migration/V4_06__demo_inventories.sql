INSERT INTO inventories (
    id,
    hotel_id,
    room_type_id,
    stay_date,
    total_rooms,
    available_rooms
)
SELECT 
    gen_random_uuid(),
    rt.hotel_id,
    rt.id,
    CURRENT_DATE + d.day,
    5,
    5
FROM room_types rt
CROSS JOIN generate_series(0, 30) AS d(day)
WHERE rt.id IN (
    '4235f559-ba9f-4fb0-9b0d-4d393a2b3fd0',
    '978ca5d5-b451-49ed-8212-2fb8b2f2ccc6',
    '1fd200b2-6325-4116-96d4-22bf4d0aac4d',
    '2052bf5e-482f-4b07-9f53-304b7e142f26',
    '458330d1-659e-4c17-aed9-b064d5a877a2',
    '81705de7-e353-44ba-804e-7d36a95ad92c',
    '765eb31f-1e90-4ce3-bbe4-29236c473c16',
    '87108db0-282f-4668-8a23-1c1fc00921c0',
    'ed8323b7-4e9a-416f-8657-c329b5d24e9d'
);
