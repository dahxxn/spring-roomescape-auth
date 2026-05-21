package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.JdbcReservationTimeRepository;

@JdbcTest
class ReservationRepositoryTest {
    private final String name = "한다";
    private final LocalDate date1 = LocalDate.of(2099, 1, 1);
    private final LocalDate date2 = LocalDate.of(2099, 9, 1);

    private Member member;
    private Member otherMember;
    private Store store;
    private ReservationTime reservationTime1;
    private ReservationTime reservationTime2;
    private Theme theme;

    private JdbcReservationRepository jdbcReservationRepository;
    private JdbcReservationTimeRepository jdbcReservationTimeRepository;
    private JdbcThemeRepository jdbcThemeRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);
        jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);

        store = createStore("강남점");
        member = createMember("한다", "handa");
        otherMember = createMember("브라운", "brown");

        reservationTime1 = jdbcReservationTimeRepository.save(
                ReservationTime.create(store, LocalTime.of(12, 0))
        );
        reservationTime2 = jdbcReservationTimeRepository.save(
                ReservationTime.create(store, LocalTime.of(20, 0))
        );

        theme = jdbcThemeRepository.save(
                Theme.create(store, "테마", "설명", "썸네일")
        );
    }

    @Test
    @DisplayName("나의 예약들을 조회하면 날짜/시간 오름차순으로 정렬해 모두 조회한다.")
    void findAllByName() {
        List<Reservation> reservations = saveAll(
                List.of(
                        Reservation.create(member, store, date1, reservationTime1.startAt(), theme),
                        Reservation.create(member, store, date1, reservationTime2.startAt(), theme),
                        Reservation.create(member, store, date2, reservationTime1.startAt(), theme),
                        Reservation.create(member, store, date2, reservationTime2.startAt(), theme)
                )
        );
        reservations.sort(Comparator.comparing(Reservation::date).thenComparing(Reservation::time));

        List<Reservation> actual = jdbcReservationRepository.findAllByNameOrderByDateAndTime(name);

        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(reservations);
    }

    @Test
    @DisplayName("자기 자신을 제외하고 날짜/시간/테마 중복 여부를 확인한다.")
    void existsByDateAndTimeAndThemeId_excludeSelf() {
        Reservation saved = jdbcReservationRepository.save(
                Reservation.create(member, store, date1, reservationTime1.startAt(), theme)
        );

        assertThat(jdbcReservationRepository.existsByDateAndTimeAndThemeId(
                date1, reservationTime1.startAt(), theme.id(), saved.id(), ReservationStatus.RESERVED))
                .isFalse();
    }

    @Test
    @DisplayName("자기 자신 외 다른 예약이 있으면 true를 반환한다.")
    void existsByDateAndTimeAndThemeId_excludeSelf_otherExists() {
        Reservation saved = jdbcReservationRepository.save(
                Reservation.create(member, store, date1, reservationTime1.startAt(), theme)
        );
        jdbcReservationRepository.save(
                Reservation.create(otherMember, store, date1, reservationTime2.startAt(), theme)
        );

        assertThat(jdbcReservationRepository.existsByDateAndTimeAndThemeId(
                date1, reservationTime2.startAt(), theme.id(), saved.id(), ReservationStatus.RESERVED))
                .isTrue();
    }

    private List<Reservation> saveAll(List<Reservation> reservations) {
        List<Reservation> savedReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            Reservation saved = jdbcReservationRepository.save(reservation);
            savedReservations.add(saved);
        }
        return savedReservations;
    }

    private Store createStore(String name) {
        jdbcTemplate.getJdbcTemplate().update("INSERT INTO store (name) VALUES (?)", name);

        Long id = jdbcTemplate.getJdbcTemplate().queryForObject(
                "SELECT id FROM store WHERE name = ?",
                Long.class,
                name
        );

        return Store.load(id, name);
    }

    private Member createMember(String name, String loginId) {
        jdbcTemplate.getJdbcTemplate().update("""
                INSERT INTO member (name, login_id, password, role)
                VALUES (?, ?, ?, ?)
                """, name, loginId, "password", "USER");

        Long id = jdbcTemplate.getJdbcTemplate().queryForObject(
                "SELECT id FROM member WHERE login_id = ?",
                Long.class,
                loginId
        );

        return Member.load(id, name, loginId, "password", MemberRole.USER);
    }
}
