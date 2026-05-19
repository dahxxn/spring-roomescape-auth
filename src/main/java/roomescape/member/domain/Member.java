package roomescape.member.domain;

import roomescape.common.exception.DomainValidationException;

public class Member {
    private final Long id;
    private final String name;
    private final String loginId;
    private final String password;
    private final MemberRole role;

    private Member(Long id, String name, String loginId, String password, MemberRole role) {
        validate(name, loginId, password);
        this.id = id;
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.role = role;
    }

    public static Member create(String name, String loginId, String password) {
        return new Member(null, name, loginId, password, MemberRole.USER);
    }

    public static Member load(Long id, String name, String loginId, String password, MemberRole role) {
        return new Member(id, name, loginId, password, role);
    }

    private static void validate(String name, String loginId, String password) {
        if (name == null || name.isBlank()) throw new DomainValidationException("이름은 필수입니다.");
        if (loginId == null || loginId.isBlank()) throw new DomainValidationException("아이디는 필수입니다.");
        if (password == null || password.isBlank()) throw new DomainValidationException("비밀번호는 필수입니다.");
    }

    public Long id() { return id; }
    public String name() { return name; }
    public String loginId() { return loginId; }
    public String password() { return password; }
    public MemberRole role() { return role; }
}
