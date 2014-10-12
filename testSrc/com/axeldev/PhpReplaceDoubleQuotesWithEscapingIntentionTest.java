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

public class PhpReplaceDoubleQuotesWithEscapingIntentionTest extends LightCodeInsightFixtureTestCase {
    private static final String TEST_INTENTION_NAME_NO_VARS       = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_NO_VARS;
    private static final String TEST_INTENTION_NAME_EMBEDDED_VARS = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_EMBEDDED_VARS;

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
        String testPath = PathManager.getJarPathForClass(PhpReplaceDoubleQuotesWithEscapingIntention.class);
        return new File(testPath, "../../..");
    }

    private void phpIntentionTest(String testName, String intentionName) {
        myFixture.configureByFile("before" + testName + ".php");
        IntentionAction intention = myFixture.getAvailableIntention(intentionName);
        if (intention == null) throw new AssertionError("Intention \"" + intentionName + "\" is not available at specified document position");
        myFixture.launchAction(intention);
        myFixture.checkResultByFile("after" + testName + ".php");
    }

    public void testIntentionDescriptionExample() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testSimpleString() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEscapeSequences() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEmbeddedVar() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedVarWithBraces() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedArrayWithBraces() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedArrayWithUnquotedKeySyntax() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedMethodCallingExpression() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testPotentialEscapeSequenceSurpassesStringEnd() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testDifferentiateOctalCodeFromDecimalDigits() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testOctalEscapeSequencesLength() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testHexEscapeSequencesLength() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #1
    public void testUnescapeEscapedDollarSign() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #7
    public void testConcatArrayNumericAccess() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testDifferentiateDecimalFromOctalArrayIndices() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    // test for #8
    public void testBackslashBeforeNewlineAndEndOfFragment() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testBackslashBeforeNewlineAndEndOfSingleQuotedString() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #9
    public void testConcatArrayVariableAccess() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

}
