package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainValidationException;

class MemberTest {

    private final Member member = Member.load(1L, "한다", "handa", "password", MemberRole.USER);

    @Test
    @DisplayName("회원 id를 가져온다.")
    void getId() {
        // given
        Long expected = 1L;

        // when
        Long actual = member.id();

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("회원 이름을 가져온다.")
    void getName() {
        // given
        String expected = "한다";

        // when
        String actual = member.name();

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("회원 로그인 아이디를 가져온다.")
    void getLoginId() {
        // given
        String expected = "handa";

        // when
        String actual = member.loginId();

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("회원 비밀번호를 가져온다.")
    void getPassword() {
        // given
        String expected = "password";

        // when
        String actual = member.password();

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("회원 권한을 가져온다.")
    void getRole() {
        // given
        MemberRole expected = MemberRole.USER;

        // when
        MemberRole actual = member.role();

        // then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("두 회원 객체의 동등성을 비교한다.")
    void equals() {
        // given & when
        Member otherMember = Member.load(1L, "한다", "handa", "password", MemberRole.USER);

        // then
        assertThat(member)
                .usingRecursiveComparison()
                .isEqualTo(otherMember);
    }

    @Test
    @DisplayName("아직 DB에 추가되지 않은 회원은 id가 없다.")
    void unpersist_member_null_id() {
        // given & when
        Member unpersistMember = Member.create("한다", "handa", "password");

        // then
        assertThat(unpersistMember.id())
                .isNull();
    }

    @Test
    @DisplayName("회원 생성 시 기본 권한은 USER이다.")
    void create_default_user_role() {
        // given & when
        Member createdMember = Member.create("한다", "handa", "password");

        // then
        assertThat(createdMember.role()).isEqualTo(MemberRole.USER);
    }

    @Test
    @DisplayName("회원 이름이 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateName() {
        // given
        String nullName = null;
        String emptyName = "";

        // when & then
        assertThatThrownBy(() -> Member.create(nullName, "handa", "password"))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> Member.create(emptyName, "handa", "password"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("회원 로그인 아이디가 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validateLoginId() {
        // given
        String nullLoginId = null;
        String emptyLoginId = "";

        // when & then
        assertThatThrownBy(() -> Member.create("한다", nullLoginId, "password"))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> Member.create("한다", emptyLoginId, "password"))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    @DisplayName("회원 비밀번호가 유효하지 않은 경우 생성 시 예외가 발생한다.")
    void validatePassword() {
        // given
        String nullPassword = null;
        String emptyPassword = "";

        // when & then
        assertThatThrownBy(() -> Member.create("한다", "handa", nullPassword))
                .isInstanceOf(DomainValidationException.class);

        assertThatThrownBy(() -> Member.create("한다", "handa", emptyPassword))
                .isInstanceOf(DomainValidationException.class);
    }
}
