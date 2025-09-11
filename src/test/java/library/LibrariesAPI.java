package library;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

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
    private void captureDbSnapshot(int bookId, String action) {
        ExtentTest current = ExtentCucumberListener.getCurrentScenario();
        if (current == null) return;

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://10.10.2.45:3306/library_model_dhin", "root", "dhi123");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `library_model_dhin`.`library` LIMIT 1000")) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            StringBuilder html = new StringBuilder("<table border='1' style='border-collapse:collapse;'>");

            // Table header
            html.append("<tr style='background-color:#f2f2f2;'>");
            for (int i = 1; i <= colCount; i++) {
                html.append("<th>").append(meta.getColumnName(i)).append("</th>");
            }
            html.append("</tr>");

            // Table rows
            while (rs.next()) {
                int currentId;
                try {
                    currentId = rs.getInt("LibraryId"); // ✅ use bookId if exists
                } catch (Exception e) {
                    currentId = rs.getInt("library_Id"); // fallback
                }

                if (currentId == LibraryId) {
                    html.append("<tr style='background-color:#90EE90; font-weight:bold;'>"); // highlight
                } else {
                    html.append("<tr>");
                }

                for (int i = 1; i <= colCount; i++) {
                    html.append("<td>").append(rs.getString(i)).append("</td>");
                }
                html.append("</tr>");
            }

            html.append("</table>");

            // ✅ Attach HTML table into Extent report
            current.info("Database Snapshot after: " + action);
            current.info(html.toString());

        } catch (Exception e) {
            current.warning("Failed to capture DB snapshot: " + e.getMessage());
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
        captureDbSnapshot(LibraryId, "Create Library");
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
        captureDbSnapshot(LibraryId, "Update Library (PUT)");
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
        captureDbSnapshot(LibraryId, "Update Library (PATCH)");
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
        captureDbSnapshot(LibraryId, "Delete Library");
    }

    @Then("The response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertEquals(statusCode.intValue(), response.getStatusCode());
    }

    @Then("The response should contain {string}")
    public void the_response_should_contain(String key) {
        assertTrue("Response does not contain key: " + key, response.getBody().asString().contains(key));
    }
}
