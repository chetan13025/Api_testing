package library;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import DataCreate.DataCreateL;
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
    private void captureElasticScreenshot(int LibraryId, String action) {
        WebDriver driver = null;
        try {
            // Launch Chrome
            driver = new ChromeDriver();
            driver.manage().window().maximize();

            // Open Elasticsearch _search URL
     String url = "http://10.10.2.81:9200/dhin_library-model_library_index/_search";      
            driver.get(url);

            // Small wait for JSON to load
            Thread.sleep(2000);
           
                WebElement prettyPrint = driver.findElement(By.xpath("//div[@class='json-formatter-container']"));
                if (!prettyPrint.isSelected()) {
                    prettyPrint.click();
                }  

            // Use browser search to highlight BookId (like Ctrl+F → search BookId)
            JavascriptExecutor js = (JavascriptExecutor) driver;
//            js.executeScript(
//                "window.find(arguments[0], false, false, true, false, true, false);", 
//                String.valueOf(bookId)
//            );
            js.executeScript("window.find('" + LibraryId + "');");

            // Screenshot
//            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//            String path = "target/screenshots/es_" + LibraryId + "_" + action.replace(" ", "_") + ".png";
//            Files.createDirectories(Paths.get("target/screenshots/"));
//            File destFile = new File(path);
//            org.openqa.selenium.io.FileHandler.copy(srcFile, destFile);
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

            // Attach to Extent
            ExtentTest current = ExtentCucumberListener.getCurrentScenario();
            if (current != null) {
                current.info("Elasticsearch Verification for BookId: " + LibraryId);
//                current.addScreenCaptureFromPath(destFile.getAbsolutePath());
                current.addScreenCaptureFromBase64String(base64Screenshot,
                        "ElasticSearch_" + action.replace(" ", "_"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Given("I have a random library payload")
    public void i_have_a_random_library_payload() {
        requestBody = DataCreateL.generateLibraryJson();
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
        captureElasticScreenshot(LibraryId, "Create Library");
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

        String newJson = DataCreateL.generateLibraryJson();
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
//        captureElasticScreenshot(LibraryId, "Create Update Library (PUT)");
    }

    @When("Update Patch Library Request")
    public void Update_Patch_Library_Request() {
        if (LibraryId == 0) {
            throw new IllegalStateException("No LibraryId available. Run POST scenario first.");
        }

        String newJson = DataCreateL.generateLibraryJson();
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
//        captureElasticScreenshot(LibraryId, "Create Update Library (PATCH)");
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