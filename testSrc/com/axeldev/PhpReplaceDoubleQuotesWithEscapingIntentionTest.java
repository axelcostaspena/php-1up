package com.axeldev;

public class PhpReplaceDoubleQuotesWithEscapingIntentionTest extends Php1UpLightCodeInsightFixtureTestCase {

    private static final String TEST_INTENTION_NAME_NO_VARS       = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_NO_VARS;
    private static final String TEST_INTENTION_NAME_EMBEDDED_VARS = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_EMBEDDED_VARS;

    public void testIntentionDescriptionExample() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testSimpleString() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEscapeSequences() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEmbeddedVar() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedVarWithBraces() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedArrayWithBraces() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedArrayWithUnquotedKeySyntax() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedMethodCallingExpression() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testPotentialEscapeSequenceSurpassesStringEnd() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testDifferentiateOctalCodeFromDecimalDigits() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testOctalEscapeSequencesLength() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testHexEscapeSequencesLength() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #1
    public void testUnescapeEscapedDollarSign() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #7
    public void testConcatArrayNumericAccess() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testDifferentiateDecimalFromOctalArrayIndices() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    // test for #8
    public void testBackslashBeforeNewlineAndEndOfFragment() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #9
    public void testConcatArrayVariableAccess() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

}
