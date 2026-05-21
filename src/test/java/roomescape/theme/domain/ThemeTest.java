package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainValidationException;
import roomescape.store.domain.Store;

class ThemeTest {
    private final Store store = Store.load(1L, "강남점");
    private final String name = "공포";
    private final String description = "테마 설명";
    private final String emptyThumbnailUrl = "";

    @Test
    @DisplayName("테마 이름이 null이면 예외가 발생한다.")
    void create_null_name() {
        assertThatThrownBy(() -> Theme.create(store, null, description, emptyThumbnailUrl))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("테마 이름이 비어있으면 예외가 발생한다.")
    void create_empty_name() {
        assertThatThrownBy(() -> Theme.create(store, "", description, emptyThumbnailUrl))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("테마 설명이 null이면 예외가 발생한다.")
    void create_null_description() {
        assertThatThrownBy(() -> Theme.create(store, name, null, emptyThumbnailUrl))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("테마 설명이 비어있으면 예외가 발생한다.")
    void create_empty_description() {
        assertThatThrownBy(() -> Theme.create(store, name, "", emptyThumbnailUrl))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("테마 썸네일 URL이 비어있으면 예외가 발생한다.")
    void create_empty_thumbnail() {
        assertThatThrownBy(() -> Theme.create(store, name, description, emptyThumbnailUrl))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("매장이 null이면 예외가 발생한다.")
    void create_null_store() {
        assertThatThrownBy(() -> Theme.create(null, name, description, "테마 썸네일"))
                .isInstanceOf(DomainValidationException.class);
    }
}
