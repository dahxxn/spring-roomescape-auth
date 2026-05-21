package roomescape.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.annotation.AdminRequired;
import roomescape.auth.annotation.LoginRequired;
import roomescape.auth.annotation.ManagerRequired;
import roomescape.auth.token.TokenProvider;
import roomescape.common.dto.ErrorDetailDto;
import roomescape.member.domain.MemberRole;
import roomescape.member.service.MemberService;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String LOGIN_MEMBER_ID_KEY = "loginMemberId";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final MemberService memberService;

    public AuthInterceptor(
            TokenProvider tokenProvider,
            ObjectMapper objectMapper,
            MemberService memberService
    ) {
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws IOException {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        boolean loginRequired = method.hasMethodAnnotation(LoginRequired.class);
        boolean adminRequired = method.hasMethodAnnotation(AdminRequired.class);
        boolean managerRequired = method.hasMethodAnnotation(ManagerRequired.class);

        if (!loginRequired && !adminRequired && !managerRequired) {
            return true;
        }

        Long loginMemberId = resolveMemberId(request);

        if (loginMemberId == null) {
            writeUnauthorized(response);
            return false;
        }

        if (adminRequired && !isAdmin(loginMemberId)) {
            writeForbidden(response);
            return false;
        }

        if (managerRequired && !isManager(loginMemberId)) {
            writeForbidden(response);
            return false;
        }

        request.setAttribute(LOGIN_MEMBER_ID_KEY, loginMemberId);
        return true;
    }

    private Long resolveMemberId(HttpServletRequest request) {
        Long memberIdFromToken = resolveMemberIdFromToken(request);

        if (memberIdFromToken != null) {
            return memberIdFromToken;
        }

        return resolveMemberIdFromSession(request);
    }

    private Long resolveMemberIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        return tokenProvider.extractMemberId(token);
    }

    private Long resolveMemberIdFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute(LOGIN_MEMBER_ID_KEY);
    }

    private boolean isManager(Long memberId) {
        return memberService.findById(memberId)
                .map(member -> member.role() == MemberRole.MANAGER)
                .orElse(false);
    }

    private boolean isAdmin(Long memberId) {
        return memberService.findById(memberId)
                .map(member -> member.role() == MemberRole.ADMIN)
                .orElse(false);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorDetailDto error = ErrorDetailDto.of(401, "로그인이 필요합니다.");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    private void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorDetailDto error = ErrorDetailDto.of(403, "권한이 없습니다.");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
