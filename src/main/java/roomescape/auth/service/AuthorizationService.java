package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.closeddate.repository.ClosedDateRepository;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.store.domain.Store;
import roomescape.store.repository.StoreRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.repository.ReservationTimeRepository;

@Service
public class AuthorizationService {
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;
    private final ClosedDateRepository closedDateRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public AuthorizationService(
            StoreRepository storeRepository,
            ReservationRepository reservationRepository,
            ClosedDateRepository closedDateRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.storeRepository = storeRepository;
        this.reservationRepository = reservationRepository;
        this.closedDateRepository = closedDateRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public void validateManagerReservationAccess(Long loginMemberId, Long reservationId) {
        Store managerStore = storeRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new ForbiddenException("관리 매장이 없습니다."));

        reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."))
                .validateManagerAccess(managerStore.id());
    }

    public void validateManagerStoreAccess(Long loginMemberId, Long storeId) {
        Store managerStore = storeRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new ForbiddenException("관리 매장이 없습니다."));

        if (!managerStore.id().equals(storeId)) {
            throw new ForbiddenException("자기 매장만 관리할 수 있습니다.");
        }
    }

    public void validateManagerClosedDateAccess(Long loginMemberId, Long closedDateId) {
        Store managerStore = storeRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new ForbiddenException("관리 매장이 없습니다."));

        closedDateRepository.findById(closedDateId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 휴무일입니다."))
                .validateManagerAccess(managerStore.id());
    }

    public void validateManagerReservationTimeAccess(Long loginMemberId, Long timeId) {
        Store managerStore = storeRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new ForbiddenException("관리 매장이 없습니다."));

        reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 시간입니다."))
                .validateManagerAccess(managerStore.id());
    }

    public void validateManagerThemeAccess(Long loginMemberId, Long themeId) {
        Store managerStore = storeRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new ForbiddenException("관리 매장이 없습니다."));

        themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."))
                .validateManagerAccess(managerStore.id());
    }
}
