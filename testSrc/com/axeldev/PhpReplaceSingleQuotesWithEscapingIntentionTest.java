package com.axeldev;

public class PhpReplaceSingleQuotesWithEscapingIntentionTest extends Php1UpLightCodeInsightFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpReplaceSingleQuotesWithEscapingIntention.INTENTION_NAME;

    public void testIntentionDescriptionExample() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testSimpleString() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapeSequences() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testStringEndingWithBackslash() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testVariableVariable() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfSingleQuotedString() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    // test for #11
    public void testExcessiveBackslashBeforeSingleQuoteEscaping() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
