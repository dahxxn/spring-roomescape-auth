package roomescape.theme.domain;

import roomescape.common.exception.DomainValidationException;
import roomescape.store.domain.Store;

public class Theme {
    private final Long id;
    private final Store store;
    private final String name;
    private final String description;
    private final String thumbnailUrl;
    private final boolean isActive;

    private Theme(Long id, Store store, String name, String description, String thumbnailUrl, boolean isActive) {
        validate(store, name, description, thumbnailUrl);
        this.id = id;
        this.store = store;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.isActive = isActive;
    }

    public static Theme create(Store store, String name, String description, String thumbnailUrl) {
        validate(store, name, description, thumbnailUrl);
        return new Theme(null, store, name, description, thumbnailUrl, false);
    }

    public static Theme load(Long id, Store store, String name, String description, String thumbnailUrl,
                             boolean isActive) {
        validate(store, name, description, thumbnailUrl);
        return new Theme(id, store, name, description, thumbnailUrl, isActive);
    }

    private static void validate(Store store, String name, String description, String thumbnailUrl) {
        validateStore(store);
        validateName(name);
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
    }

    private static void validateStore(Store store) {
        if (store == null) {
            throw new DomainValidationException("매장은 필수입니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("테마 이름은 필수입니다.");
        }
    }

    private static void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new DomainValidationException("테마 썸네일 URL은 필수입니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new DomainValidationException("테마 설명은 필수입니다.");
        }
    }

    public Long id() {
        return id;
    }

    public Store store() {
        return store;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String thumbnailUrl() {
        return thumbnailUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public Theme changeStatus(boolean isActive) {
        return new Theme(id, store, name, description, thumbnailUrl, isActive);
    }
}
