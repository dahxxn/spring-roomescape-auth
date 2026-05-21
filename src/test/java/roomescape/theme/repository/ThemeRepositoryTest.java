package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;

@JdbcTest
class ThemeRepositoryTest {
    private static final String DEFAULT_DESCRIPTION = "테마 설명";
    private static final String DEFAULT_THUMBNAIL_URL = "테마 썸네일";

    private JdbcThemeRepository jdbcThemeRepository;
    private Store store;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
        store = createStore("강남점");
    }

    @Test
    @DisplayName("활성화된 테마 목록을 가나다순으로 조회한다.")
    void findByActive() {
        List<Theme> themes = saveAll(generateActiveThemesByName(List.of("다테마", "나테마", "가테마")));
        Collections.sort(themes, Comparator.comparing(Theme::name));

        List<Theme> actual = jdbcThemeRepository.findByStatus(true);

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(themes);
    }

    private List<Theme> saveAll(List<Theme> themes) {
        List<Theme> savedThemes = new ArrayList<>();

        for (Theme theme : themes) {
            Theme savedTheme = jdbcThemeRepository.save(theme);
            savedThemes.add(savedTheme);
        }

        return savedThemes;
    }

    private List<Theme> generateActiveThemesByName(List<String> names) {
        List<Theme> themes = new ArrayList<>();

        for (String name : names) {
            Theme theme = Theme.create(store, name, DEFAULT_DESCRIPTION, DEFAULT_THUMBNAIL_URL);
            themes.add(theme.changeStatus(true));
        }

        return themes;
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
}
