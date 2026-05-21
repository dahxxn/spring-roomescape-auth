package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.store.domain.Store;
import roomescape.store.repository.StoreRepository;

@Service
public class AuthorizationService {
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    public AuthorizationService(
            StoreRepository storeRepository,
            ReservationRepository reservationRepository
    ) {
        this.storeRepository = storeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void validateManagerReservationAccess(Long loginMemberId, Long reservationId) {
        Store managerStore = storeRepository.findByMemberId(loginMemberId)
                .orElseThrow(() -> new ForbiddenException("관리 매장이 없습니다."));

        reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."))
                .validateManagerAccess(managerStore.id());
    }
}
