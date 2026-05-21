package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;

@JdbcTest
class ReservationTimeRepositoryTest {
    private JdbcReservationTimeRepository jdbcReservationTimeRepository;
    private JdbcThemeRepository jdbcThemeRepository;
    private JdbcReservationRepository jdbcReservationRepository;

    private Store store;
    private Member member;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);

        store = createStore("강남점");
        member = createMember("한다", "handa");
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회한다.")
    void findAvailableTimes() {
        ReservationTime time1 = jdbcReservationTimeRepository.save(ReservationTime.create(store, LocalTime.of(12, 0)));
        ReservationTime time2 = jdbcReservationTimeRepository.save(ReservationTime.create(store, LocalTime.of(13, 0)));
        jdbcReservationTimeRepository.save(ReservationTime.create(store, LocalTime.of(14, 0)));

        LocalDate date = LocalDate.of(2099, 10, 10);

        Theme theme = Theme.create(store, "테마1", "테마 설명", "테마 썸네일")
                .changeStatus(true);
        Theme savedTheme = jdbcThemeRepository.save(theme);

        jdbcReservationRepository.save(Reservation.create(member, store, date, time1.startAt(), savedTheme));
        jdbcReservationRepository.save(Reservation.create(member, store, date, time2.startAt(), savedTheme));

        List<ReservationTime> availableTimes = jdbcReservationTimeRepository.findAvailableByDateAndThemeId(
                date, savedTheme.id(), ReservationStatus.RESERVED);

        assertThat(availableTimes).hasSize(1);
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
