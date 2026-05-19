package roomescape.member.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.request.MemberJoinDto;
import roomescape.member.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<Void> join(@Valid @RequestBody MemberJoinDto dto) {
        memberService.join(dto.name(), dto.loginId(), dto.password());
        return ResponseEntity.status(CREATED).build();
    }
}
