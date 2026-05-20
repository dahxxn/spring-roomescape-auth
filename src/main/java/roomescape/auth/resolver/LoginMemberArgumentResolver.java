package roomescape.auth.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.auth.annotation.LoginMember;
import roomescape.auth.dto.request.LoginMemberDto;
import roomescape.common.exception.UnauthorizedException;
import roomescape.member.service.MemberService;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String SESSION_KEY = "loginMemberId";
    private final MemberService memberService;

    public LoginMemberArgumentResolver(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && parameter.getParameterType().equals(LoginMemberDto.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Long loginMemberId = (Long) webRequest.getAttribute(
                SESSION_KEY, NativeWebRequest.SCOPE_REQUEST);

        if (loginMemberId == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        return memberService.findById(loginMemberId)
                .map(LoginMemberDto::from)
                .orElseThrow(() -> new UnauthorizedException("존재하지 않는 사용자입니다."));
    }
}
