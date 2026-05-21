package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.closeddate.repository.ClosedDateRepository;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.store.domain.Store;
import roomescape.store.repository.StoreRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@Slf4j
@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ClosedDateRepository closedDateRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;


    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ClosedDateRepository closedDateRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              StoreRepository storeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.closedDateRepository = closedDateRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByName(String name) {
        return reservationRepository.findAllByNameOrderByDateAndTime(name);
    }

    @Transactional
    public Reservation create(Long memberId, Long storeId, LocalDate date, Long timeId, Long themeId) {
        Member member = findMemberOrThrow(memberId);
        Store store = findStoreOrThrow(storeId);
        ReservationTime reservationTime = findReservationTimeOrThrow(timeId);
        Theme theme = findThemeByIdOrThrow(themeId);

        validateNotClosedDate(date);
        validateNotAlreadyBookedByOthers(date, reservationTime.startAt(), theme);
        validateUserHasNoReservationAtSameTime(member.name(), date, reservationTime);

        Reservation savedReservation = reservationRepository.save(
                Reservation.create(member, store, date, reservationTime.startAt(), theme)
        );

        log.info("Reservation created: memberId={}, storeId={}, date={}", member.id(), store.id(), date);
        return savedReservation;
    }

    @Transactional
    public Reservation cancel(Long id, String loginMemberName) {
        Reservation reservation = findReservationOrThrow(id);
        validateReservationOwner(reservation, loginMemberName);
        validateNotPastReservation(reservation, "취소");
        return cancelReservation(reservation);
    }

    @Transactional
    public Reservation cancelByAdmin(Long id) {
        Reservation reservation = findReservationOrThrow(id);
        validateNotPastReservation(reservation, "취소");
        return cancelReservation(reservation);
    }

    private Reservation cancelReservation(Reservation reservation) {
        Reservation canceledReservation = reservation.cancel();
        Reservation updatedReservation = reservationRepository.updateStatus(canceledReservation);
        log.info("Reservation canceled: id={}", updatedReservation.id());
        return updatedReservation;
    }

    @Transactional
    public Reservation change(Long id, String loginMemberName, LocalDate newDate, Long newTimeId) {
        Reservation reservation = findReservationOrThrow(id);
        validateReservationOwner(reservation, loginMemberName);
        validateNotPastReservation(reservation, "변경");

        LocalTime newTime = findReservationTimeOrThrow(newTimeId).startAt();
        validateNotClosedDate(newDate);
        validateNotAlreadyBookedByOthers(newDate, newTime, reservation.theme(), id);

        Reservation rescheduledReservation = reservation.rescheduled(newDate, newTime);
        Reservation updatedReservation = reservationRepository.updateDateAndTime(rescheduledReservation);
        log.info("Reservation changed: id={}, date={}, time={}",
                updatedReservation.id(), updatedReservation.date(), updatedReservation.time());
        return updatedReservation;
    }

    private void validateReservationOwner(Reservation reservation, String loginMemberName) {
        if (!reservation.member().name().equals(loginMemberName)) {
            log.warn("Forbidden reservation access: reservationId={}, owner={}, loginMember={}",
                    reservation.id(), reservation.member().name(), loginMemberName);
            throw new ForbiddenException("본인의 예약만 변경할 수 있습니다.");
        }
    }

    @NonNull
    private ReservationTime findReservationTimeOrThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> {
                    log.warn("Reservation time not found: id={}", timeId);
                    return new NotFoundException("존재하지 않는 예약 시간입니다.");
                });
    }

    @NonNull
    private Theme findThemeByIdOrThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> {
                    log.warn("Theme not found: id={}", themeId);
                    return new NotFoundException("해당 테마가 존재하지 않습니다.");
                });
    }

    @NonNull
    private Reservation findReservationOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reservation not found: id={}", id);
                    return new NotFoundException("존재하지 않는 예약입니다.");
                });
    }

    @NonNull
    private Member findMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("Member not found: id={}", memberId);
                    return new NotFoundException("존재하지 않는 회원입니다.");
                });
    }

    @NonNull
    private Store findStoreOrThrow(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    log.warn("Store not found: id={}", storeId);
                    return new NotFoundException("존재하지 않는 매장입니다.");
                });
    }

    private void validateNotPastReservation(Reservation reservation, String action) {
        if (LocalDateTime.of(reservation.date(), reservation.time()).isBefore(LocalDateTime.now())) {
            log.warn("Cannot {} past reservation: id={}", action, reservation.id());
            throw new IllegalArgumentException("이미 지난 예약은 " + action + "할 수 없습니다.");
        }
    }

    private void validateNotClosedDate(LocalDate date) {
        if (closedDateRepository.existsByDate(date)) {
            log.warn("Cannot reserve closed date: date={}", date);
            throw new IllegalArgumentException("예약 불가능한 날짜입니다.");
        }
    }

    private void validateNotAlreadyBookedByOthers(LocalDate date, LocalTime time, Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndThemeId(date, time, theme.id(), ReservationStatus.RESERVED)) {
            log.warn("Reservation already exists: date={}, time={}, theme={}", date, time, theme.name());
            throw new ConflictException("해당 날짜/시간/테마는 이미 예약되었습니다.");
        }
    }

    private void validateNotAlreadyBookedByOthers(LocalDate date, LocalTime time, Theme theme, Long excludeId) {
        if (reservationRepository.existsByDateAndTimeAndThemeId(date, time, theme.id(), excludeId,
                ReservationStatus.RESERVED)) {
            log.warn("Reservation already exists: date={}, time={}, theme={}", date, time, theme.name());
            throw new ConflictException("해당 날짜/시간/테마는 이미 예약되었습니다.");
        }
    }

    private void validateUserHasNoReservationAtSameTime(String name, LocalDate date, ReservationTime time) {
        if (reservationRepository.existsByNameAndDateAndTime(name, date, time.startAt())) {
            log.warn("User already has a reservation at the same time: name={}, date={}, time={}",
                    name, date, time.startAt());
            throw new ConflictException("동일한 날짜와 시간에 예약이 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByStoreId(Long storeId) {
        return reservationRepository.findAllByStoreId(storeId);
    }
}
