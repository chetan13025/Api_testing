package library;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import DataCreate.DataCreate;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import utils.ConfigReader;

import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import reports.ExtentCucumberListener;
import com.aventstack.extentreports.ExtentTest;

public class LibrariesAPI {
    private String requestBody;
    private Response response;
    private static int LibraryId;
    private static String baseUrl = ConfigReader.getProperty("baseUrl");
    private static String tenantId = ConfigReader.getProperty("tenantId");
    private static String author = ConfigReader.getProperty("author");
    private static String Library_endpoint = ConfigReader.getProperty("library_endpoint");

    private void logToExtent(String action, String reqBody, Response resp) {
        ExtentTest current = ExtentCucumberListener.getCurrentScenario();
        if (current == null) return;

        current.info("Action: " + action);

        if (reqBody != null) {
            current.info("Request Payload:");
            current.info(MarkupHelper.createCodeBlock(reqBody, CodeLanguage.JSON));
        }

        current.info("Response Status Code: " + (resp == null ? "null" : resp.getStatusCode()));
        if (resp != null) {
            current.info("Response Body:");
            current.info(MarkupHelper.createCodeBlock(resp.getBody().asString(), CodeLanguage.JSON));
        }
    }

    @Given("I have a random library payload")
    public void i_have_a_random_library_payload() {
        requestBody = DataCreate.generateLibraryJson();
        System.out.println("Library JSON:\n" + requestBody);
    }

    @When("Create Library Request")
    public void Create_library_Request() {
        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .body(requestBody)
                .when().post(baseUrl + Library_endpoint)
                .then().extract().response();

        System.out.println("Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());
        LibraryId = response.jsonPath().getInt("id");
        System.out.println("Stored Library ID: " + LibraryId);

        logToExtent("Create Library", requestBody, response);
    }

    @When("Fetch All Libraries Details")
    public void FetchAll_Libraries_Details() {
        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .when().get(baseUrl + Library_endpoint)
                .then().extract().response();

        System.out.println("Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        logToExtent("Fetch All Libraries", null, response);
    }

    @When("Fetch Library Details with ID")
    public void Fetch_Lirary_Details_with_ID() {
        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .pathParam("id", LibraryId)
                .when().get(baseUrl + Library_endpoint + "/{id}")
                .then().extract().response();

        System.out.println("Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        logToExtent("Fetch Library by ID", null, response);
    }

    @When("Update Library Request")
    public void Update_library_Request() {
        if (LibraryId == 0) {
            throw new IllegalStateException("No LibraryId available. Run POST scenario first.");
        }

        String newJson = DataCreate.generateLibraryJson();
        String updatePayload = newJson.substring(0, newJson.length() - 1) + ", \"id\": " + LibraryId + "}";

        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .pathParam("id", LibraryId)
                .body(updatePayload)
                .when().put(baseUrl + Library_endpoint + "/{id}")
                .then().extract().response();

        System.out.println("PUT Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        logToExtent("Update Library (PUT)", updatePayload, response);
    }

    @When("Update Patch Library Request")
    public void Update_Patch_Library_Request() {
        if (LibraryId == 0) {
            throw new IllegalStateException("No LibraryId available. Run POST scenario first.");
        }

        String newJson = DataCreate.generateLibraryJson();
        String LibraryName = newJson.split("\"libraryName\"\\s*:\\s*\"")[1].split("\"")[0];
        String patchPayload = "{ \"libraryName\": \"" + LibraryName + "\" }";

        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .pathParam("id", LibraryId)
                .body(patchPayload)
                .when().patch(baseUrl + Library_endpoint + "/{id}")
                .then().extract().response();

        System.out.println("PATCH Request Body:\n" + patchPayload);
        System.out.println("PATCH Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        logToExtent("Update Library (PATCH)", patchPayload, response);
    }

    @When("Delete Library with ID")
    public void delete_Library_with_ID() {
        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .pathParam("id", LibraryId)
                .when().delete(baseUrl + Library_endpoint + "/{id}")
                .then().extract().response();

        System.out.println("Delete Response Body:\n" + response.getBody().asString());
        System.out.println("Delete Status Code: " + response.getStatusCode());

        logToExtent("Delete Library", null, response);
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertEquals(statusCode.intValue(), response.getStatusCode());
    }

    @Then("the response should contain {string}")
    public void the_response_should_contain(String key) {
        assertTrue("Response does not contain key: " + key, response.getBody().asString().contains(key));
    }
}
