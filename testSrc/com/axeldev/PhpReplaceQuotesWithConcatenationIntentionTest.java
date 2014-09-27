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

public class PhpReplaceQuotesWithConcatenationIntentionTest extends LightCodeInsightFixtureTestCase {
    public static final String TEST_INTENTION_NAME = PhpReplaceQuotesWithConcatenationIntention.INTENTION_NAME;

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
        String testPath = PathManager.getJarPathForClass(PhpReplaceQuotesWithConcatenationIntention.class);
        return new File(testPath, "../../..");
    }

    private void phpIntentionTest(String testName, String intentionName) {
        myFixture.configureByFile("before" + testName + ".php");
        IntentionAction intention = myFixture.findSingleIntention(intentionName);
        myFixture.launchAction(intention);
        myFixture.checkResultByFile("after" + testName + ".php");
    }

    public void testIntentionDescriptionExample() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testSimpleString() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapeSequences() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedVar() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedVarWithBraces() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedArrayWithBraces() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedArrayWithUnquotedKeySyntax() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedMethodCallingExpression() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
