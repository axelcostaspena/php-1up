package com.axeldev;

public class PhpNowdocToHeredocIntentionTest extends Php1UpLightCodeInsightFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpNowdocToHeredocIntention.INTENTION_NAME;

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

    public void testExcessiveBackslashBeforeSingleQuoteEscaping() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapingSeveralBackslashesMissesLast() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
