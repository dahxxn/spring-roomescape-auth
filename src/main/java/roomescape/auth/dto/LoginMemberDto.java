package roomescape.auth.dto;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record LoginMemberDto(
        Long id,
        String name,
        MemberRole role
) {
    public static LoginMemberDto from(Member member) {
        return new LoginMemberDto(member.id(), member.name(), member.role());
    }
}
