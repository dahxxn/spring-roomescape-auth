package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.store.domain.Store;
import roomescape.store.repository.StoreRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Slf4j
@Service
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final StoreRepository storeRepository;

    public ThemeService(ThemeRepository themeRepository, StoreRepository storeRepository) {
        this.themeRepository = themeRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public List<Theme> findThemes() {
        return themeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Theme findTheme(Long id) {
        return findThemeOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Theme> findActiveThemes() {
        return themeRepository.findByStatus(true);
    }

    @Transactional(readOnly = true)
    public List<Theme> findPopularThemes(int top) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);
        LocalDate endDate = today;
        return themeRepository.findPopularThemes(startDate, endDate, top, ReservationStatus.RESERVED);
    }

    @Transactional
    public Theme register(Long storeId, String name, String description, String thumbnailUrl) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new NotFoundException("해당 매장이 존재하지 않습니다."));
        Theme theme = themeRepository.save(Theme.create(store, name, description, thumbnailUrl));
        log.info("Theme registered: id={}, storeId={}, name={}", theme.id(), theme.store().id(), theme.name());
        return theme;
    }

    @Transactional
    public Theme updateStatus(Long id, boolean isActive) {
        Theme theme = findThemeOrThrow(id);
        Theme changedTheme = theme.changeStatus(isActive);
        Theme updatedTheme = themeRepository.updateStatus(changedTheme);
        log.info("Theme status updated: id={}, name={}, isActive={}",
                updatedTheme.id(), updatedTheme.name(), updatedTheme.isActive());
        return updatedTheme;
    }

    @NonNull
    private Theme findThemeOrThrow(Long id) {
        return themeRepository.findById(id).orElseThrow(() -> {
            log.warn("Theme not found: id={}", id);
            return new NotFoundException("해당 테마가 존재하지 않습니다.");
        });
    }
}
