package roomescape.store.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.store.dto.response.StoreDetailDto;
import roomescape.store.service.StoreService;

@RestController
@RequestMapping("/stores")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public ResponseEntity<List<StoreDetailDto>> getStores() {
        List<StoreDetailDto> responseData = storeService.findAll().stream()
                .map(StoreDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }
}
