package roomescape.member.repository;

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

@Repository
public class JdbcMemberRepository implements MemberRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Member> rowMapper = (rs, rowNum) ->
            Member.load(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("login_id"),
                    rs.getString("password"),
                    MemberRole.valueOf(rs.getString("role"))
            );

    public JdbcMemberRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        String sql = "SELECT * FROM member WHERE login_id = :loginId";
        SqlParameterSource params = new MapSqlParameterSource("loginId", loginId);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Member save(Member member) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", member.name())
                .addValue("login_id", member.loginId())
                .addValue("password", member.password())
                .addValue("role", member.role().name())
                ;
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Member.load(id, member.name(), member.loginId(), member.password(), member.role());
    }
}
