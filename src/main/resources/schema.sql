CREATE TABLE IF NOT EXISTS member
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    login_id VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS store
(
    id      BIGINT        NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS store_admin
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT uk_store_admin_member UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT uk_reservation_time_store_start_at UNIQUE (store_id, start_at)
);

CREATE TABLE IF NOT EXISTS closed_date
(
    id   BIGINT NOT NULL AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    date DATE   NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT uk_closed_date_store_date UNIQUE (store_id, date)
);

CREATE TABLE IF NOT EXISTS theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    store_id      BIGINT       NOT NULL,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url TEXT NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT uk_theme_store_name UNIQUE (store_id, name)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    member_id BIGINT       NOT NULL,
    store_id BIGINT       NOT NULL,
    date     DATE         NOT NULL,
    start_at TIME         NOT NULL,
    theme_id BIGINT       NOT NULL,
    status   ENUM('RESERVED', 'CANCELED') NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (member_id) REFERENCES member (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT uk_reservation_date_time_theme UNIQUE (store_id, date, theme_id, start_at)
);
