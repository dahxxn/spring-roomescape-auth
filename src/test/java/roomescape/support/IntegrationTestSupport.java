package roomescape.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.sql.init.data-locations=",
                "spring.datasource.url=jdbc:h2:mem:integrationtestdb"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class IntegrationTestSupport {

    @LocalServerPort
    private int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    private String adminSessionId;
    private String userSessionId;

    private String adminToken;
    private String userToken;

    protected Long storeId;
    protected Long adminId;
    protected Long userId;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        setUpMembers();

        adminSessionId = login("admin", "admin1234");
        userSessionId = login("user01", "user1234");

        adminToken = issueToken("admin", "admin1234");
        userToken = issueToken("user01", "user1234");
    }

    private void setUpMembers() {
        jdbcTemplate.update(
                "INSERT INTO member (name, login_id, password, role) VALUES (?, ?, ?, ?)",
                "관리자", "admin", "admin1234", "ADMIN"
        );
        jdbcTemplate.update(
                "INSERT INTO member (name, login_id, password, role) VALUES (?, ?, ?, ?)",
                "테스트유저", "user01", "user1234", "USER"
        );

        adminId = jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE login_id = ?",
                Long.class,
                "admin"
        );
        userId = jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE login_id = ?",
                Long.class,
                "user01"
        );

        jdbcTemplate.update("INSERT INTO store (name) VALUES (?)", "강남점");

        storeId = jdbcTemplate.queryForObject(
                "SELECT id FROM store WHERE name = ?",
                Long.class,
                "강남점"
        );

        jdbcTemplate.update(
                "INSERT INTO store_admin (member_id, store_id) VALUES (?, ?)",
                adminId,
                storeId
        );
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

    protected io.restassured.specification.RequestSpecification givenAdmin() {
        return RestAssured.given().cookie("JSESSIONID", adminSessionId);
    }

    protected io.restassured.specification.RequestSpecification givenUser() {
        return RestAssured.given().cookie("JSESSIONID", userSessionId);
    }

    private String issueToken(String loginId, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("loginId", loginId, "password", password))
                .when().post("/token")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString("accessToken");
    }

    protected io.restassured.specification.RequestSpecification givenAdminWithToken() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + adminToken);
    }

    protected io.restassured.specification.RequestSpecification givenUserWithToken() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + userToken);
    }

    protected Long createTime(String startAt) {
        return givenAdmin()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "storeId", storeId,
                        "startAt", startAt
                ))
                .when().post("/admin/times")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    protected Long createTheme(String name) {
        return givenAdmin()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "storeId", storeId,
                        "name", name,
                        "description", name + " 설명",
                        "thumbnailUrl", name + " 썸네일"
                ))
                .when().post("/admin/themes")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    protected Long createActiveTheme(String name) {
        Long themeId = createTheme(name);
        givenAdmin()
                .contentType(ContentType.JSON)
                .body(Map.of("isActive", true))
                .when().patch("/admin/themes/{id}", themeId)
                .then()
                .statusCode(200);
        return themeId;
    }

    protected Long createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        return givenUser()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "memberId", userId,
                        "storeId", storeId,
                        "date", date.toString(),
                        "timeId", timeId,
                        "themeId", themeId
                ))
                .when().post("/reservations")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    protected Long savePastReservation(String name, LocalDate date, String startAt, Long themeId) {
        jdbcTemplate.update(
                """
                        INSERT INTO reservation (member_id, store_id, date, start_at, theme_id, status)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                userId, storeId, date, startAt, themeId, "RESERVED"
        );

        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
    }
}
