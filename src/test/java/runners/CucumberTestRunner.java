package runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.example.stepdefinitions",
        plugin = {
            "pretty",
            "reports.ExtentCucumberListener"
        },
        monochrome = true
)
public class CucumberTestRunner {
}
