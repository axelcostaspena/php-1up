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
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class PhpReplaceQuotesWithConcatenationIntention extends PsiElementBaseIntentionAction {

    public static final String FAMILY_NAME = "Replace quotes";
    // TODO mention also variable concatenation, make title dynamic?
    public static final String INTENTION_NAME = "Replace quotes with unescaping and variable";
    public static final char CHAR_VERTICAL_TAB = (char) 11;
    public static final char CHAR_ESC = (char) 27;

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
        return isPhpStringLiteralDoubleQuote(psiElement);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (!isPhpStringLiteralDoubleQuote(psiElement)) return;
        PsiElement parentPsi = psiElement.getParent();
        if (!(parentPsi instanceof StringLiteralExpression)) return;
        String stringLiteralContent = getPhpDoubleQuotedStringRealContent(psiElement);
        StringLiteralExpression phpSingleQuotedStringLiteralPsi = getPhpSingleQuotedStringLiteralPsiFromText(psiElement.getProject(), stringLiteralContent);
        parentPsi.replace(phpSingleQuotedStringLiteralPsi);
    }

    private boolean isPhpStringLiteralDoubleQuote(PsiElement psiElement) {
        ASTNode astNode = psiElement.getNode();
        return astNode != null && astNode.getElementType() == PhpTokenTypes.STRING_LITERAL;
    }

    private String getPhpDoubleQuotedStringRealContent(PsiElement psiElement) {
        String phpStringLiteralText = psiElement.getText();
        String unescapedContent = phpStringLiteralText.substring(1, phpStringLiteralText.length() - 1);
        StringBuilder escapedContentBuffer = new StringBuilder();
        EscapingState escapingState = EscapingState.ReadingNewCharacter;
        StringBuilder currentEscapeBuffer = new StringBuilder();
        for (char currentCharacter : unescapedContent.toCharArray()) {
            boolean currentCharIsDigit = Character.isDigit(currentCharacter);
            if ((escapingState == EscapingState.ReadingPotentialEscapeSequence && currentCharIsDigit) ||
                escapingState == EscapingState.ReadingDecimalCharEscape) {
                escapingState = EscapingState.ReadingDecimalCharEscape;
                if (currentCharIsDigit) {
                    currentEscapeBuffer.append(currentCharacter);
                }
                if (!currentCharIsDigit || currentEscapeBuffer.length() == 3) {
                    // sequence broken or max length reached, process current buffer and empty it
                    escapedContentBuffer.append((char) Integer.parseInt(currentEscapeBuffer.toString()));
                    currentEscapeBuffer.setLength(0);
                    // stop decimal sequence reading
                    escapingState = EscapingState.ReadingNewCharacter;
                    if (currentCharIsDigit) {
                        // current character has just been processed, continue with next character
                        continue;
                    }
                    // else let current character be normally processed
                }
            }
            if (escapingState == EscapingState.ReadingPotentialEscapeSequence && currentCharacter == 'x') {
                // maybe hex character sequence detected, jump to next character to check
                escapingState = EscapingState.ReadingPotentialHexCharEscape;
                continue;
            }
            if (escapingState == EscapingState.ReadingPotentialHexCharEscape) {
                if (Character.toString(currentCharacter).matches("[0-9A-Fa-f]")) {
                    escapingState = EscapingState.ReadingHexCharEscape;
                    // verified it's a hex char sequence, let the character be processed as part of it
                } else {
                    // output the backslash and the wrongly escaped x which finally are no part of an hex char escape
                    escapedContentBuffer.append("\\x");
                    escapingState = EscapingState.ReadingNewCharacter;
                    // let the character be normally processed as no escaping sequence in course
                }
            }
            if (escapingState == EscapingState.ReadingHexCharEscape) {
                boolean currentCharIsHex = Character.toString(currentCharacter).matches("[0-9A-Fa-f]");
                if (currentCharIsHex) {
                    currentEscapeBuffer.append(currentCharacter);
                }
                if (!currentCharIsHex || currentEscapeBuffer.length() == 2) {
                    // sequence broken or max length reached, process current buffer
                    escapedContentBuffer.append((char) Integer.parseInt(currentEscapeBuffer.toString(), 16));
                    // stop hex sequence reading
                    escapingState = EscapingState.ReadingNewCharacter;
                    if (currentCharIsHex) {
                        // current character has just been processed, continue with next character
                        continue;
                    }
                    // else let current character be normally processed
                }
            }
            if (escapingState == EscapingState.ReadingPotentialEscapeSequence) {
                // check if character is a valid escape sequence
                switch (currentCharacter) {
                    case 'n':
                        escapedContentBuffer.append('\n');
                        break;
                    case 'r':
                        escapedContentBuffer.append('\r');
                        break;
                    case 't':
                        escapedContentBuffer.append('\t');
                        break;
                    case 'v':
                        escapedContentBuffer.append(CHAR_VERTICAL_TAB);
                        break;
                    case 'e':
                        escapedContentBuffer.append(CHAR_ESC);
                        break;
                    case 'f':
                        escapedContentBuffer.append('\f');
                        break;
                    case '\\':
                        escapedContentBuffer.append('\\');
                        break;
                    case '"':
                        escapedContentBuffer.append('"');
                        break;
                    default:
                        // escape sequence was bad, so print both the backslash and the character
                        escapedContentBuffer.append('\\');
                        escapedContentBuffer.append(currentCharacter);
                        break;
                }
                // job already done, jump to next character
                escapingState = EscapingState.ReadingNewCharacter;
                continue;
            }
            if (escapingState == EscapingState.ReadingNewCharacter) {
                if (currentCharacter == '\\') {
                    escapingState = EscapingState.ReadingPotentialEscapeSequence;
                } else {
                    escapedContentBuffer.append(currentCharacter);
                }
            }
        }
        return escapedContentBuffer.toString();
    }

    private StringLiteralExpression getPhpSingleQuotedStringLiteralPsiFromText(Project project, String stringContent) {
        String escapedPhpDoubleQuoteStringContent = EscapeForPhpDoubleQuotedString(stringContent);
        String phpStringLiteralText = "'" + escapedPhpDoubleQuoteStringContent + "'";
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteralText);
    }

    private String EscapeForPhpDoubleQuotedString(String text) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String singleQuotesAndBackslashEscaped = text.replaceAll("(\\\\|')", "\\\\$1");
        return singleQuotesAndBackslashEscaped;
    }
}
