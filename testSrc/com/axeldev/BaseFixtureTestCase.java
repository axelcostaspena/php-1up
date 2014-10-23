package com.axeldev;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public abstract class BaseFixtureTestCase extends LightCodeInsightFixtureTestCase {

    private static File getProjectRootPath() {
        String testPath = PathManager.getJarPathForClass(PhpHeredocToNowdocIntention.class);
        return new File(testPath, "../../..");
    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new MyDescriptor();
    }

    @Override
    protected String getTestDataPath() {
        File sourceRoot = getProjectRootPath();
        return new File(new File(sourceRoot, "testData"), getClass().getName()).getPath();
    }

    public abstract String getTestDataFilesExtension();

    protected void checkIntentionAvailableTest(String testName, String intentionName) {
        fixtureTestCase(testName, getTestDataFilesExtension(), true, intentionName, false, false);
    }

    protected void checkUniqueIntentionAvailableTest(String testName, String intentionName) {
        fixtureTestCase(testName, getTestDataFilesExtension(), true, intentionName, true, false);
    }

    protected void invokeUniqueIntentionTest(String testName, String intentionName) {
        fixtureTestCase(testName, getTestDataFilesExtension(), false, intentionName, true, true);
    }

    protected void invokeIntentionTest(String testName, String intentionName) {
        fixtureTestCase(testName, getTestDataFilesExtension(), false, intentionName, false, true);
    }

    protected void invokeCancelledUniqueIntentionTest(String testName, String intentionName) {
        fixtureTestCase(testName, getTestDataFilesExtension(), true, intentionName, true, true);
    }

    protected void invokeCancelledIntentionTest(String testName, String intentionName) {
        fixtureTestCase(testName, getTestDataFilesExtension(), true, intentionName, false, true);
    }

    private void fixtureTestCase(String testDataFilesName, String testDataFilesExtension, boolean reuseTestDataBeforeFile, String intentionName, boolean breakOnDuplicateIntention, boolean launchIntention) {
        String beforeTestDataFileName = "before" + testDataFilesName + "." + testDataFilesExtension;
        myFixture.configureByFile(beforeTestDataFileName);
        List<IntentionAction> intentions = findAvailableIntentions(myFixture, intentionName);
        if (intentions.size() == 0) {
            throw new AssertionError("Intention \"" + intentionName + "\" is not available at specified document position");
        }
        if (breakOnDuplicateIntention && intentions.size() > 1) {
            throw new AssertionError("Duplicate intention \"" + intentionName + "\" found at specified document position");
        }
        if (launchIntention) {
            myFixture.launchAction(intentions.get(0));
        }
        String afterTestDataFileName = (reuseTestDataBeforeFile ? "before" : "after") + testDataFilesName + "." + testDataFilesExtension;
        myFixture.checkResultByFile(afterTestDataFileName);
    }

    private List<IntentionAction> findAvailableIntentions(JavaCodeInsightTestFixture fixture, String intentionName) {
        List<IntentionAction> intentions = fixture.filterAvailableIntentions(intentionName);
        for (IntentionAction intention : intentions) {
            if (!intention.getText().equals(intentionName)) {
                intentions.remove(intention);
            }
        }
        return intentions;
    }

    private static class MyDescriptor extends DefaultLightProjectDescriptor {
        @Override
        public Sdk getSdk() {
            return JavaSdk.getInstance().createJdk("1.7", new File(getProjectRootPath(), "mockJDK-1.7").getPath(), false);
        }
    }
}
