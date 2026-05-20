package roomescape.auth;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.IntegrationTestSupport;

class AuthApiTest extends IntegrationTestSupport {

    @Test
    @DisplayName("모바일 사용자는 로그인 후 토큰을 발급받을 수 있다")
    void issueToken() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "loginId", "user01",
                        "password", "user1234"
                ))
                .when().post("/token")
                .then().log().all()
                .statusCode(200)
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("잘못된 로그인 정보로 토큰을 발급받을 수 없다")
    void cannotIssueTokenWithInvalidPassword() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "loginId", "user01",
                        "password", "wrong-password"
                ))
                .when().post("/token")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 인증에 실패한다")
    void cannotAccessWithInvalidToken() {
        RestAssured.given().log().all()
                .header("Authorization", "Bearer invalid-token")
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401)
                .body("message", is("유효하지 않은 토큰입니다."));
    }

    @Test
    @DisplayName("일반 사용자 토큰으로 관리자 API에 접근할 수 없다")
    void cannotAccessAdminApiWithUserToken() {
        givenUserWithToken().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(403)
                .body("message", is("권한이 없습니다."));
    }

    @Test
    @DisplayName("관리자 토큰으로 관리자 API에 접근할 수 있다")
    void accessAdminApiWithAdminToken() {
        givenAdminWithToken().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이 있으면 정상 세션이 있어도 인증에 실패한다")
    void invalidTokenDoesNotFallbackToSession() {
        givenUser().log().all()
                .header("Authorization", "Bearer invalid-token")
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401)
                .body("message", is("유효하지 않은 토큰입니다."));
    }
}
