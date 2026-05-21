MERGE INTO member (name, login_id, password, role) KEY (login_id)
    VALUES ('강남점 관리자', 'admin1', 'admin1234', 'MANAGER');

MERGE INTO member (name, login_id, password, role) KEY (login_id)
    VALUES ('잠실점 관리자', 'admin2', 'admin1234', 'MANAGER');

MERGE INTO member (name, login_id, password) KEY (login_id)
    VALUES ('테스트유저', 'user01', 'user1234');

MERGE INTO store (name) KEY (name)
    VALUES ('강남점');

MERGE INTO store (name) KEY (name)
    VALUES ('잠실점');

MERGE INTO store_admin (member_id, store_id) KEY (member_id, store_id)
    VALUES (
    (SELECT id FROM member WHERE login_id = 'admin1'),
    (SELECT id FROM store WHERE name = '강남점')
    );

MERGE INTO store_admin (member_id, store_id) KEY (member_id, store_id)
    VALUES (
    (SELECT id FROM member WHERE login_id = 'admin2'),
    (SELECT id FROM store WHERE name = '잠실점')
    );

MERGE INTO reservation_time (store_id, start_at) KEY (store_id, start_at)
    VALUES ((SELECT id FROM store WHERE name = '강남점'), '11:00:00');

MERGE INTO reservation_time (store_id, start_at) KEY (store_id, start_at)
    VALUES ((SELECT id FROM store WHERE name = '강남점'), '12:00:00');

MERGE INTO reservation_time (store_id, start_at) KEY (store_id, start_at)
    VALUES ((SELECT id FROM store WHERE name = '잠실점'), '11:00:00');

MERGE INTO reservation_time (store_id, start_at) KEY (store_id, start_at)
    VALUES ((SELECT id FROM store WHERE name = '잠실점'), '12:00:00');

MERGE INTO theme (store_id, name, description, thumbnail_url, is_active) KEY (store_id, name)
    VALUES (
    (SELECT id FROM store WHERE name = '강남점'),
    '잠겨버린 연구실',
    '제한 시간 안에 단서를 찾아 연구실을 탈출해야 합니다.',
    'https://images.unsplash.com/photo-1518005020951-eccb494ad742',
    true
    );

MERGE INTO theme (store_id, name, description, thumbnail_url, is_active) KEY (store_id, name)
    VALUES (
    (SELECT id FROM store WHERE name = '강남점'),
    '사라진 탐정',
    '실종된 탐정의 흔적을 따라 사건의 진실을 밝혀내세요.',
    'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee',
    true
    );

MERGE INTO theme (store_id, name, description, thumbnail_url, is_active) KEY (store_id, name)
    VALUES (
    (SELECT id FROM store WHERE name = '잠실점'),
    '고대 유적의 비밀',
    '고대 유적에 숨겨진 암호를 풀고 보물을 찾아야 합니다.',
    'https://images.unsplash.com/photo-1506744038136-46273834b3fb',
    true
    );

MERGE INTO theme (store_id, name, description, thumbnail_url, is_active) KEY (store_id, name)
    VALUES (
    (SELECT id FROM store WHERE name = '잠실점'),
    '유령 호텔',
    '폐쇄된 호텔에서 벌어진 미스터리한 사건을 해결하세요.',
    'https://images.unsplash.com/photo-1566073771259-6a8506099945',
    true
    );
INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES
    (
        (SELECT id FROM member WHERE login_id = 'user01'),
        (SELECT id FROM store WHERE name = '강남점'),
        DATEADD('DAY', -1, CURRENT_DATE),
        '11:00:00',
        (SELECT id FROM theme WHERE name = '잠겨버린 연구실' AND store_id = (SELECT id FROM store WHERE name = '강남점')),
        'RESERVED'
    ),
    (
        (SELECT id FROM member WHERE login_id = 'user01'),
        (SELECT id FROM store WHERE name = '강남점'),
        DATEADD('DAY', -1, CURRENT_DATE),
        '12:00:00',
        (SELECT id FROM theme WHERE name = '사라진 탐정' AND store_id = (SELECT id FROM store WHERE name = '강남점')),
        'RESERVED'
    ),
    (
        (SELECT id FROM member WHERE login_id = 'user01'),
        (SELECT id FROM store WHERE name = '잠실점'),
        DATEADD('DAY', -1, CURRENT_DATE),
        '11:00:00',
        (SELECT id FROM theme WHERE name = '고대 유적의 비밀' AND store_id = (SELECT id FROM store WHERE name = '잠실점')),
        'RESERVED'
    ),
    (
        (SELECT id FROM member WHERE login_id = 'user01'),
        (SELECT id FROM store WHERE name = '잠실점'),
        DATEADD('DAY', -1, CURRENT_DATE),
        '12:00:00',
        (SELECT id FROM theme WHERE name = '유령 호텔' AND store_id = (SELECT id FROM store WHERE name = '잠실점')),
        'RESERVED'
    );
