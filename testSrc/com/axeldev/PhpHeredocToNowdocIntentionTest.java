package com.axeldev;

public class PhpHeredocToNowdocIntentionTest extends PhpFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpHeredocToNowdocIntention.INTENTION_NAME;

    public void testIntentionDescriptionExample() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testAvailableInSimpleString() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testAvailableAtStringsLeft() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnavailableAtStringsRight() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnavailableInNowdoc() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnavailableInDoubleQuotedString() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnavailableInSingleQuotedString() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testAvailableInEmptyString() {
        checkIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testAvailableInStringWithEscapeSequences() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testAvailableInStringWithEmbeddedVariable() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testAvailableInsideComplexExpression() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME);
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

    public void testEmbeddedArrayWithBraces() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testPotentialEscapeSequenceSurpassesStringEnd() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHexEscapeSequencesLength() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnescapeEscapedDollarSign() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfFragment() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHeredocWhichContainsIdentifierItselfSuspiciousScenarios() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
