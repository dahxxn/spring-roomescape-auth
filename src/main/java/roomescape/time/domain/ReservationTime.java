package roomescape.time.domain;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.common.exception.DomainValidationException;
import roomescape.store.domain.Store;

public class ReservationTime {
    private final Long id;
    private final Store store;
    private final LocalTime startAt;

    private ReservationTime(Long id, Store store, LocalTime startAt) {
        validate(store, startAt);
        this.id = id;
        this.store = store;
        this.startAt = startAt;
    }

    public static ReservationTime create(Store store, LocalTime startAt) {
        return new ReservationTime(null, store, startAt);
    }

    public static ReservationTime load(Long id, Store store, LocalTime startAt) {
        return new ReservationTime(id, store, startAt);
    }

    private static void validate(Store store, LocalTime startAt) {
        validateStore(store);
        validateStartAt(startAt);
    }

    private static void validateStore(Store store) {
        if (store == null) {
            throw new DomainValidationException("매장은 필수입니다.");
        }
    }

    private static void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new DomainValidationException("예약 시작 시간은 필수입니다.");
        }
    }

    public Long id() {
        return id;
    }

    public Store store() {
        return store;
    }

    public LocalTime startAt() {
        return startAt;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ReservationTime that)) {
            return false;
        }

        if (that.id == null || this.id == null) {
            return false;
        }

        return Objects.equals(this.id, that.id);
    }
}
