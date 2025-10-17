package runners;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import utils.EmailReportSender;
import utils.ReportUtils;

@RunWith(Cucumber.class)
@CucumberOptions(
  plugin = { "pretty", "reports.ExtentCucumberListener" },
  features = "src/test/resources/features",
  glue = { "library" }
)
public class TestRunner {
@AfterClass
public static void tearDown() {
    try {
        // You can change extension to .html or .pdf depending on your report
        String latest = ReportUtils.getLatestReportFile("target", ".html");
        System.out.println("Latest report file: " + latest);

        if (latest != null) {
            // change recipient(s) as needed (comma-separated accepted by InternetAddress.parse)
            EmailReportSender.sendReport("chetan.patil@dharbor.com", latest);
        } else {
            System.out.println("No report file found in target/ to send.");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
