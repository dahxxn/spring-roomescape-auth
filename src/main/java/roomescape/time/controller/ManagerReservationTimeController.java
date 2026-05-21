package roomescape.time.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMember;
import roomescape.auth.annotation.LoginRequired;
import roomescape.auth.annotation.ManagerRequired;
import roomescape.auth.dto.request.LoginMemberDto;
import roomescape.auth.service.AuthorizationService;
import roomescape.time.dto.request.ReservationTimeSaveDto;
import roomescape.time.dto.response.ReservationTimeDetailDto;
import roomescape.time.service.ReservationTimeService;

@RestController
@RequestMapping("/manager/times")
public class ManagerReservationTimeController {
    private final ReservationTimeService reservationTimeService;
    private final AuthorizationService authorizationService;

    public ManagerReservationTimeController(
            ReservationTimeService reservationTimeService,
            AuthorizationService authorizationService
    ) {
        this.reservationTimeService = reservationTimeService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<List<ReservationTimeDetailDto>> getReservationTimes(
            @LoginMember LoginMemberDto loginMember
    ) {
        List<ReservationTimeDetailDto> responseData = reservationTimeService.findAllByMemberId(loginMember.id())
                .stream()
                .map(ReservationTimeDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @PostMapping
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<ReservationTimeDetailDto> createReservationTime(
            @LoginMember LoginMemberDto loginMember,
            @Valid @RequestBody ReservationTimeSaveDto dto
    ) {
        authorizationService.validateManagerStoreAccess(loginMember.id(), dto.storeId());
        ReservationTimeDetailDto responseData = ReservationTimeDetailDto.from(
                reservationTimeService.create(dto.storeId(), dto.startAt()));
        return ResponseEntity.status(CREATED).body(responseData);
    }

    @DeleteMapping("/{id}")
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<Void> deleteReservationTime(
            @LoginMember LoginMemberDto loginMember,
            @PathVariable Long id
    ) {
        authorizationService.validateManagerReservationTimeAccess(loginMember.id(), id);
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
