package roomescape.time.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.store.domain.Store;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationTimeRepository implements ReservationTimeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<ReservationTime> rowMapper = (resultSet, rowNum) -> {
        Store store = Store.load(
                resultSet.getLong("store_id"),
                resultSet.getString("store_name")
        );

        return ReservationTime.load(
                resultSet.getLong("time_id"),
                store,
                resultSet.getTime("start_at").toLocalTime()
        );
    };

    public JdbcReservationTimeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at,
                    s.id AS store_id,
                    s.name AS store_name
                FROM reservation_time rt
                JOIN store s ON rt.store_id = s.id
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource(), rowMapper);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at,
                    s.id AS store_id,
                    s.name AS store_name
                FROM reservation_time rt
                JOIN store s ON rt.store_id = s.id
                WHERE rt.id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource("id", id);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("store_id", reservationTime.store().id())
                .addValue("start_at", reservationTime.startAt());

        Long savedId = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return ReservationTime.load(
                savedId,
                reservationTime.store(),
                reservationTime.startAt()
        );
    }

    @Override
    public void delete(Long id) {
        String sql = """
                DELETE FROM reservation_time
                WHERE id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation_time
                WHERE start_at = :start_at
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("start_at", startAt);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<ReservationTime> findAvailableByDateAndThemeId(LocalDate date, long themeId, ReservationStatus status) {
        String sql = """
                SELECT
                    rt.id AS time_id,
                    rt.start_at,
                    s.id AS store_id,
                    s.name AS store_name
                FROM reservation_time rt
                JOIN store s ON rt.store_id = s.id
                WHERE rt.start_at NOT IN (
                    SELECT r.start_at
                    FROM reservation r
                    WHERE r.date = :date
                      AND r.theme_id = :theme_id
                      AND r.status = :status
                )
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("theme_id", themeId)
                .addValue("status", status.name());

        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
