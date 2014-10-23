package com.axeldev;

public class PhpReplaceDoubleQuotesWithEscapingIntentionTest extends PhpFixtureTestCase {

    private static final String TEST_INTENTION_NAME_NO_VARS       = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_NO_VARS;
    private static final String TEST_INTENTION_NAME_EMBEDDED_VARS = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_EMBEDDED_VARS;

    public void testIntentionDescriptionExample() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testSimpleString() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEscapeSequences() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEmbeddedVar() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedVarWithBraces() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedArrayWithBraces() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedArrayWithUnquotedKeySyntax() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedMethodCallingExpression() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testPotentialEscapeSequenceSurpassesStringEnd() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testDifferentiateOctalCodeFromDecimalDigits() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testOctalEscapeSequencesLength() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testHexEscapeSequencesLength() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #1
    public void testUnescapeEscapedDollarSign() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #7
    public void testConcatArrayNumericAccess() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testDifferentiateDecimalFromOctalArrayIndices() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    // test for #8
    public void testBackslashBeforeNewlineAndEndOfFragment() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #9
    public void testConcatArrayVariableAccess() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

}
