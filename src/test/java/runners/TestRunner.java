package runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
  plugin = { "pretty", "reports.ExtentCucumberListener" },
  features = "src/test/resources/features",
  glue = { "library" }
)
public class TestRunner {
}
