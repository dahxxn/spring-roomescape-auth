package roomescape.auth.dto;

import roomescape.member.domain.Member;

public record LoginMemberDto(Long id, String name) {
    public static LoginMemberDto from(Member member) {
        return new LoginMemberDto(member.id(), member.name());
    }
}
