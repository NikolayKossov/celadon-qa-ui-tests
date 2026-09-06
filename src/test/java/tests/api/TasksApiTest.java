package tests.api;

import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import support.PracticeApi;
import java.io.IOException;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Tag("api")
@Epic("QA automation portfolio")
@Feature("Educational Tasks REST API")
@DisplayName("REST Assured — educational Tasks API")
class TasksApiTest {
    private PracticeApi api;
    private RequestSpecification request;

    @BeforeEach
    void startIsolatedApi() throws IOException {
        api = new PracticeApi();
        request = new RequestSpecBuilder().setBaseUri(api.baseUrl())
                .setContentType(ContentType.JSON).addFilter(new AllureRestAssured()).build();
    }

    @AfterEach
    void stopApi() { if (api != null) api.close(); }

    @Test
    @DisplayName("GET returns an empty JSON collection on a fresh service")
    void emptyCollection() {
        given().spec(request).get("/tasks").then().statusCode(200)
                .contentType(ContentType.JSON).body("size()", is(0));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("POST → GET → PUT → GET → DELETE → GET verifies persisted lifecycle")
    void taskLifecycle() {
        int id = given().spec(request).body(Map.of("title", "Review vacancy"))
                .post("/tasks").then().statusCode(201).contentType(ContentType.JSON)
                .header("Location", matchesPattern("/tasks/[0-9]+"))
                .body("id", greaterThan(0), "title", equalTo("Review vacancy"), "completed", is(false))
                .extract().path("id");
        given().spec(request).get("/tasks/" + id).then().statusCode(200)
                .body("id", is(id), "title", equalTo("Review vacancy"), "completed", is(false));
        given().spec(request).body(Map.of("title", "Review complete", "completed", true))
                .put("/tasks/" + id).then().statusCode(200).body("id", is(id), "completed", is(true));
        given().spec(request).get("/tasks/" + id).then().statusCode(200)
                .body("title", equalTo("Review complete"), "completed", is(true));
        given().spec(request).get("/tasks").then().statusCode(200).body("id", hasItem(id));
        given().spec(request).delete("/tasks/" + id).then().statusCode(204).body(isEmptyString());
        given().spec(request).get("/tasks/" + id).then().statusCode(404).body("error", equalTo("Task not found"));
    }

    @ParameterizedTest(name = "Invalid task payload: {0}")
    @ValueSource(strings = {"{}", "{\"title\":\"   \"}", "{\"title\":42}",
            "{\"title\":\"Task\",\"completed\":\"yes\"}"})
    void rejectsInvalidPayload(String body) {
        given().spec(request).body(body).post("/tasks").then().statusCode(400)
                .contentType(ContentType.JSON).body("error", equalTo("Invalid task"));
        given().spec(request).get("/tasks").then().statusCode(200).body("size()", is(0));
    }

    @Test
    @DisplayName("Malformed JSON returns 400")
    void malformedJson() {
        given().spec(request).body("{broken").post("/tasks").then().statusCode(400)
                .body("error", equalTo("Malformed JSON"));
    }

    @Test
    @DisplayName("Plain text payload returns 415")
    void unsupportedContentType() {
        given().spec(request).contentType(ContentType.TEXT).body("hello").post("/tasks")
                .then().statusCode(415).body("error", equalTo("Expected application/json"));
    }

    @Test
    @DisplayName("Unknown resource returns 404")
    void missingTask() {
        given().spec(request).get("/tasks/999").then().statusCode(404)
                .body("error", equalTo("Task not found"));
    }

    @Test
    @DisplayName("Unsupported method returns 405 with allowed methods")
    void unsupportedMethod() {
        given().spec(request).delete("/tasks").then().statusCode(405)
                .header("Allow", equalTo("GET, POST")).body("error", equalTo("Method not allowed"));
    }
}
