package runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features/book_api.feature",
    glue = {"library"},
    plugin = {"pretty", "html:target/cucumber-report-book.html"},
    monochrome = true
)
public class BookRunner {
}
