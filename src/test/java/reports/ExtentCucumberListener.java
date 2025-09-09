package reports;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtentCucumberListener implements ConcurrentEventListener {

    // Map featureName -> ExtentTest (feature node). The ExtentTest instances come from
    // the corresponding ExtentReports instance for that feature.
    private static final Map<String, ExtentTest> featureTestMap = new ConcurrentHashMap<>();
    // ThreadLocal for current scenario node
    public static com.aventstack.extentreports.ExtentTest getCurrentScenario() {
        return scenarioNode.get();
    }
    private static final ThreadLocal<ExtentTest> scenarioNode = new ThreadLocal<>();

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
        publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
    }

    private void onTestCaseStarted(TestCaseStarted event) {
        try {
            String featurePath = event.getTestCase().getUri().getPath();
            String featureName = extractFeatureName(featurePath);
            String scenarioName = event.getTestCase().getName();

            // Get ExtentReports instance for this feature (creates one if missing)
            ExtentReports extent = ExtentManager.getInstance();

            // Create or get the feature node (ExtentTest) from featureTestMap
            ExtentTest featureTest = featureTestMap.computeIfAbsent(featureName, fn -> {
                System.out.println("[ExtentListener] Creating feature node: " + fn);
                return extent.createTest(fn);
            });

            // Create scenario node under that feature
            ExtentTest scenario = featureTest.createNode(scenarioName);
            scenarioNode.set(scenario);

            System.out.println("[ExtentListener] Started scenario: " + scenarioName + " (feature: " + featureName + ")");
        } catch (Exception e) {
            System.err.println("[ExtentListener] Exception in onTestCaseStarted:");
            e.printStackTrace();
        }
    }

    private void onTestStepFinished(TestStepFinished event) {
        try {
            Result result = event.getResult();
            String stepText = getStepText(event.getTestStep());

            ExtentTest scen = scenarioNode.get();
            if (scen == null) {
                System.err.println("[ExtentListener] scenarioNode is null for step: " + stepText);
                return;
            }

            switch (result.getStatus()) {
                case PASSED:
                    scen.pass(stepText);
                    break;
                case FAILED:
                    Throwable err = result.getError();
                    scen.fail(stepText + " : " + (err != null ? err.getMessage() : "No exception"));
                    break;
                default:
                    scen.skip(stepText);
            }
        } catch (Exception e) {
            System.err.println("[ExtentListener] Exception in onTestStepFinished:");
            e.printStackTrace();
        }
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        try {
            System.out.println("[ExtentListener] Finished scenario: " + event.getTestCase().getName());
            scenarioNode.remove();
        } catch (Exception e) {
            System.err.println("[ExtentListener] Exception in onTestCaseFinished:");
            e.printStackTrace();
        }
    }

    private void onTestRunFinished(TestRunFinished event) {
        try {
            System.out.println("[ExtentListener] Test run finished - flushing Extent reports.");
            ExtentManager.flushReports();
        } catch (Exception e) {
            System.err.println("[ExtentListener] Exception in onTestRunFinished:");
            e.printStackTrace();
        }
    }

    private String extractFeatureName(String featurePath) {
        if (featurePath == null) return "UnknownFeature";
        String[] parts = featurePath.replace("\\", "/").split("/");
        String file = parts[parts.length - 1];
        if (file.endsWith(".feature")) {
            return file.substring(0, file.length() - ".feature".length());
        }
        return file;
    }

    private String getStepText(TestStep step) {
        if (step instanceof PickleStepTestStep) {
            return ((PickleStepTestStep) step).getStep().getText();
        } else {
            return step.getCodeLocation();
        }
    }
}
