package roomescape.theme.repository;

import java.time.LocalDate;
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
import roomescape.theme.domain.Theme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> {
        Store store = Store.load(
                resultSet.getLong("store_id"),
                resultSet.getString("store_name")
        );

        return Theme.load(
                resultSet.getLong("theme_id"),
                store,
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url"),
                resultSet.getBoolean("is_active")
        );
    };

    public JdbcThemeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAll() {
        String sql = """
                SELECT
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail_url,
                    t.is_active,
                    s.id AS store_id,
                    s.name AS store_name
                FROM theme t
                JOIN store s ON t.store_id = s.id
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource(), rowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = """
                SELECT
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail_url,
                    t.is_active,
                    s.id AS store_id,
                    s.name AS store_name
                FROM theme t
                JOIN store s ON t.store_id = s.id
                WHERE t.id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource("id", id);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findByStatus(boolean status) {
        String sql = """
                SELECT
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail_url,
                    t.is_active,
                    s.id AS store_id,
                    s.name AS store_name
                FROM theme t
                JOIN store s ON t.store_id = s.id
                WHERE t.is_active = :status
                ORDER BY t.name ASC
                """;

        SqlParameterSource params = new MapSqlParameterSource("status", status);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("store_id", theme.store().id())
                .addValue("name", theme.name())
                .addValue("description", theme.description())
                .addValue("thumbnail_url", theme.thumbnailUrl())
                .addValue("is_active", theme.isActive());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return Theme.load(
                id,
                theme.store(),
                theme.name(),
                theme.description(),
                theme.thumbnailUrl(),
                theme.isActive()
        );
    }

    @Override
    public Theme updateStatus(Theme theme) {
        String sql = """
                UPDATE theme
                SET is_active = :is_active
                WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", theme.id())
                .addValue("is_active", theme.isActive());

        jdbcTemplate.update(sql, params);
        return theme;
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit, ReservationStatus status) {
        String sql = """
                SELECT
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description,
                    t.thumbnail_url,
                    t.is_active,
                    s.id AS store_id,
                    s.name AS store_name,
                    COUNT(r.id) AS reservation_count
                FROM reservation r
                JOIN theme t ON r.theme_id = t.id
                JOIN store s ON t.store_id = s.id
                WHERE t.is_active = true
                  AND r.status = :status
                  AND r.date >= :startDate
                  AND r.date < :endDate
                GROUP BY
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url,
                    t.is_active,
                    s.id,
                    s.name
                ORDER BY reservation_count DESC
                LIMIT :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit)
                .addValue("status", status.name());

        return jdbcTemplate.query(sql, params, rowMapper);
    }
}
