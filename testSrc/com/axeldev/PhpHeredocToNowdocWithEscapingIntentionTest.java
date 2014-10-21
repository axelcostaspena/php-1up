package com.axeldev;

public class PhpHeredocToNowdocWithEscapingIntentionTest extends Php1UpLightCodeInsightFixtureTestCase {

    private static final String TEST_INTENTION_NAME = PhpHeredocToNowdocWithEscapingIntention.INTENTION_NAME;

    public void testIntentionDescriptionExample() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testSimpleString() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testEscapeSequences() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testPotentialEscapeSequenceSurpassesStringEnd() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testDifferentiateOctalCodeFromDecimalDigits() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testOctalEscapeSequencesLength() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHexEscapeSequencesLength() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testUnescapeEscapedDollarSign() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfFragment() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testBackslashBeforeNewlineAndEndOfHeredoc() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHeredocWhichContainsIdentifierItselfIsNotConverted() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

    public void testHeredocWhichContainsIdentifierItselfSuspiciousScenarios() {
        launchPhpIntentionTest(getTestName(false), TEST_INTENTION_NAME);
    }

}
