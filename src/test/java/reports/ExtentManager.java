package reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.io.File;

public class ExtentManager {

    private static ExtentReports extent;

    // single shared report file for entire test run
    private static final String REPORT_PATH = "target/Report.html";

    // get the single ExtentReports instance (lazy init)
    public synchronized static ExtentReports getInstance() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
            spark.config().setReportName("Automation Test Report");
            spark.config().setDocumentTitle("API Automation Project");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Tester", "Chetan Patil");
            extent.setSystemInfo("Project", "API Automation");
            extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }

    // flush at end of run
    public synchronized static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}
