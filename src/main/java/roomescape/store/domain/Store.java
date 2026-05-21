package roomescape.store.domain;

import roomescape.common.exception.DomainValidationException;

public class Store {
    private final Long id;
    private final String name;

    private Store(Long id, String name){
        validateName(name);
        this.id = id;
        this.name = name;
    }

    public static Store load(Long id, String name) {
        validateId(id);
        return new Store(id, name);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new DomainValidationException("매장 ID는 필수입니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("매장 이름은 필수입니다.");
        }
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }
}
