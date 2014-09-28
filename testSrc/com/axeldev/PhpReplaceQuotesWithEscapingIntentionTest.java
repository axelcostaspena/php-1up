package com.axeldev;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PhpReplaceQuotesWithEscapingIntentionTest extends LightCodeInsightFixtureTestCase {

    public static final String TEST_INTENTION_NAME = PhpReplaceQuotesWithEscapingIntention.INTENTION_NAME;

    private static class MyDescriptor extends DefaultLightProjectDescriptor {
        @Override
        public Sdk getSdk() {
            return JavaSdk.getInstance().createJdk("1.7", new File(getProjectRootPath(), "mockJDK-1.7").getPath(), false);
        }
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

    private static File getProjectRootPath() {
        String testPath = PathManager.getJarPathForClass(PhpReplaceQuotesWithEscapingIntention.class);
        return new File(testPath, "../../..");
    }

    private void basePhpIntentionTest(String testName, String intentionName) {
        myFixture.configureByFile("before" + testName + ".php");
        IntentionAction intention = myFixture.findSingleIntention(intentionName);
        myFixture.launchAction(intention);
        myFixture.checkResultByFile("after" + testName + ".php");
    }

    public void testSimpleString() {
        basePhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testDescriptionExample() {
        basePhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testStringEndingWithBackslash() {
        basePhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
