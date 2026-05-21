package roomescape.closeddate.controller;

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
import roomescape.closeddate.dto.request.ClosedDateSaveDto;
import roomescape.closeddate.dto.response.ClosedDateDetailDto;
import roomescape.closeddate.service.ClosedDateService;

@RestController
@RequestMapping("/manager/closed-dates")
public class ManagerClosedDateController {
    private final ClosedDateService closedDateService;
    private final AuthorizationService authorizationService;

    public ManagerClosedDateController(
            ClosedDateService closedDateService,
            AuthorizationService authorizationService
    ) {
        this.closedDateService = closedDateService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<List<ClosedDateDetailDto>> getClosedDates(
            @LoginMember LoginMemberDto loginMember
    ) {
        List<ClosedDateDetailDto> responseData = closedDateService.findClosedDatesByMemberId(loginMember.id()).stream()
                .map(ClosedDateDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @PostMapping
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<ClosedDateDetailDto> createClosedDate(
            @LoginMember LoginMemberDto loginMember,
            @Valid @RequestBody ClosedDateSaveDto dto
    ) {
        authorizationService.validateManagerStoreAccess(loginMember.id(), dto.storeId());
        ClosedDateDetailDto responseData = ClosedDateDetailDto.from(
                closedDateService.register(dto.storeId(), dto.date()));
        return ResponseEntity.status(CREATED).body(responseData);
    }

    @DeleteMapping("/{id}")
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<Void> deleteClosedDate(
            @LoginMember LoginMemberDto loginMember,
            @PathVariable Long id
    ) {
        authorizationService.validateManagerClosedDateAccess(loginMember.id(), id);
        closedDateService.deregister(id);
        return ResponseEntity.noContent().build();
    }
}
