SET REFERENTIAL_INTEGRITY FALSE;

TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE closed_date RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE store_admin RESTART IDENTITY;
TRUNCATE TABLE store RESTART IDENTITY;
TRUNCATE TABLE member RESTART IDENTITY;

SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO member (name, login_id, password, role)
VALUES ('테스트유저', 'user01', 'user1234', 'USER');

INSERT INTO store (name)
VALUES ('강남점');

INSERT INTO reservation_time (store_id, start_at) VALUES (1, '10:00');
INSERT INTO reservation_time (store_id, start_at) VALUES (1, '12:00');
INSERT INTO reservation_time (store_id, start_at) VALUES (1, '14:00');

INSERT INTO theme (store_id, name, description, thumbnail_url, is_active)
VALUES (1, '공포', '공포 테마 설명', 'https://horror.jpg', true);

INSERT INTO theme (store_id, name, description, thumbnail_url, is_active)
VALUES (1, '판타지', '판타지 테마 설명', 'https://fantasy.jpg', true);

INSERT INTO theme (store_id, name, description, thumbnail_url, is_active)
VALUES (1, '미스터리', '미스터리 테마 설명', 'https://mystery.jpg', true);

INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -1, CURRENT_DATE), '10:00', 1, 'RESERVED');
INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -2, CURRENT_DATE), '10:00', 1, 'RESERVED');
INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -3, CURRENT_DATE), '10:00', 1, 'RESERVED');
INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -4, CURRENT_DATE), '10:00', 1, 'RESERVED');
INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -5, CURRENT_DATE), '10:00', 1, 'RESERVED');

INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -1, CURRENT_DATE), '12:00', 2, 'RESERVED');
INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -2, CURRENT_DATE), '12:00', 2, 'RESERVED');
INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -3, CURRENT_DATE), '12:00', 2, 'RESERVED');

INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
VALUES (1, 1, DATEADD('DAY', -1, CURRENT_DATE), '14:00', 3, 'RESERVED');
