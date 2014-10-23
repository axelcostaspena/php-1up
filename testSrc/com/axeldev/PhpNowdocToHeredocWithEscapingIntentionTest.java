package com.axeldev;

public class PhpNowdocToHeredocWithEscapingIntentionTest extends PhpFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpNowdocToHeredocWithEscapingIntention.INTENTION_NAME;

    public void testIntentionDescriptionExample() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
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

    // test for #11
    public void testExcessiveBackslashBeforeSingleQuoteEscaping() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapingSeveralBackslashesMissesLast() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
