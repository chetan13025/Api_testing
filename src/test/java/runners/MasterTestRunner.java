package runners;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BookRunner.class,
    LibrariesRunner.class
})
public class MasterTestRunner {
}

//package runners;
//
//import org.junit.runner.RunWith;
//import io.cucumber.junit.Cucumber;
//import io.cucumber.junit.CucumberOptions;
//
//@RunWith(Cucumber.class)
//@CucumberOptions(
//	    features = "src/test/resources/features",
//	    glue = {"steps"},
//	    plugin = {
//	        "pretty",
//	        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"  // ✅ Add this
//	    },
//	    monochrome = true
//	)
//
//public class MasterTestRunner {
//}
