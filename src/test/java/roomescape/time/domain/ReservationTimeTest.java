package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainValidationException;
import roomescape.store.domain.Store;

class ReservationTimeTest {
    private Store store;
    private ReservationTime reservationTime;

    @BeforeEach
    void setUp() {
        store = Store.load(1L, "강남점");
        reservationTime = ReservationTime.load(1L, store, LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("예약 시간 id를 가져온다.")
    void getId() {
        Long expected = 1L;

        Long actual = reservationTime.id();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("예약 시작 시간을 가져온다.")
    void getStartAt() {
        LocalTime expected = LocalTime.of(10, 0);

        LocalTime actual = reservationTime.startAt();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("두 예약 객체의 동등성을 비교한다.")
    void equals() {
        ReservationTime otherReservationTime = ReservationTime.load(1L, store, LocalTime.of(20, 0));

        assertEquals(reservationTime, otherReservationTime);
    }

    @Test
    @DisplayName("아직 DB에 추가되지 않은 예약끼리와는 동등하지 않다.")
    void equals_null_id() {
        ReservationTime reservationTime1 = ReservationTime.create(store, LocalTime.of(10, 0));
        ReservationTime reservationTime2 = ReservationTime.create(store, LocalTime.of(10, 0));

        assertNotEquals(reservationTime1, reservationTime2);
    }

    @Test
    @DisplayName("예약 시작 시간이 유효하지 않은 경우 예외가 발생한다.")
    void validate_startAt() {
        assertThatThrownBy(() -> ReservationTime.load(1L, store, null))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("매장이 유효하지 않은 경우 예외가 발생한다.")
    void validate_store() {
        assertThatThrownBy(() -> ReservationTime.load(1L, null, LocalTime.of(10, 0)))
                .isInstanceOf(DomainValidationException.class);
    }
}
