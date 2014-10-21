package com.axeldev;

public class PhpReplaceSingleQuotesWithEscapingIntentionTest extends Php1UpLightCodeInsightFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpReplaceSingleQuotesWithEscapingIntention.INTENTION_NAME;

    public void testIntentionDescriptionExample() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testSimpleString() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapeSequences() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testStringEndingWithBackslash() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testVariableVariable() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfSingleQuotedString() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    // test for #11
    public void testExcessiveBackslashBeforeSingleQuoteEscaping() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
