package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMember;
import roomescape.auth.annotation.LoginRequired;
import roomescape.auth.annotation.ManagerRequired;
import roomescape.auth.dto.request.LoginMemberDto;
import roomescape.auth.service.AuthorizationService;
import roomescape.reservation.dto.response.ReservationDetailDto;
import roomescape.reservation.service.ReservationService;
import roomescape.store.service.StoreService;

@RestController
@RequestMapping("/manager/reservations")
public class ManagerReservationController {
    private final ReservationService reservationService;
    private final AuthorizationService authorizationService;
    private final StoreService storeService;

    public ManagerReservationController(
            ReservationService reservationService,
            AuthorizationService authorizationService,
            StoreService storeService
    ) {
        this.reservationService = reservationService;
        this.authorizationService = authorizationService;
        this.storeService = storeService;
    }

    @GetMapping
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<List<ReservationDetailDto>> getReservations(
            @LoginMember LoginMemberDto loginMember
    ) {
        Long storeId = storeService.findStoreByMemberId(loginMember.id()).id();
        List<ReservationDetailDto> responseData = reservationService.findAllByStoreId(storeId).stream()
                .map(ReservationDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @PatchMapping("/{id}/cancel")
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<ReservationDetailDto> cancelReservation(
            @PathVariable Long id,
            @LoginMember LoginMemberDto loginMember
    ) {
        authorizationService.validateManagerReservationAccess(loginMember.id(), id);
        ReservationDetailDto responseData = ReservationDetailDto.from(
                reservationService.cancelByAdmin(id));
        return ResponseEntity.ok(responseData);
    }
}
