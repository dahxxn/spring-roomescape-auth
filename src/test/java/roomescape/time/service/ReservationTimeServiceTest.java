package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;

@SpringBootTest
@Transactional
class ReservationTimeServiceTest {

    private Long storeId;
    private Long memberId;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        storeId = createStore("강남점");
        memberId = createMember("한다", "handa");

        reservationTimeService.create(storeId, LocalTime.of(15, 40));
        reservationTimeService.create(storeId, LocalTime.of(16, 0));
    }

    @Test
    @DisplayName("모든 예약 시간 정보를 조회한다.")
    void findAll() {
        List<ReservationTime> result = reservationTimeService.findAll();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void create() {
        reservationTimeService.create(storeId, LocalTime.of(12, 0));

        assertThat(reservationTimeService.findAll().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간 생성 시 예외가 발생한다.")
    void create_already_exist() {
        assertThatThrownBy(() -> reservationTimeService.create(storeId, LocalTime.of(15, 40)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void delete() {
        ReservationTime savedReservation = reservationTimeService.create(storeId, LocalTime.of(12, 0));
        Long id = savedReservation.id();

        reservationTimeService.delete(id);

        assertThat(reservationTimeService.findAll().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("예약이 존재하는 시간 삭제 시 예외가 발생한다.")
    void delete_time_with_reservation() {
        ReservationTime time = reservationTimeService.create(storeId, LocalTime.of(12, 0));
        Theme theme = themeService.register(storeId, "테마", "설명", "썸네일");

        reservationService.create(
                memberId,
                storeId,
                LocalDate.of(2099, 1, 1),
                time.id(),
                theme.id()
        );

        assertThatThrownBy(() -> reservationTimeService.delete(time.id()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간 삭제 시 정상 처리된다.")
    void deleteNotExist() {
        assertThatCode(() -> reservationTimeService.delete(999L))
                .doesNotThrowAnyException();
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
