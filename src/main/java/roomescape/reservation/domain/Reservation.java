package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.DomainValidationException;
import roomescape.member.domain.Member;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;

public class Reservation {
    private final Long id;
    private final Member member;
    private final Store store;
    private final LocalDate date;
    private final LocalTime time;
    private final Theme theme;
    private final ReservationStatus status;

    private Reservation(Long id, Member member, Store store, LocalDate date, LocalTime time, Theme theme,
                        ReservationStatus status) {
        validate(member, store, date, time, theme);
        validateThemeBelongsToStore(store, theme);
        this.id = id;
        this.member = member;
        this.store = store;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation create(Member member, Store store, LocalDate date, LocalTime time, Theme theme) {
        validatePast(date, time);
        return new Reservation(null, member, store, date, time, theme, ReservationStatus.RESERVED);
    }

    public static Reservation load(Long id, Member member, Store store, LocalDate date, LocalTime time, Theme theme,
                                   ReservationStatus status) {
        return new Reservation(id, member, store, date, time, theme, status);
    }

    private static void validate(Member member, Store store, LocalDate date, LocalTime time, Theme theme) {
        validateMember(member);
        validateStore(store);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
    }

    private static void validatePast(LocalDate date, LocalTime time) {
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("과거 날짜/시간으로는 예약할 수 없습니다.");
        }
    }

    private static void validateMember(Member member) {
        if (member == null) {
            throw new DomainValidationException("예약자는 필수입니다.");
        }
    }

    private static void validateStore(Store store) {
        if (store == null) {
            throw new DomainValidationException("스토어는 필수입니다.");
        }
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new DomainValidationException("예약 날짜는 필수입니다.");
        }
    }

    private static void validateTime(LocalTime time) {
        if (time == null) {
            throw new DomainValidationException("예약 시간은 필수입니다.");
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new DomainValidationException("테마는 필수입니다.");
        }
    }

    private static void validateThemeBelongsToStore(Store store, Theme theme) {
        if (!theme.store().id().equals(store.id())) {
            throw new DomainValidationException("해당 매장의 테마가 아닙니다.");
        }
    }

    public Long id() {
        return id;
    }

    public Member member() {
        return member;
    }

    public Store store() {
        return store;
    }

    public LocalDate date() {
        return date;
    }

    public LocalTime time() {
        return time;
    }

    public Theme theme() {
        return theme;
    }

    public ReservationStatus status() {
        return status;
    }

    public Reservation cancel() {
        return new Reservation(id, member, store, date, time, theme, ReservationStatus.CANCELED);
    }

    public Reservation rescheduled(LocalDate date, LocalTime time) {
        validateChangeable();
        validatePast(date, time);
        return new Reservation(id, member, store, date, time, theme, status);
    }

    private void validateChangeable() {
        if (status == ReservationStatus.CANCELED) {
            throw new ConflictException("이미 취소된 예약은 수정할 수 없습니다.");
        }
    }
}
