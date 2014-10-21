package com.axeldev;

public class PhpReplaceDoubleQuotesWithEscapingIntentionTest extends Php1UpLightCodeInsightFixtureTestCase {

    private static final String TEST_INTENTION_NAME_NO_VARS       = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_NO_VARS;
    private static final String TEST_INTENTION_NAME_EMBEDDED_VARS = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_EMBEDDED_VARS;

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

    // test for #9
    public void testConcatArrayVariableAccess() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

}
