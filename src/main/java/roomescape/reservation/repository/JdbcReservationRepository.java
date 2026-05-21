package roomescape.reservation.repository;

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
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;

@Repository
public class JdbcReservationRepository implements ReservationRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Reservation> rowMapper = (resultSet, rowNumber) -> {
        Member member = Member.load(
                resultSet.getLong("member_id"),
                resultSet.getString("member_name"),
                resultSet.getString("login_id"),
                resultSet.getString("password"),
                MemberRole.valueOf(resultSet.getString("role"))
        );

        Store store = Store.load(
                resultSet.getLong("store_id"),
                resultSet.getString("store_name")
        );

        Theme theme = Theme.load(
                resultSet.getLong("theme_id"),
                store,
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url"),
                resultSet.getBoolean("is_active")
        );

        return Reservation.load(
                resultSet.getLong("reservation_id"),
                member,
                store,
                resultSet.getDate("date").toLocalDate(),
                resultSet.getTime("start_at").toLocalTime(),
                theme,
                ReservationStatus.valueOf(resultSet.getString("status"))
        );
    };

    public JdbcReservationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = selectReservationSql();

        return jdbcTemplate.query(sql, new MapSqlParameterSource(), rowMapper);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = selectReservationSql() + """
                WHERE r.id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource("id", id);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAllByNameOrderByDateAndTime(String name) {
        String sql = selectReservationSql() + """
                WHERE m.name = :name
                ORDER BY r.date ASC, r.start_at ASC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Reservation save(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("member_id", reservation.member().id())
                .addValue("store_id", reservation.store().id())
                .addValue("date", reservation.date())
                .addValue("start_at", reservation.time())
                .addValue("theme_id", reservation.theme().id())
                .addValue("status", reservation.status().name());

        Long savedId = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Reservation.load(
                savedId,
                reservation.member(),
                reservation.store(),
                reservation.date(),
                reservation.time(),
                reservation.theme(),
                reservation.status()
        );
    }

    @Override
    public boolean existsByDateAndTimeAndThemeId(
            LocalDate date,
            LocalTime time,
            long themeId,
            ReservationStatus status
    ) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE date = :date
                    AND start_at = :start_at
                    AND theme_id = :theme_id
                    AND status = :status
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("start_at", time)
                .addValue("theme_id", themeId)
                .addValue("status", status.name());

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByDateAndTimeAndThemeId(
            LocalDate date,
            LocalTime time,
            long themeId,
            long excludeId,
            ReservationStatus status
    ) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation
                WHERE date = :date
                    AND start_at = :start_at
                    AND theme_id = :theme_id
                    AND id != :excludeId
                    AND status = :status
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("start_at", time)
                .addValue("theme_id", themeId)
                .addValue("excludeId", excludeId)
                .addValue("status", status.name());

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNameAndDateAndTime(String name, LocalDate date, LocalTime time) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation r
                JOIN member m ON r.member_id = m.id
                WHERE m.name = :name
                  AND r.date = :date
                  AND r.start_at = :start_at
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("date", date)
                .addValue("start_at", time);

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTimeId(long timeId, ReservationStatus status) {
        String sql = """
                SELECT COUNT(*)
                FROM reservation r
                JOIN reservation_time rt ON r.start_at = rt.start_at
                    AND r.store_id = rt.store_id
                WHERE rt.id = :timeId
                  AND r.status = :status
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("timeId", timeId)
                .addValue("status", status.name());

        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public Reservation updateStatus(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET status = :status
                WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservation.id())
                .addValue("status", reservation.status().name());

        jdbcTemplate.update(sql, params);
        return reservation;
    }

    @Override
    public Reservation updateDateAndTime(Reservation reservation) {
        String sql = """
                UPDATE reservation
                SET date = :date, start_at = :start_at
                WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservation.id())
                .addValue("date", reservation.date())
                .addValue("start_at", reservation.time());

        jdbcTemplate.update(sql, params);
        return reservation;
    }

    @Override
    public List<Reservation> findAllByStoreId(Long storeId) {
        String sql = selectReservationSql() +
                """
                        WHERE r.store_id = :storeId
                        """;
        SqlParameterSource params = new MapSqlParameterSource("storeId", storeId);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    private String selectReservationSql() {
        return """
                SELECT
                    r.id AS reservation_id,
                    r.date,
                    r.start_at,
                    r.status,
                    m.id AS member_id,
                    m.name AS member_name,
                    m.login_id,
                    m.password,
                    m.role,
                    s.id AS store_id,
                    s.name AS store_name,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail_url,
                    t.is_active
                FROM reservation r
                JOIN member m ON r.member_id = m.id
                JOIN store s ON r.store_id = s.id
                JOIN theme t ON r.theme_id = t.id
                """;
    }
}
