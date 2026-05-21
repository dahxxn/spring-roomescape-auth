package roomescape.closeddate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import roomescape.closeddate.domain.ClosedDate;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;

@SpringBootTest
@Transactional
class ClosedDateServiceTest {

    private static final LocalDate DEFAULT_DATE = LocalDate.of(2099, 1, 1);

    @Autowired
    private ClosedDateService closedDateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("등록된 휴무일과 조회된 휴무일의 모든 필드는 일치한다.")
    void readClosedDate() {
        Long storeId = createStore("강남점");
        ClosedDate saved = closedDateService.register(storeId, DEFAULT_DATE);

        List<ClosedDate> actual = closedDateService.findClosedDates();

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(List.of(saved));
    }

    @Test
    @DisplayName("등록된 휴무일이 여러개이면 조회 시 등록된 개수만큼 반환한다.")
    void findClosedDates() {
        Long storeId = createStore("강남점");

        closedDateService.register(storeId, DEFAULT_DATE);
        closedDateService.register(storeId, DEFAULT_DATE.plusDays(1));

        List<ClosedDate> actual = closedDateService.findClosedDates();

        assertThat(actual).hasSize(2);
    }

    @Test
    @DisplayName("휴무일을 1개 등록하면 데이터 수가 1 증가한다.")
    void register() {
        Long storeId = createStore("강남점");

        closedDateService.register(storeId, DEFAULT_DATE);

        assertThat(closedDateService.findClosedDates()).hasSize(1);
    }

    @Test
    @DisplayName("등록한 휴무일과 다시 조회한 휴무일의 모든 필드가 일치한다.")
    void register_fields_match() {
        Long storeId = createStore("강남점");

        ClosedDate registered = closedDateService.register(storeId, DEFAULT_DATE);

        assertThat(closedDateService.findClosedDates())
                .usingRecursiveComparison()
                .isEqualTo(List.of(registered));
    }

    @Test
    @DisplayName("이미 등록된 휴무일을 다시 등록하면 예외가 발생한다.")
    void register_duplicate_date() {
        Long storeId = createStore("강남점");
        closedDateService.register(storeId, DEFAULT_DATE);

        assertThatThrownBy(() -> closedDateService.register(storeId, DEFAULT_DATE))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("등록된 휴무일 2개 중 한 개를 삭제하면 데이터 수는 1개가 된다.")
    void deregister() {
        Long storeId = createStore("강남점");

        ClosedDate saved1 = closedDateService.register(storeId, DEFAULT_DATE);
        closedDateService.register(storeId, DEFAULT_DATE.plusDays(1));

        closedDateService.deregister(saved1.id());

        assertThat(closedDateService.findClosedDates()).hasSize(1);
    }

    @Test
    @DisplayName("등록되지 않은 휴무일을 삭제하면 예외가 발생한다.")
    void deregister_not_exists() {
        Long wrongId = Long.MIN_VALUE;

        assertThatThrownBy(() -> closedDateService.deregister(wrongId))
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
}
