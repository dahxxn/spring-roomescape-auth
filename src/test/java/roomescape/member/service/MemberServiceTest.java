package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입한다.")
    void join() {
        // given & when
        Member actual = memberService.join("한다", "handa", "password");

        // then
        assertThat(actual.id()).isNotNull();
        assertThat(actual.name()).isEqualTo("한다");
        assertThat(actual.loginId()).isEqualTo("handa");
        assertThat(actual.password()).isEqualTo("password");
        assertThat(actual.role()).isEqualTo(MemberRole.USER);
    }

    @Test
    @DisplayName("중복된 로그인 아이디로 가입 시 예외가 발생한다.")
    void join_duplicate_login_id() {
        // given
        memberService.join("한다", "handa", "password");

        // when & then
        assertThatThrownBy(() -> memberService.join("다른회원", "handa", "password"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("로그인 아이디로 회원을 조회한다.")
    void findByLoginId() {
        // given
        Member saved = memberService.join("한다", "handa", "password");

        // when
        Member actual = memberService.findByLoginId("handa");

        // then
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    @DisplayName("존재하지 않는 로그인 아이디로 회원 조회 시 예외가 발생한다.")
    void findByLoginId_not_exist() {
        // given
        String wrongLoginId = "wrong";

        // when & then
        assertThatThrownBy(() -> memberService.findByLoginId(wrongLoginId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("회원 id로 회원을 조회한다.")
    void findById() {
        // given
        Member saved = memberService.join("한다", "handa", "password");

        // when
        Member actual = memberService.findById(saved.id()).get();

        // then
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    @DisplayName("로그인 정보가 일치하면 회원을 반환한다.")
    void login() {
        // given
        Member saved = memberService.join("한다", "handa", "password");

        // when
        Member actual = memberService.login("handa", "password");

        // then
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인 시 예외가 발생한다.")
    void login_wrong_password() {
        // given
        memberService.join("한다", "handa", "password");

        // when & then
        assertThatThrownBy(() -> memberService.login("handa", "wrong-password"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("존재하지 않는 로그인 아이디로 로그인 시 예외가 발생한다.")
    void login_not_exist_login_id() {
        // given
        String wrongLoginId = "wrong";

        // when & then
        assertThatThrownBy(() -> memberService.login(wrongLoginId, "password"))
                .isInstanceOf(NotFoundException.class);
    }
}
