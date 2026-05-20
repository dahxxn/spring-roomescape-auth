package roomescape.member;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.IntegrationTestSupport;

class MemberApiTest extends IntegrationTestSupport {

    @Test
    @DisplayName("회원은 가입할 수 있다")
    void join() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "새로운유저",
                        "loginId", "newUser",
                        "password", "newPassword"
                ))
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("중복된 아이디로 회원 가입할 수 없다")
    void cannotJoinWithDuplicateLoginId() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "중복유저",
                        "loginId", "user01",
                        "password", "password"
                ))
                .when().post("/members")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("회원 가입 시 필수값이 없으면 가입할 수 없다")
    void cannotJoinWithInvalidInput() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "",
                        "loginId", "newUser",
                        "password", "newPassword"
                ))
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("세션으로 로그인한 사용자는 내 정보를 조회할 수 있다")
    void findMeWithSession() {
        givenUser().log().all()
                .when().get("/members/me")
                .then().log().all()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", is("테스트유저"))
                .body("role", is("USER"));
    }

    @Test
    @DisplayName("토큰으로 로그인한 사용자는 내 정보를 조회할 수 있다")
    void findMeWithToken() {
        givenUserWithToken().log().all()
                .when().get("/members/me")
                .then().log().all()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", is("테스트유저"))
                .body("role", is("USER"));
    }

    @Test
    @DisplayName("인증 정보가 없으면 내 정보를 조회할 수 없다")
    void cannotFindMeWithoutAuthentication() {
        RestAssured.given().log().all()
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401)
                .body("message", is("로그인이 필요합니다."));
    }
}
