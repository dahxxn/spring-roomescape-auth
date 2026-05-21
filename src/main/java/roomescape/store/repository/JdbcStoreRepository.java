package roomescape.store.repository;

import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import roomescape.store.domain.Store;

@Repository
public class JdbcStoreRepository implements StoreRepository{
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Store> rowMapper = (resultSet, rowNum) -> Store.load(
            resultSet.getLong("id"),
            resultSet.getString("name")
    );

    public JdbcStoreRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Optional<Store> findById(Long id) {
        String sql = "SELECT * FROM store WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        return findStoreOptional(sql, params);
    }

    @Override
    public  Optional<Store> findByMemberId(Long memberId) {
        String sql = """
                SELECT s.*
                FROM store s
                JOIN store_admin sa ON s.id = sa.store_id
                WHERE sa.member_id = :memberId
                """;
        SqlParameterSource params = new MapSqlParameterSource("memberId", memberId);
        return findStoreOptional(sql, params);
    }

    private Optional<Store> findStoreOptional(String sql, SqlParameterSource params) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
