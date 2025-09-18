package library;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import utils.ConfigReader;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import reports.ExtentCucumberListener;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;

import static io.restassured.RestAssured.*;
import static org.junit.Assert.*;

import DataCreate.DataCreate;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import utils.ConfigReader;
import javax.imageio.ImageIO;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

public class BookAPI {

    private Response response;
    private String requestBody;
    private static int BookId;
    private static String baseUrl = ConfigReader.getProperty("baseUrl");
    private static String tenantId = ConfigReader.getProperty("tenantId");
    private static String author = ConfigReader.getProperty("author");
    private static String Book_endpoint = ConfigReader.getProperty("book_endpoint");
//    private static String URL = ConfigReader.getProperty("url");    

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

    // ✅ Log DB snapshot after POST/PUT/PATCH (highlight the new BookId)
    private void captureDbSnapshot(int bookId, String action) {
        ExtentTest current = ExtentCucumberListener.getCurrentScenario();
        if (current == null) return;

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://10.10.2.45:3306/library_model_dhin", "root", "dhi123");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `library_model_dhin`.`book` LIMIT 1000")) {

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
                    currentId = rs.getInt("bookId"); // ✅ use bookId if exists
                } catch (Exception e) {
                    currentId = rs.getInt("book_Id"); // fallback
                }

                if (currentId == bookId) {
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
            current.warning("⚠️ Failed to capture DB snapshot: " + e.getMessage());
        }
    }
    private void captureElasticScreenshot(int bookId, String action) {
        WebDriver driver = null;
        try {
            // Launch Chrome
            driver = new ChromeDriver();
            driver.manage().window().maximize();

            // Open Elasticsearch _search URL
     String url = "http://10.10.2.81:9200/dhin_library-model_book_index/_search";      
            driver.get(url);

            // Small wait for JSON to load
            Thread.sleep(2000);
           
                WebElement prettyPrint = driver.findElement(By.xpath("//div[@class='json-formatter-container']"));
                    prettyPrint.click(); 

            // Use browser search to highlight BookId 
            JavascriptExecutor js = (JavascriptExecutor) driver;
//            js.executeScript(
//                "window.find(arguments[0], false, false, true, false, true, false);", 
//                String.valueOf(bookId)
//            );
            js.executeScript("window.find('" + bookId + "');");

            // Screenshot
//            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
//            String path = "target/screenshots/es_" + bookId + "_" + action.replace(" ", "_") + ".png";
//            Files.createDirectories(Paths.get("target/screenshots/"));
//            File destFile = new File(path);
//            org.openqa.selenium.io.FileHandler.copy(srcFile, destFile);
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            // Attach to Extent
//            ExtentTest current = ExtentCucumberListener.getCurrentScenario();
//            if (current != null) {
//                current.info("Elasticsearch Verification for BookId: " + bookId);
//                current.addScreenCaptureFromPath(destFile.getAbsolutePath());
//            }
            ExtentTest current = ExtentCucumberListener.getCurrentScenario();
            if (current != null) {
                current.info("Elasticsearch Verification for BookId: " + bookId);
                current.addScreenCaptureFromBase64String(base64Screenshot,
                        "ElasticSearch_" + action.replace(" ", "_"));
//                MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot, "ElasticSearch Screenshot").build();

            }

            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
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
        captureDbSnapshot(BookId, "Create Book");
        captureElasticScreenshot(BookId, "Create Book");
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
        if (BookId == 0) throw new IllegalStateException("No BookId available. Run POST first.");

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
        captureDbSnapshot(BookId, "Update Book (PUT)");
//        captureElasticScreenshot(BookId, "Update Book (PUT)");
        
    }

    @When("Update Patch Book Request")
    public void Update_Patch_Book_Request() {
        if (BookId == 0) throw new IllegalStateException("No BookId available. Run POST first.");

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
        captureDbSnapshot(BookId, "Update Book (PATCH)");
//        captureElasticScreenshot(BookId, "Update Book (PATCH)");
        
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
        captureDbSnapshot(BookId, "Delete Book");
        
    }

    @Then("The Book response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertEquals(statusCode.intValue(), response.getStatusCode());
    }

    @Then("The Book response should contain {string}")
    public void the_response_should_contain(String key) {
        assertTrue("Response does not contain key: " + key, response.getBody().asString().contains(key));
    }
}
