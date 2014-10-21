package com.axeldev;

public class PhpHeredocToNowdocWithEscapingIntentionTest extends Php1UpLightCodeInsightFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpHeredocToNowdocWithEscapingIntention.INTENTION_NAME;

    public void testIntentionDescriptionExample() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testSimpleString() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapeSequences() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testPotentialEscapeSequenceSurpassesStringEnd() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testDifferentiateOctalCodeFromDecimalDigits() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testOctalEscapeSequencesLength() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHexEscapeSequencesLength() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnescapeEscapedDollarSign() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfFragment() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfHeredoc() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHeredocWhichContainsIdentifierItselfIsNotConverted() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHeredocWhichContainsIdentifierItselfSuspiciousScenarios() {
        phpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
