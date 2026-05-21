package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.NotFoundException;
import roomescape.store.domain.Store;
import roomescape.theme.domain.Theme;

@SpringBootTest
@Transactional
class ThemeServiceTest {
    private static final String DEFAULT_DESCRIPTION = "테마 설명";
    private static final String DEFAULT_THUMBNAIL_URL = "테마 썸네일";

    @Autowired
    private ThemeService themeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("등록된 테마가 여러개이면 조회 시 등록된 갯수만큼 반환한다.")
    void findThemes() {
        Long storeId = createStore("강남점");
        Store store = Store.load(storeId, "강남점");

        List<Theme> themes = List.of(
                Theme.create(store, "테마1", "테마1 설명", "테마1 썸네일"),
                Theme.create(store, "테마2", "테마2 설명", "테마2 썸네일"),
                Theme.create(store, "테마3", "테마3 설명", "테마3 썸네일")
        );
        saveAll(storeId, themes);

        List<Theme> actual = themeService.findThemes();

        assertThat(actual).hasSize(themes.size());
    }

    @Test
    @DisplayName("등록된 테마와 조회되는 테마의 모든 필드가 일치한다.")
    void findTheme() {
        Long storeId = createStore("강남점");
        Theme savedTheme = themeService.register(storeId, "테마1", "테마1 설명", "테마1 썸네일");

        Theme actual = themeService.findTheme(savedTheme.id());

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("등록되지 않은 테마 조회 시 예외가 발생한다.")
    void findTheme_unregistered() {
        Long unregisteredId = Long.MIN_VALUE;

        assertThatThrownBy(() -> themeService.findTheme(unregisteredId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("활성화된 테마 목록을 가나다순으로 조회한다.")
    void findActiveThemes() {
        Long storeId = createStore("강남점");
        Store store = Store.load(storeId, "강남점");

        List<Theme> themes = saveAll(storeId, generateActiveThemesByName(store, List.of("다테마", "나테마", "가테마")));
        themes.sort(Comparator.comparing(Theme::name));

        List<Theme> actual = themeService.findActiveThemes();

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(themes);
    }

    @Test
    @DisplayName("테마를 1개 등록하면 테마 데이터 수가 1 증가한다.")
    void register() {
        Long storeId = createStore("강남점");

        themeService.register(storeId, "테마1", "테마1 설명", "테마1 썸네일");

        assertThat(themeService.findThemes()).hasSize(1);
    }

    @Test
    @DisplayName("등록한 테마와 다시 조회한 테마의 모든 필드가 일치한다.")
    void register_theme_fields_match() {
        Long storeId = createStore("강남점");

        Theme registeredTheme = themeService.register(storeId, "테마1", "테마1 설명", "테마1 썸네일");

        assertThat(registeredTheme)
                .usingRecursiveComparison()
                .isEqualTo(themeService.findTheme(registeredTheme.id()));
    }

    @Test
    @DisplayName("테마를 활성화한다.")
    void changeStatus_active() {
        Long storeId = createStore("강남점");
        Theme savedTheme = themeService.register(storeId, "테마1", "테마1 설명", "테마1 썸네일");

        themeService.updateStatus(savedTheme.id(), true);

        assertThat(themeService.findTheme(savedTheme.id()).isActive()).isTrue();
    }

    @Test
    @DisplayName("테마를 비활성화한다.")
    void changeStatus_deactivate() {
        Long storeId = createStore("강남점");
        Theme theme = themeService.register(storeId, "테마1", "테마1 설명", "테마1 썸네일");
        themeService.updateStatus(theme.id(), true);

        themeService.updateStatus(theme.id(), false);

        assertThat(themeService.findTheme(theme.id()).isActive()).isFalse();
    }

    private List<Theme> saveAll(Long storeId, List<Theme> themes) {
        List<Theme> savedThemes = new ArrayList<>();

        for (Theme theme : themes) {
            Theme savedTheme = themeService.register(storeId, theme.name(), theme.description(), theme.thumbnailUrl());
            themeService.updateStatus(savedTheme.id(), theme.isActive());
            savedThemes.add(themeService.findTheme(savedTheme.id()));
        }

        return savedThemes;
    }

    private List<Theme> generateActiveThemesByName(Store store, List<String> names) {
        List<Theme> themes = new ArrayList<>();

        for (String name : names) {
            Theme theme = Theme.create(store, name, DEFAULT_DESCRIPTION, DEFAULT_THUMBNAIL_URL);
            themes.add(theme.changeStatus(true));
        }

        return themes;
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
