package roomescape.reservation.controller;

import static org.springframework.http.HttpStatus.CREATED;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMember;
import roomescape.auth.annotation.LoginRequired;
import roomescape.auth.dto.request.LoginMemberDto;
import roomescape.reservation.dto.request.ReservationCreateDto;
import roomescape.reservation.dto.request.ReservationUpdateDto;
import roomescape.reservation.dto.response.ReservationDetailDto;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @LoginRequired
    @Operation(summary = "Create a reservation", description = "예약을 생성하는 api")
    public ResponseEntity<ReservationDetailDto> createReservation(
            @LoginMember LoginMemberDto loginMember,
            @Valid @RequestBody ReservationCreateDto dto
    ) {
        ReservationDetailDto responseData = ReservationDetailDto.from(
                reservationService.create(loginMember.id(), dto.storeId(), dto.date(), dto.timeId(), dto.themeId()));
        return ResponseEntity.status(CREATED).body(responseData);
    }

    @GetMapping
    @LoginRequired
    @Operation(summary = "Read my reservations", description = "내 예약 목록을 조회하는 api")
    public ResponseEntity<List<ReservationDetailDto>> getMyReservations(
            @LoginMember LoginMemberDto loginMember
    ) {
        List<ReservationDetailDto> responseData = reservationService.findAllByName(loginMember.name()).stream()
                .map(ReservationDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @PatchMapping("/{id}/cancel")
    @LoginRequired
    @Operation(summary = "Cancel a reservation", description = "예약을 취소하는 api")
    public ResponseEntity<ReservationDetailDto> cancelReservation(
            @PathVariable Long id,
            @LoginMember LoginMemberDto loginMember
    ) {
        ReservationDetailDto responseData = ReservationDetailDto.from(
                reservationService.cancel(id, loginMember.name()));
        return ResponseEntity.ok(responseData);
    }

    @PatchMapping("/{id}")
    @LoginRequired
    @Operation(summary = "Update a reservation", description = "예약 날짜/시간을 변경하는 api")
    public ResponseEntity<ReservationDetailDto> updateReservation(
            @PathVariable Long id,
            @LoginMember LoginMemberDto loginMember,
            @Valid @RequestBody ReservationUpdateDto dto
    ) {
        ReservationDetailDto responseData = ReservationDetailDto.from(
                reservationService.change(id, loginMember.name(), dto.date(), dto.timeId()));
        return ResponseEntity.ok(responseData);
    }
}
