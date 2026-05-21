package roomescape.reservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.support.IntegrationTestSupport;

class ManagerReservationApiTest extends IntegrationTestSupport {

    @Test
    @DisplayName("매니저는 자기 매장의 예약 목록을 조회할 수 있다")
    void findMyStoreReservations() {
        Long timeId = createTime("10:00");
        Long themeId = createActiveTheme("테마1");
        LocalDate date = LocalDate.now().plusDays(1);
        createReservation("테스트유저", date, timeId, themeId);

        givenManager().log().all()
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    @DisplayName("매니저는 자기 매장의 예약을 취소할 수 있다")
    void cancelMyStoreReservation() {
        Long timeId = createTime("10:00");
        Long themeId = createActiveTheme("테마1");
        LocalDate date = LocalDate.now().plusDays(1);
        Long reservationId = createReservation("테스트유저", date, timeId, themeId);

        givenManager().log().all()
                .when().patch("/manager/reservations/{id}/cancel", reservationId)
                .then().log().all()
                .statusCode(200)
                .body("status", is("CANCELED"));
    }

    @Test
    @DisplayName("일반 사용자는 매니저 API에 접근할 수 없다")
    void userCannotAccessManagerApi() {
        givenUser().log().all()
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("로그인하지 않으면 매니저 API에 접근할 수 없다")
    void unauthenticatedCannotAccessManagerApi() {
        RestAssured.given().log().all()
                .when().get("/manager/reservations")
                .then().log().all()
                .statusCode(401);
    }
}
