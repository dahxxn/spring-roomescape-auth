package roomescape.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberJoinDto(

        @NotBlank(message = "이름은 필수 항목입니다.")
        String name,

        @NotBlank(message = "아이디는 필수 항목입니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        String password
) {}
