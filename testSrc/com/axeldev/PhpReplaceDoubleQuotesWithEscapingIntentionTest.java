package com.axeldev;

public class PhpReplaceDoubleQuotesWithEscapingIntentionTest extends PhpFixtureTestCase {

    private static final String TEST_INTENTION_NAME_NO_VARS       = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_NO_VARS;
    private static final String TEST_INTENTION_NAME_EMBEDDED_VARS = PhpReplaceDoubleQuotesWithEscapingIntention.INTENTION_NAME_EMBEDDED_VARS;

    public void testIntentionDescriptionExample() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnIntentionDescriptionExample() {
        checkIntentionUnavailableTest("IntentionDescriptionExample", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvailableInSimpleString() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnAvailableInSimpleString() {
        checkIntentionUnavailableTest("AvailableInSimpleString", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvailableAtStringsLeft() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnAvailableAtStringsLeft() {
        checkIntentionUnavailableTest("AvailableAtStringsLeft", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testUnavailableAtStringsRight() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnUnavailableAtStringsRight() {
        checkIntentionUnavailableTest("UnavailableAtStringsRight", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testJustComplementaryIntentionAvailableInSingleQuotedString() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnJustComplementaryIntentionAvailableInSingleQuotedString() {
        checkIntentionUnavailableTest("JustComplementaryIntentionAvailableInSingleQuotedString", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testUnavailableInHeredoc() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnUnavailableInHeredoc() {
        checkIntentionUnavailableTest("UnavailableInHeredoc", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testUnavailableInNowdoc() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnUnavailableInNowdoc() {
        checkIntentionUnavailableTest("UnavailableInNowdoc", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testUnavailableInDoubleQuotedHeredocIdentifier() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnUnavailableInDoubleQuotedHeredocIdentifier() {
        checkIntentionUnavailableTest("UnavailableInDoubleQuotedHeredocIdentifier", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testUnavailableInEmptyString() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnUnavailableInEmptyString() {
        checkIntentionUnavailableTest("UnavailableInEmptyString", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvailableInStringWithEmbeddedVariable() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnAvailableInStringWithEmbeddedVariable() {
        checkIntentionUnavailableTest("AvailableInStringWithEmbeddedVariable", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvailableInEmbeddedVariableWithoutBraces() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnAvailableInEmbeddedVariableWithoutBraces() {
        checkIntentionUnavailableTest("AvailableInEmbeddedVariableWithoutBraces", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvailableInEmbeddedVariableWithBraces() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnAvailableInEmbeddedVariableWithBraces() {
        checkIntentionUnavailableTest("AvailableInEmbeddedVariableWithBraces", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvailableInsideComplexExpression() {
        checkUniqueIntentionAvailableTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnAvailableInsideComplexExpression() {
        checkIntentionUnavailableTest("AvailableInsideComplexExpression", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testSimpleString() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnSimpleString() {
        checkIntentionUnavailableTest("SimpleString", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEscapeSequences() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnEscapeSequences() {
        checkIntentionUnavailableTest("EscapeSequences", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testEmbeddedVar() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnEmbeddedVar() {
        checkIntentionUnavailableTest("EmbeddedVar", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEmbeddedVarWithBraces() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnEmbeddedVarWithBraces() {
        checkIntentionUnavailableTest("EmbeddedVarWithBraces", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEmbeddedArrayWithBraces() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnEmbeddedArrayWithBraces() {
        checkIntentionUnavailableTest("EmbeddedArrayWithBraces", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEmbeddedArrayWithUnquotedKeySyntax() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnEmbeddedArrayWithUnquotedKeySyntax() {
        checkIntentionUnavailableTest("EmbeddedArrayWithUnquotedKeySyntax", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testEmbeddedMethodCallingExpression() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnEmbeddedMethodCallingExpression() {
        checkIntentionUnavailableTest("EmbeddedMethodCallingExpression", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testPotentialEscapeSequenceSurpassesStringEnd() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnPotentialEscapeSequenceSurpassesStringEnd() {
        checkIntentionUnavailableTest("PotentialEscapeSequenceSurpassesStringEnd", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testDifferentiateOctalCodeFromDecimalDigits() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnDifferentiateOctalCodeFromDecimalDigits() {
        checkIntentionUnavailableTest("DifferentiateOctalCodeFromDecimalDigits", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testOctalEscapeSequencesLength() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnOctalEscapeSequencesLength() {
        checkIntentionUnavailableTest("OctalEscapeSequencesLength", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testHexEscapeSequencesLength() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnHexEscapeSequencesLength() {
        checkIntentionUnavailableTest("HexEscapeSequencesLength", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    // test for #1
    public void testUnescapeEscapedDollarSign() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnUnescapeEscapedDollarSign() {
        checkIntentionUnavailableTest("UnescapeEscapedDollarSign", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    // test for #7
    public void testConcatArrayNumericAccess() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnConcatArrayNumericAccess() {
        checkIntentionUnavailableTest("ConcatArrayNumericAccess", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testDifferentiateDecimalFromOctalArrayIndices() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnDifferentiateDecimalFromOctalArrayIndices() {
        checkIntentionUnavailableTest("DifferentiateDecimalFromOctalArrayIndices", TEST_INTENTION_NAME_NO_VARS);
    }

    // test for #8
    public void testBackslashBeforeNewlineAndEndOfFragment() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_NO_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnBackslashBeforeNewlineAndEndOfFragment() {
        checkIntentionUnavailableTest("BackslashBeforeNewlineAndEndOfFragment", TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    // test for #9
    public void testConcatArrayVariableAccess() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnConcatArrayVariableAccess() {
        checkIntentionUnavailableTest("ConcatArrayVariableAccess", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testDontApplyToUnquotedArrayKey() {
        invokeUniqueIntentionTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnDontApplyToUnquotedArrayKey() {
        checkIntentionUnavailableTest("DontApplyToUnquotedArrayKey", TEST_INTENTION_NAME_NO_VARS);
    }

    public void testDontCollideWithSingleQuotedStringIntention() {
        checkIntentionUnavailableTest(getTestName(false), TEST_INTENTION_NAME_EMBEDDED_VARS);
    }

    public void testAvoidAlternativeIntentionNameMessesOnDontCollideWithSingleQuotedStringIntention() {
        invokeUniqueIntentionTest("DontCollideWithSingleQuotedStringIntention", TEST_INTENTION_NAME_NO_VARS);
    }

}
