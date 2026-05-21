package roomescape.store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMember;
import roomescape.auth.annotation.LoginRequired;
import roomescape.auth.annotation.ManagerRequired;
import roomescape.auth.dto.request.LoginMemberDto;
import roomescape.store.domain.Store;
import roomescape.store.dto.response.StoreDetailDto;
import roomescape.store.service.StoreService;

@RestController
@RequestMapping("/manager/store")
public class ManagerStoreController {
    private final StoreService storeService;

    public ManagerStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<StoreDetailDto> getMyStore(@LoginMember LoginMemberDto loginMember) {
        Store store = storeService.findStoreByMemberId(loginMember.id());
        return ResponseEntity.ok(StoreDetailDto.from(store));
    }
}
