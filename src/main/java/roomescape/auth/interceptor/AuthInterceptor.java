package roomescape.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.annotation.AdminRequired;
import roomescape.auth.annotation.LoginRequired;
import roomescape.common.dto.ErrorDetailDto;
import roomescape.member.service.MemberService;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String SESSION_KEY = "loginMemberId";

    private final ObjectMapper objectMapper;
    private final MemberService memberService;

    public AuthInterceptor(ObjectMapper objectMapper, MemberService memberService) {
        this.objectMapper = objectMapper;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        boolean loginRequired = method.hasMethodAnnotation(LoginRequired.class);
        boolean adminRequired = method.hasMethodAnnotation(AdminRequired.class);

        if (!loginRequired && !adminRequired) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Long loginMemberId = (session != null)
                ? (Long) session.getAttribute(SESSION_KEY)
                : null;

        if (loginMemberId == null) {
            writeUnauthorized(response);
            return false;
        }

        if (adminRequired && !isAdmin(loginMemberId)) {
            writeForbidden(response);
            return false;
        }

        request.setAttribute(SESSION_KEY, loginMemberId);
        return true;
    }

    private boolean isAdmin(Long memberId) {
        return memberService.findById(memberId)
                .map(member -> member.role().name().equals("ADMIN"))
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
