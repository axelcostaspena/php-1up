package com.axeldev;

public class PhpHeredocToNowdocWithEscapingIntentionTest extends PhpFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpHeredocToNowdocWithEscapingIntention.INTENTION_NAME;

    public void testIntentionDescriptionExample() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testSimpleString() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapeSequences() {
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

    public void testBackslashBeforeNewlineAndEndOfFragment() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfHeredoc() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHeredocWhichContainsIdentifierItselfIsNotConverted() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHeredocWhichContainsIdentifierItselfSuspiciousScenarios() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
