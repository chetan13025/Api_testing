package library;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import utils.ConfigReader;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import reports.ExtentCucumberListener;
import com.aventstack.extentreports.ExtentTest;

import static io.restassured.RestAssured.*;
import static org.junit.Assert.*;

import DataCreate.DataCreate;

public class BookAPI {

    private Response response;
    private String requestBody;
    private static int BookId;
    private static String baseUrl = ConfigReader.getProperty("baseUrl");
    private static String tenantId = ConfigReader.getProperty("tenantId");
    private static String author = ConfigReader.getProperty("author");
    private static String Book_endpoint = ConfigReader.getProperty("book_endpoint");

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

    @Given("I have a random book payload")
    public void i_have_a_random_book_payload() {
        requestBody = DataCreate.generateBookJson();
        System.out.println("Book JSON:\n" + requestBody);
    }

    @When("Create Book Request")
    public void Create_Book_Request() {
        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .body(requestBody)
                .when().post(baseUrl + Book_endpoint)
                .then().extract().response();

        System.out.println("Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());
        BookId = response.jsonPath().getInt("id");
        System.out.println("Stored Book ID: " + BookId);

        logToExtent("Create Book", requestBody, response);
    }

    @When("Fetch All Books Details")
    public void FetchAll_Books_Details() {
        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .when().get(baseUrl + Book_endpoint)
                .then().extract().response();

        System.out.println("Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        logToExtent("Fetch All Books", null, response);
    }

    @When("Fetch Book Details with ID")
    public void Fetch_Book_Details_with_ID() {
        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .pathParam("id", BookId)
                .when().get(baseUrl + Book_endpoint + "/{id}")
                .then().extract().response();

        System.out.println("Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        logToExtent("Fetch Book by ID", null, response);
    }

    @When("Update Book Request")
    public void Update_Book_Request() {
        if (BookId == 0) {
            throw new IllegalStateException("No BookId available. Run POST scenario first.");
        }

        String newJson = DataCreate.generateBookJson();
        String updatePayload = newJson.substring(0, newJson.length() - 1) + ", \"id\": " + BookId + "}";

        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .pathParam("id", BookId)
                .body(updatePayload)
                .when().put(baseUrl + Book_endpoint + "/{id}")
                .then().extract().response();

        System.out.println("PUT Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        logToExtent("Update Book (PUT)", updatePayload, response);
    }

    @When("Update Patch Book Request")
    public void Update_Patch_Book_Request() {
        if (BookId == 0) {
            throw new IllegalStateException("No BookId available. Run POST scenario first.");
        }

        String newJson = DataCreate.generateBookJson();
        String title = newJson.split("\"title\"\\s*:\\s*\"")[1].split("\"")[0];
        String patchPayload = "{ \"title\": \"" + title + "\" }";

        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .pathParam("id", BookId)
                .body(patchPayload)
                .when().patch(baseUrl + Book_endpoint + "/{id}")
                .then().extract().response();

        System.out.println("PATCH Request Body:\n" + patchPayload);
        System.out.println("PATCH Response Body:\n" + response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());

        logToExtent("Update Book (PATCH)", patchPayload, response);
    }

    @When("Delete Book with ID")
    public void delete_Book_with_ID() {
        response = given().header("Content-Type", "application/json")
                .header("X-TENANT-ID", tenantId)
                .header("Author", author)
                .pathParam("id", BookId)
                .when().delete(baseUrl + Book_endpoint + "/{id}")
                .then().extract().response();

        System.out.println("Delete Response Body:\n" + response.getBody().asString());
        System.out.println("Delete Status Code: " + response.getStatusCode());

        logToExtent("Delete Book", null, response);
    }

    @Then("the Book response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertEquals(statusCode.intValue(), response.getStatusCode());
    }

    @Then("the Book response should contain {string}")
    public void the_response_should_contain(String key) {
        assertTrue("Response does not contain key: " + key, response.getBody().asString().contains(key));
    }
}
