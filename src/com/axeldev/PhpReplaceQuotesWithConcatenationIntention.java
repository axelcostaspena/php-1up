package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpExpressionCodeFragment;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class PhpReplaceQuotesWithConcatenationIntention extends PsiElementBaseIntentionAction {

    public static final String FAMILY_NAME = "Replace quotes";
    // TODO mention also variable concatenation, make title dynamic?
    public static final String INTENTION_NAME = "Replace quotes with unescaping and variable";
    public static final char CHAR_VERTICAL_TAB = (char) 11;
    public static final char CHAR_ESC = (char) 27;
    public static final char CHAR_NEWLINE = '\n';
    public static final char CHAR_CARRIAGE_RETURN = '\r';
    public static final char CHAR_TAB = '\t';
    public static final char CHAR_FORM_FEED = '\f';
    public static final char CHAR_BACKSLASH = '\\';
    public static final char CHAR_DOUBLE_QUOTE = '"';
    public static final char CHAR_SINGLE_QUOTE = '\'';
    public static final char CHAR_F = 'f';
    public static final char CHAR_E = 'e';
    public static final char CHAR_V = 'v';
    public static final char CHAR_T = 't';
    public static final char CHAR_R = 'r';
    public static final char CHAR_N = 'n';
    public static final String REGEXP_TEST_HEX_CHAR = "[0-9A-Fa-f]";

    private enum EscapingState {
        ReadingNewCharacter,
        ReadingPotentialEscapeSequence,
        ReadingDecimalCharEscape,
        ReadingPotentialHexCharEscape,
        ReadingHexCharEscape
    }

    @NotNull
    @Override
    public String getText() {
        return INTENTION_NAME;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        PsiFile containingFile = psiElement.getContainingFile();
        //noinspection SimplifiableIfStatement
        if (containingFile instanceof PhpExpressionCodeFragment) return false;
        return getPhpDoubleQuotedStringLiteralExpression(psiElement) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiElement stringLiteralExpressionPsi = getPhpDoubleQuotedStringLiteralExpression(psiElement);
        String stringLiteralContent = getPhpDoubleQuotedStringRealContent(stringLiteralExpressionPsi);
        StringLiteralExpression phpSingleQuotedStringLiteralPsi = getPhpSingleQuotedStringLiteralPsiFromText(psiElement.getProject(), stringLiteralContent);
        stringLiteralExpressionPsi.replace(phpSingleQuotedStringLiteralPsi);
    }

    private PsiElement getPhpDoubleQuotedStringLiteralExpression(PsiElement psiElement) {
        if (psiElement instanceof PhpFile) return null;
        if (psiElement instanceof StringLiteralExpression) {
            PsiElement firstChild = psiElement.getFirstChild();
            if (firstChild != null) {
                ASTNode astNode = firstChild.getNode();
                if (astNode.getElementType() == PhpTokenTypes.STRING_LITERAL || astNode.getElementType() == PhpTokenTypes.chLDOUBLE_QUOTE) {
                    return psiElement;
                }
            }
        }
        PsiElement parentPsi = psiElement.getParent();
        return parentPsi != null ? getPhpDoubleQuotedStringLiteralExpression(parentPsi) : null;
    }

    private String getPhpDoubleQuotedStringRealContent(PsiElement psiElement) {
        String phpStringLiteralText = psiElement.getText();
        String unescapedContent = phpStringLiteralText.substring(1, phpStringLiteralText.length() - 1);
        return unescapePhpDoubleQuotedNonComplexStringContent(unescapedContent);
    }

    private String unescapePhpDoubleQuotedNonComplexStringContent(String text) {
        StringBuilder escapedContentBuffer = new StringBuilder();
        EscapingState escapingState = EscapingState.ReadingNewCharacter;
        StringBuilder currentEscapeBuffer = new StringBuilder();
        for (char currentCharacter : text.toCharArray()) {
            boolean currentCharIsDigit = Character.isDigit(currentCharacter);
            // ReadingPotentialEscapeSequence means we have just found a backslash so next character will be checked
            // for matching any valid escape sequence
            if (escapingState == EscapingState.ReadingPotentialEscapeSequence && currentCharIsDigit) {
                // detected decimal character escape sequence
                escapingState = EscapingState.ReadingDecimalCharEscape;
            }
            if (escapingState == EscapingState.ReadingDecimalCharEscape) {
                if (currentCharIsDigit) {
                    currentEscapeBuffer.append(currentCharacter);
                }
                if (!currentCharIsDigit || currentEscapeBuffer.length() == 3) {
                    // decimal sequence broken or max length reached, process current buffer and empty it
                    escapedContentBuffer.append((char) Integer.parseInt(currentEscapeBuffer.toString()));
                    currentEscapeBuffer.setLength(0);
                    // stop decimal sequence reading
                    escapingState = EscapingState.ReadingNewCharacter;
                    if (currentCharIsDigit) {
                        // current character has been processed as part of the decimal sequence, jump to next character
                        continue;
                    }
                    // else since current character wasn't part of the decimal sequence, let it be normally processed on
                    // the ReadingNewCharacter block
                }
            }
            if (escapingState == EscapingState.ReadingPotentialEscapeSequence && currentCharacter == 'x') {
                // maybe hex character sequence detected, jump to next character to check for that
                escapingState = EscapingState.ReadingPotentialHexCharEscape;
                continue;
            }
            if (escapingState == EscapingState.ReadingPotentialHexCharEscape) {
                // we have just found \x, so check if now follows an hex sequence
                if (Character.toString(currentCharacter).matches(REGEXP_TEST_HEX_CHAR)) {
                    // verified it's an hex char sequence, let the character be processed as part of it on the
                    // ReadingHexCharEscape block
                    escapingState = EscapingState.ReadingHexCharEscape;
                } else {
                    // no hex sequence follows, so just output the backslash and the wrongly escaped x, and let current
                    // character  be normally processed on the ReadingNewCharacter block
                    escapedContentBuffer.append("\\x");
                    escapingState = EscapingState.ReadingNewCharacter;
                }
            }
            if (escapingState == EscapingState.ReadingHexCharEscape) {
                boolean currentCharIsHex = Character.toString(currentCharacter).matches(REGEXP_TEST_HEX_CHAR);
                if (currentCharIsHex) {
                    currentEscapeBuffer.append(currentCharacter);
                }
                if (!currentCharIsHex || currentEscapeBuffer.length() == 2) {
                    // hex sequence broken or max length reached, process current buffer and empty it
                    escapedContentBuffer.append((char) Integer.parseInt(currentEscapeBuffer.toString(), 16));
                    currentEscapeBuffer.setLength(0);
                    // stop hex sequence reading
                    escapingState = EscapingState.ReadingNewCharacter;
                    if (currentCharIsHex) {
                        // current character has been processed as part of the hex sequence, jump to next character
                        continue;
                    }
                    // else since current character wasn't part of the hex sequence, let it be normally processed on
                    // the ReadingNewCharacter block
                }
            }
            if (escapingState == EscapingState.ReadingPotentialEscapeSequence) {
                // last character was a backslash so check if current character is a valid escape sequence
                switch (currentCharacter) {
                    case CHAR_N:
                        escapedContentBuffer.append(CHAR_NEWLINE);
                        break;
                    case CHAR_R:
                        escapedContentBuffer.append(CHAR_CARRIAGE_RETURN);
                        break;
                    case CHAR_T:
                        escapedContentBuffer.append(CHAR_TAB);
                        break;
                    case CHAR_V:
                        escapedContentBuffer.append(CHAR_VERTICAL_TAB);
                        break;
                    case CHAR_E:
                        escapedContentBuffer.append(CHAR_ESC);
                        break;
                    case CHAR_F:
                        escapedContentBuffer.append(CHAR_FORM_FEED);
                        break;
                    case CHAR_BACKSLASH:
                        escapedContentBuffer.append(CHAR_BACKSLASH);
                        break;
                    case CHAR_DOUBLE_QUOTE:
                        escapedContentBuffer.append(CHAR_DOUBLE_QUOTE);
                        break;
                    default:
                        // escape sequence was bad, so output both the backslash and the wrongly escaped character
                        escapedContentBuffer.append(CHAR_BACKSLASH);
                        escapedContentBuffer.append(currentCharacter);
                        break;
                }
                // reset reading mode and jump to next character
                escapingState = EscapingState.ReadingNewCharacter;
                continue;
            }
            // normal processing of character: if it's backslash a new escape sequence begins, otherwise character is
            // outputted as is
            if (escapingState == EscapingState.ReadingNewCharacter) {
                if (currentCharacter == CHAR_BACKSLASH) {
                    escapingState = EscapingState.ReadingPotentialEscapeSequence;
                } else {
                    escapedContentBuffer.append(currentCharacter);
                }
            }
        }
        return escapedContentBuffer.toString();
    }

    private StringLiteralExpression getPhpSingleQuotedStringLiteralPsiFromText(Project project, String stringContent) {
        String escapedPhpDoubleQuoteStringContent = EscapeForPhpSingleQuotedString(stringContent);
        String phpStringLiteralText = CHAR_SINGLE_QUOTE + escapedPhpDoubleQuoteStringContent + CHAR_SINGLE_QUOTE;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteralText);
    }

    private String EscapeForPhpSingleQuotedString(String text) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String singleQuotesAndBackslashEscaped = text.replaceAll("(\\\\|')", "\\\\$1");
        return singleQuotesAndBackslashEscaped;
    }
}
