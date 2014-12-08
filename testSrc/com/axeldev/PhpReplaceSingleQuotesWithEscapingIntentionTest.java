package com.axeldev;

public class PhpReplaceSingleQuotesWithEscapingIntentionTest extends PhpFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpReplaceSingleQuotesWithEscapingIntention.INTENTION_NAME;

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

    public void testUnavailableInHeredoc() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnavailableInNowdocIdentifier() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnavailableInEmptyString() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testSimpleString() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapeSequences() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testStringEndingWithBackslash() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testVariableVariable() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfSingleQuotedString() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    // test for #11
    public void testExcessiveBackslashBeforeSingleQuoteEscaping() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
