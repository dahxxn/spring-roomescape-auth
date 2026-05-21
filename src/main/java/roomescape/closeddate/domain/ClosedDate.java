package roomescape.closeddate.domain;

import java.time.LocalDate;
import roomescape.common.exception.DomainValidationException;
import roomescape.store.domain.Store;

public class ClosedDate {
    private final Long id;
    private final Store store;
    private final LocalDate date;

    private ClosedDate(Long id, Store store, LocalDate date) {
        validate(store, date);
        this.id = id;
        this.store = store;
        this.date = date;
    }

    public static ClosedDate create(Store store, LocalDate date) {
        validatePast(date);
        return new ClosedDate(null, store, date);
    }

    public static ClosedDate load(Long id, Store store, LocalDate date) {
        return new ClosedDate(id, store, date);
    }

    private static void validate(Store store, LocalDate date) {
        validateStore(store);
        validateDate(date);
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new DomainValidationException("휴일 날짜는 필수입니다.");
        }
    }

    private static void validateStore(Store store) {
        if (store == null) {
            throw new DomainValidationException("매장은 필수입니다.");
        }
    }

    private static void validatePast(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("과거 날짜는 등록할 수 없습니다.");
        }
    }

    public Long id() {
        return id;
    }

    public Store store() {
        return store;
    }

    public LocalDate date() {
        return date;
    }

}
