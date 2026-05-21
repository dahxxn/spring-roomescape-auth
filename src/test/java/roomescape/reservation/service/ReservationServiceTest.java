package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import roomescape.closeddate.service.ClosedDateService;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ClosedDateService closedDateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String name = "한다";
    private final LocalDate date1 = LocalDate.now().plusWeeks(1);
    private final LocalDate date2 = LocalDate.now().plusWeeks(2);

    private Long storeId;
    private Long memberId;
    private Member member;
    private Store store;
    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private Theme theme1;
    private Theme theme2;

    @BeforeEach
    void setup() {
        storeId = createStore("강남점");
        memberId = createMember(name, "handa");

        store = Store.load(storeId, "강남점");
        member = Member.load(memberId, name, "handa", "password", MemberRole.USER);

        reservationTime1 = reservationTimeService.create(storeId, LocalTime.of(15, 40));
        reservationTime2 = reservationTimeService.create(storeId, LocalTime.of(16, 0));
        theme1 = themeService.register(storeId, "테마1", "설명1", "썸네일1");
        theme2 = themeService.register(storeId, "테마2", "설명2", "썸네일2");
    }

    @Test
    @DisplayName("전체 예약 정보를 가져온다.")
    void findAll() {
        Long otherMemberId = createMember("송송", "songsong");

        reservationService.create(memberId, storeId, date1, reservationTime1.id(), theme1.id());
        reservationService.create(otherMemberId, storeId, date2, reservationTime1.id(), theme2.id());

        List<Reservation> actual = reservationService.findAll();

        assertThat(actual).hasSize(2);
    }

    @Test
    @DisplayName("나의 예약들을 조회하면 날짜/시간 오름차순으로 정렬해 모두 조회한다.")
    void findAllByName() {
        reservationService.create(memberId, storeId, date1, reservationTime1.id(), theme1.id());
        reservationService.create(memberId, storeId, date1, reservationTime2.id(), theme1.id());
        reservationService.create(memberId, storeId, date2, reservationTime1.id(), theme1.id());
        reservationService.create(memberId, storeId, date2, reservationTime2.id(), theme1.id());

        List<Reservation> actual = reservationService.findAllByName(name);

        assertThat(actual).hasSize(4);
        assertThat(actual).isSortedAccordingTo(
                Comparator.comparing(Reservation::date).thenComparing(Reservation::time));
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void create() {
        reservationService.create(memberId, storeId, date1, reservationTime1.id(), theme1.id());

        assertThat(reservationService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간이면 예외를 발생한다.")
    void create_does_not_exist_reservation_time() {
        Long wrongTimeId = Long.MIN_VALUE;

        assertThatThrownBy(() -> reservationService.create(memberId, storeId, date1, wrongTimeId, theme1.id()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약 생성 시 예약 날짜/시간/테마가 중복되면 예외를 발생한다.")
    void create_duplicate_reservation() {
        Long otherMemberId = createMember("브라운", "brown");
        reservationService.create(otherMemberId, storeId, date1, reservationTime1.id(), theme1.id());

        assertThatThrownBy(
                () -> reservationService.create(memberId, storeId, date1, reservationTime1.id(), theme1.id()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("예약을 취소하면 CANCELED 상태가 된다.")
    void cancel() {
        Reservation savedReservation = reservationService.create(memberId, storeId, date1, reservationTime1.id(),
                theme1.id());

        Reservation actual = reservationService.cancel(savedReservation.id(), name);

        assertThat(actual.status()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("이미 지난 예약 취소 시 예외가 발생한다.")
    void cancel_past_reservation() {
        Reservation pastReservation = Reservation.load(
                null,
                member,
                store,
                LocalDate.of(2000, 1, 1),
                reservationTime1.startAt(),
                theme1,
                ReservationStatus.RESERVED
        );
        Reservation saved = reservationRepository.save(pastReservation);

        assertThatThrownBy(() -> reservationService.cancel(saved.id(), name))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 날짜/시간을 변경한다.")
    void change() {
        Reservation saved = reservationService.create(memberId, storeId, date1, reservationTime1.id(), theme1.id());

        Reservation actual = reservationService.change(saved.id(), name, date2, reservationTime2.id());

        assertThat(actual.date()).isEqualTo(date2);
        assertThat(actual.time()).isEqualTo(reservationTime2.startAt());
    }

    @Test
    @DisplayName("이미 지난 예약 변경 시 예외가 발생한다.")
    void change_past_reservation() {
        Reservation pastReservation = Reservation.load(
                null,
                member,
                store,
                LocalDate.of(2000, 1, 1),
                reservationTime1.startAt(),
                theme1,
                ReservationStatus.RESERVED
        );
        Reservation saved = reservationRepository.save(pastReservation);

        assertThatThrownBy(() -> reservationService.change(saved.id(), name, date1, reservationTime1.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("변경하려는 날짜/시간/테마가 이미 예약된 경우 예외가 발생한다.")
    void change_duplicate_reservation() {
        Long otherMemberId = createMember("브라운", "brown");
        reservationService.create(otherMemberId, storeId, date1, reservationTime1.id(), theme1.id());
        Reservation saved = reservationService.create(memberId, storeId, date1, reservationTime2.id(), theme1.id());

        assertThatThrownBy(() -> reservationService.change(saved.id(), name, date1, reservationTime1.id()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("같은 날짜/시간으로 변경 요청 시 정상 처리된다.")
    void change_same_datetime() {
        Reservation saved = reservationService.create(memberId, storeId, date1, reservationTime1.id(), theme1.id());

        assertThat(reservationService.change(saved.id(), name, date1, reservationTime1.id()))
                .isNotNull();
    }

    @Test
    @DisplayName("변경하려는 날짜가 휴무일인 경우 예외가 발생한다.")
    void change_closed_date() {
        Reservation saved = reservationService.create(memberId, storeId, date1, reservationTime1.id(), theme1.id());
        closedDateService.register(storeId, date2);

        assertThatThrownBy(() -> reservationService.change(saved.id(), name, date2, reservationTime1.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 변경 시 예외가 발생한다.")
    void change_not_exist_reservation() {
        Long wrongId = Long.MIN_VALUE;

        assertThatThrownBy(() -> reservationService.change(wrongId, name, date1, reservationTime1.id()))
                .isInstanceOf(NotFoundException.class);
    }

    private Long createStore(String name) {
        jdbcTemplate.update("INSERT INTO store (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM store WHERE name = ?",
                Long.class,
                name
        );
    }

    private Long createMember(String name, String loginId) {
        jdbcTemplate.update("""
                INSERT INTO member (name, login_id, password, role)
                VALUES (?, ?, ?, ?)
                """, name, loginId, "password", "USER");

        return jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE login_id = ?",
                Long.class,
                loginId
        );
    }
}
