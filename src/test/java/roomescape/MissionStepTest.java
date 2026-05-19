package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.controller.AdminReservationController;
import roomescape.reservation.domain.Reservation;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.sql.init.data-locations=",
                "spring.datasource.url=jdbc:h2:mem:missiondb"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    private String adminSessionId;
    private String userSessionId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update(
                "INSERT INTO member (name, login_id, password, role) VALUES (?, ?, ?, ?)",
                "관리자", "admin", "admin1234", "ADMIN"
        );
        jdbcTemplate.update(
                "INSERT INTO member (name, login_id, password, role) VALUES (?, ?, ?, ?)",
                "테스트유저", "user01", "user1234", "USER"
        );
        adminSessionId = login("admin", "admin1234");
        userSessionId = login("user01", "user1234");
    }

    private String login(String loginId, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("loginId", loginId, "password", password))
                .when().post("/login")
                .then()
                .statusCode(200)
                .extract().cookie("JSESSIONID");
    }

    @Test
    @DisplayName("예약 조회")
    void 예약_조회() {
        RestAssured.given()
                .cookie("JSESSIONID", adminSessionId)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("데이터베이스 연동")
    void 데이터베이스_연동() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("MISSIONDB");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("DB 조회 API 전환")
    void DB_조회_API_전환() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "15:40");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테마1", "테마1 설명", "테마1 썸네일");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, start_at, theme_id, status) VALUES (?, ?, ?, ?, ?)",
                "테스트유저", "2099-01-01", "15:40", 1, "RESERVED");

        List<Reservation> reservations = RestAssured.given()
                .cookie("JSESSIONID", userSessionId)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", Reservation.class);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(1) from reservation WHERE name = ?",
                Integer.class, "테스트유저");

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    @DisplayName("시간 관리 API")
    void 시간_관리_API() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given()
                .cookie("JSESSIONID", adminSessionId)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given()
                .cookie("JSESSIONID", adminSessionId)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given()
                .cookie("JSESSIONID", adminSessionId)
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("예약과 시간 연결")
    void 예약과_시간_연결() {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", "10:00");
        RestAssured.given()
                .cookie("JSESSIONID", adminSessionId)
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> theme = new HashMap<>();
        theme.put("name", "테마1");
        theme.put("description", "테마1 설명");
        theme.put("thumbnailUrl", "테마1 썸네일");
        RestAssured.given()
                .cookie("JSESSIONID", adminSessionId)
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("date", LocalDate.now().plusWeeks(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given()
                .cookie("JSESSIONID", userSessionId)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given()
                .cookie("JSESSIONID", userSessionId)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Autowired
    private AdminReservationController adminReservationController;

    @Test
    @DisplayName("계층화 리팩터링")
    void 계층화_리팩터링() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : adminReservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
