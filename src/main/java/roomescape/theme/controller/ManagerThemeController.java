package roomescape.theme.controller;

import static org.springframework.http.HttpStatus.CREATED;

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
import roomescape.auth.annotation.ManagerRequired;
import roomescape.auth.dto.request.LoginMemberDto;
import roomescape.auth.service.AuthorizationService;
import roomescape.theme.dto.request.ThemeActiveUpdateDto;
import roomescape.theme.dto.request.ThemeSaveDto;
import roomescape.theme.dto.response.ThemeDetailDto;
import roomescape.theme.service.ThemeService;

@RestController
@RequestMapping("/manager/themes")
public class ManagerThemeController {
    private final ThemeService themeService;
    private final AuthorizationService authorizationService;

    public ManagerThemeController(
            ThemeService themeService,
            AuthorizationService authorizationService
    ) {
        this.themeService = themeService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<List<ThemeDetailDto>> getThemes(
            @LoginMember LoginMemberDto loginMember
    ) {
        List<ThemeDetailDto> responseData = themeService.findThemesByMemberId(loginMember.id()).stream()
                .map(ThemeDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @PostMapping
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<ThemeDetailDto> createTheme(
            @LoginMember LoginMemberDto loginMember,
            @Valid @RequestBody ThemeSaveDto dto
    ) {
        authorizationService.validateManagerStoreAccess(loginMember.id(), dto.storeId());
        ThemeDetailDto responseData = ThemeDetailDto.from(
                themeService.register(dto.storeId(), dto.name(), dto.description(), dto.thumbnailUrl()));
        return ResponseEntity.status(CREATED).body(responseData);
    }

    @PatchMapping("/{id}")
    @LoginRequired
    @ManagerRequired
    public ResponseEntity<ThemeDetailDto> updateThemeStatus(
            @LoginMember LoginMemberDto loginMember,
            @PathVariable Long id,
            @Valid @RequestBody ThemeActiveUpdateDto dto
    ) {
        authorizationService.validateManagerThemeAccess(loginMember.id(), id);
        ThemeDetailDto responseData = ThemeDetailDto.from(
                themeService.updateStatus(id, dto.isActive()));
        return ResponseEntity.ok(responseData);
    }
}
