package roomescape.auth.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.request.LoginDto;
import roomescape.auth.dto.response.TokenDto;
import roomescape.auth.token.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@RestController
public class AuthController {
    private static final String SESSION_KEY = "loginMemberId";

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    public AuthController(MemberService memberService, TokenProvider tokenProvider) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginDto request,
            HttpSession session
    ) {
        Member member = memberService.login(request.loginId(), request.password());
        session.setAttribute(SESSION_KEY, member.id());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/token")
    public ResponseEntity<TokenDto> token(
            @Valid @RequestBody LoginDto request
    ) {
        Member member = memberService.login(request.loginId(), request.password());
        String token = tokenProvider.createToken(member.id());
        return ResponseEntity.ok(new TokenDto(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }
}
