package com.axeldev;

public class PhpHeredocToNowdocIntentionTest extends PhpFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpHeredocToNowdocIntention.INTENTION_NAME;

    public void testIntentionDescriptionExample() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testSimpleString() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapeSequences() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedVar() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedVarWithBraces() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedArrayWithBraces() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedArrayWithUnquotedKeySyntax() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEmbeddedMethodCallingExpression() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testPotentialEscapeSequenceSurpassesStringEnd() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testDifferentiateOctalCodeFromDecimalDigits() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testOctalEscapeSequencesLength() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHexEscapeSequencesLength() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnescapeEscapedDollarSign() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testConcatArrayNumericAccess() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testDifferentiateDecimalFromOctalArrayIndices() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfFragment() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testConcatArrayVariableAccess() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
