package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainValidationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;

class ReservationTest {
    private final LocalDate date = LocalDate.now().plusMonths(1);
    private final LocalTime startAt = LocalTime.of(15, 40);

    private final Member member = Member.load(1L, "한다", "handa", "password", MemberRole.USER);
    private final Store store = Store.load(1L, "강남점");
    private final Theme theme = Theme.load(1L, store, "테마", "설명", "썸네일", true);
    private final Reservation reservation = Reservation.load(1L, member, store, date, startAt, theme, RESERVED);

    @Test
    @DisplayName("예약 id를 가져온다.")
    void getId() {
        Long expected = 1L;

        Long actual = reservation.id();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("예약자를 가져온다.")
    void getMember() {
        assertThat(reservation.member())
                .usingRecursiveComparison()
                .isEqualTo(member);
    }

    @Test
    @DisplayName("예약 매장을 가져온다.")
    void getStore() {
        assertThat(reservation.store())
                .usingRecursiveComparison()
                .isEqualTo(store);
    }

    @Test
    @DisplayName("예약날짜를 가져온다.")
    void getDate() {
        LocalDate actual = reservation.date();

        assertEquals(date, actual);
    }

    @Test
    @DisplayName("예약시간을 가져온다.")
    void getTime() {
        LocalTime actual = reservation.time();

        assertEquals(startAt, actual);
    }

    @Test
    @DisplayName("두 예약 객체의 동등성을 비교한다.")
    void equals() {
        Reservation otherReservation = Reservation.load(1L, member, store, date, startAt, theme, RESERVED);

        assertThat(reservation)
                .usingRecursiveComparison()
                .isEqualTo(otherReservation);
    }

    @Test
    @DisplayName("아직 DB에 추가되지 않은 예약은 id가 없다.")
    void unpersist_reservation_null_id() {
        Reservation unpersistReservation = Reservation.create(member, store, date, startAt, theme);

        assertThat(unpersistReservation.id())
                .isNull();
    }

    @Test
    @DisplayName("과거 날짜로 예약 생성 시 예외 발생한다.")
    void create_before_now() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> Reservation.create(member, store, pastDate, startAt, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약자가 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateMember() {
        Member nullMember = null;

        assertThatThrownBy(() -> Reservation.create(nullMember, store, date, startAt, theme))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("테마가 예약 매장에 속하지 않으면 예외가 발생한다.")
    void validateThemeStore() {
        Store otherStore = Store.load(2L, "잠실점");
        Theme otherStoreTheme = Theme.load(2L, otherStore, "다른 테마", "설명", "썸네일", true);

        assertThatThrownBy(() -> Reservation.create(member, store, date, startAt, otherStoreTheme))
                .isInstanceOf(DomainValidationException.class);
    }
}
