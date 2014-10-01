package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PhpReplaceQuotesWithConcatenationIntention extends PsiElementBaseIntentionAction {

    public static final String FAMILY_NAME = "Replace quotes";
    // TODO mention also variable concatenation, make title dynamic?
    public static final String INTENTION_NAME = "Replace quotes with unescaping and variable concatenation";
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
    public static final char CHAR_DOT = '.';
    public static final char CHAR_LEFT_SQUARE_BRACKET = '[';
    public static final char CHAR_RIGHT_SQUARE_BRACKET = ']';

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
        //noinspection SimplifiableIfStatement
        if (!PhpWorkaroundUtil.isIntentionAvailable(psiElement)) return false;
        return getPhpDoubleQuotedStringExpression(psiElement) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiElement stringLiteralExpression = getPhpDoubleQuotedStringExpression(psiElement);
        if (stringLiteralExpression == null) return;
        PsiElement singleQuoteExpression = convertPhpDoubleQuotedStringToSingleQuoteAndExpressionConcatenation(project, psiElement, stringLiteralExpression);
        if (singleQuoteExpression == null) return;
        stringLiteralExpression.replace(singleQuoteExpression);
    }

    private PsiElement convertPhpDoubleQuotedStringToSingleQuoteAndExpressionConcatenation(Project project, PsiElement psiElement, PsiElement stringLiteralExpression) {
        ASTNode stringExpressionAstNode = stringLiteralExpression.getNode();
        if (stringExpressionAstNode == null) return null;
        ASTNode[] stringLiteralExpressionPieces = stringExpressionAstNode.getChildren(null);
        // TODO does this happen on empty string? if so, we should replace the empty string with a single quoted one, or maybe hide this intention?
        if (stringLiteralExpressionPieces.length == 0) return null;
        if (stringLiteralExpressionPieces.length > 1) {
            /* the string literal expression PSI has several children when there are embedded variables or expressions,
             * so the pieces are the left double quote, one or more string pieces and embedded variables or expressions,
             * and the final right double quote */
            List<String> stringAndVariableList = new ArrayList<String>();
            for (ASTNode stringLiteralExpressionPiece : stringLiteralExpressionPieces) {
                IElementType pieceType = stringLiteralExpressionPiece.getElementType();
                // skip delimiter quotes
                if (pieceType == PhpTokenTypes.chLDOUBLE_QUOTE ||
                    pieceType == PhpTokenTypes.chRDOUBLE_QUOTE) continue;
                if (pieceType == PhpTokenTypes.STRING_LITERAL) {
                    // ASTNode is a piece of textual content of the string
                    String stringPieceContent = stringLiteralExpressionPiece.getText();
                    String unescapedContent = unescapePhpDoubleQuotedStringContent(stringPieceContent);
                    String singleQuoteEscapedContent = escapePhpSingleQuotedStringContent(unescapedContent);
                    stringAndVariableList.add(CHAR_SINGLE_QUOTE + singleQuoteEscapedContent + CHAR_SINGLE_QUOTE);
                } else {
                    // ASTNode is a variable or expression embedded in the string
                    String variableOrExpression = cleanupStringEmbeddedExpression(stringLiteralExpressionPiece);
                    stringAndVariableList.add(variableOrExpression);
                }
            }
            String stringAndExpressionConcatenation = StringUtils.join(stringAndVariableList, CHAR_DOT);
            if (stringAndExpressionConcatenation == null) return null;
            return PhpPsiElementFactory.createPhpPsiFromText(project, PhpExpression.class, stringAndExpressionConcatenation);
        } else {
            ASTNode singleStringLiteralPiece = stringLiteralExpressionPieces[0];
            String unescapedContent = getPhpDoubleQuotedStringUnescapedContent(singleStringLiteralPiece.getPsi());
            String singleQuoteEscapedContent = escapePhpSingleQuotedStringContent(unescapedContent);
            String phpSingleQuotedString = CHAR_SINGLE_QUOTE + singleQuoteEscapedContent + CHAR_SINGLE_QUOTE;
            return PhpPsiElementFactory.createPhpPsiFromText(psiElement.getProject(), StringLiteralExpression.class, phpSingleQuotedString);
        }
    }

    private String cleanupStringEmbeddedExpression(ASTNode astNode) {
        ASTNode[] children = astNode.getChildren(null);
        if (children.length == 3 &&
            children[0].getElementType() == PhpTokenTypes.chLBRACE &&
            children[children.length - 1].getElementType() == PhpTokenTypes.chRBRACE) {
            // it's a variable or expression which was wrapped in curly braces in the string
            String expression = astNode.getText();
            return expression.substring(1, expression.length() - 1);
        } else if (children[0].getPsi() instanceof ArrayAccessExpression) {
            /* it's an array access expression, and since it's the only child it isn't wrapped in curly braces
             * so it's using the unquoted syntax on the access key: take the identifier part, the left square bracket,
             * wrap the key in quotes and finally take the right square bracket */
            ASTNode[] arrayAccessExpressionChildren = children[0].getChildren(null);
            String identifier = arrayAccessExpressionChildren[0].getText();
            String arrayAccessKey = arrayAccessExpressionChildren[2].getText();
            return identifier + CHAR_LEFT_SQUARE_BRACKET + CHAR_SINGLE_QUOTE + arrayAccessKey + CHAR_SINGLE_QUOTE + CHAR_RIGHT_SQUARE_BRACKET;
        } else {
            // if none of the previous condition is matched, then it's a simple variable embedding
            return astNode.getText();
        }
    }

    private PsiElement getPhpDoubleQuotedStringExpression(PsiElement psiElement) {
        if (psiElement instanceof PhpFile) return null;
        if (psiElement instanceof StringLiteralExpression) {
            PsiElement firstChild = psiElement.getFirstChild();
            if (firstChild != null) {
                ASTNode childAstNode = firstChild.getNode();
                IElementType childElementType = childAstNode.getElementType();
                if (childElementType == PhpTokenTypes.STRING_LITERAL || childElementType == PhpTokenTypes.chLDOUBLE_QUOTE) {
                    return psiElement;
                }
            }
        }
        PsiElement parentPsi = psiElement.getParent();
        return parentPsi != null ? getPhpDoubleQuotedStringExpression(parentPsi) : null;
    }

    private String getPhpDoubleQuotedStringUnescapedContent(PsiElement psiElement) {
        String phpStringLiteral = psiElement.getText();
        String escapedContent = phpStringLiteral.substring(1, phpStringLiteral.length() - 1);
        return unescapePhpDoubleQuotedStringContent(escapedContent);
    }

    private String unescapePhpDoubleQuotedStringContent(String escapedContent) {
        StringBuilder unescapedContentBuffer = new StringBuilder();
        EscapingState unescapingState = EscapingState.ReadingNewCharacter;
        StringBuilder currentUnescapeBuffer = new StringBuilder();
        for (char currentCharacter : escapedContent.toCharArray()) {
            boolean currentCharIsDigit = Character.isDigit(currentCharacter);
            // ReadingPotentialEscapeSequence means we have just found a backslash so next character will be checked
            // for matching any valid escape sequence
            if (unescapingState == EscapingState.ReadingPotentialEscapeSequence && currentCharIsDigit) {
                // detected decimal character escape sequence
                unescapingState = EscapingState.ReadingDecimalCharEscape;
            }
            if (unescapingState == EscapingState.ReadingDecimalCharEscape) {
                if (currentCharIsDigit) {
                    currentUnescapeBuffer.append(currentCharacter);
                }
                if (!currentCharIsDigit || currentUnescapeBuffer.length() == 3) {
                    // decimal sequence broken or max length reached, process current buffer and empty it
                    unescapedContentBuffer.append((char) Integer.parseInt(currentUnescapeBuffer.toString()));
                    currentUnescapeBuffer.setLength(0);
                    // stop decimal sequence reading
                    unescapingState = EscapingState.ReadingNewCharacter;
                    if (currentCharIsDigit) {
                        // current character has been processed as part of the decimal sequence, jump to next character
                        continue;
                    }
                    // else since current character wasn't part of the decimal sequence, let it be normally processed on
                    // the ReadingNewCharacter block
                }
            }
            if (unescapingState == EscapingState.ReadingPotentialEscapeSequence && currentCharacter == 'x') {
                // maybe hex character sequence detected, jump to next character to check for that
                unescapingState = EscapingState.ReadingPotentialHexCharEscape;
                continue;
            }
            if (unescapingState == EscapingState.ReadingPotentialHexCharEscape) {
                // we have just found \x, so check if now follows an hex sequence
                if (Character.toString(currentCharacter).matches(REGEXP_TEST_HEX_CHAR)) {
                    // verified it's an hex char sequence, let the character be processed as part of it on the
                    // ReadingHexCharEscape block
                    unescapingState = EscapingState.ReadingHexCharEscape;
                } else {
                    // no hex sequence follows, so just output the backslash and the wrongly escaped x, and let current
                    // character  be normally processed on the ReadingNewCharacter block
                    unescapedContentBuffer.append("\\x");
                    unescapingState = EscapingState.ReadingNewCharacter;
                }
            }
            if (unescapingState == EscapingState.ReadingHexCharEscape) {
                boolean currentCharIsHex = Character.toString(currentCharacter).matches(REGEXP_TEST_HEX_CHAR);
                if (currentCharIsHex) {
                    currentUnescapeBuffer.append(currentCharacter);
                }
                if (!currentCharIsHex || currentUnescapeBuffer.length() == 2) {
                    // hex sequence broken or max length reached, process current buffer and empty it
                    unescapedContentBuffer.append((char) Integer.parseInt(currentUnescapeBuffer.toString(), 16));
                    currentUnescapeBuffer.setLength(0);
                    // stop hex sequence reading
                    unescapingState = EscapingState.ReadingNewCharacter;
                    if (currentCharIsHex) {
                        // current character has been processed as part of the hex sequence, jump to next character
                        continue;
                    }
                    // else since current character wasn't part of the hex sequence, let it be normally processed on
                    // the ReadingNewCharacter block
                }
            }
            if (unescapingState == EscapingState.ReadingPotentialEscapeSequence) {
                // last character was a backslash so check if current character is a valid escape sequence
                switch (currentCharacter) {
                    case CHAR_N:
                        unescapedContentBuffer.append(CHAR_NEWLINE);
                        break;
                    case CHAR_R:
                        unescapedContentBuffer.append(CHAR_CARRIAGE_RETURN);
                        break;
                    case CHAR_T:
                        unescapedContentBuffer.append(CHAR_TAB);
                        break;
                    case CHAR_V:
                        unescapedContentBuffer.append(CHAR_VERTICAL_TAB);
                        break;
                    case CHAR_E:
                        unescapedContentBuffer.append(CHAR_ESC);
                        break;
                    case CHAR_F:
                        unescapedContentBuffer.append(CHAR_FORM_FEED);
                        break;
                    case CHAR_BACKSLASH:
                        unescapedContentBuffer.append(CHAR_BACKSLASH);
                        break;
                    case CHAR_DOUBLE_QUOTE:
                        unescapedContentBuffer.append(CHAR_DOUBLE_QUOTE);
                        break;
                    default:
                        // escape sequence was bad, so output both the backslash and the wrongly escaped character
                        unescapedContentBuffer.append(CHAR_BACKSLASH);
                        unescapedContentBuffer.append(currentCharacter);
                        break;
                }
                // reset reading mode and jump to next character
                unescapingState = EscapingState.ReadingNewCharacter;
                continue;
            }
            // normal processing of character: if it's backslash a new escape sequence begins, otherwise character is
            // outputted as is
            if (unescapingState == EscapingState.ReadingNewCharacter) {
                if (currentCharacter == CHAR_BACKSLASH) {
                    unescapingState = EscapingState.ReadingPotentialEscapeSequence;
                } else {
                    unescapedContentBuffer.append(currentCharacter);
                }
            }
        }
        return unescapedContentBuffer.toString();
    }

    private String escapePhpSingleQuotedStringContent(String text) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String singleQuotesAndBackslashEscaped = text.replaceAll("(\\\\|')", "\\\\$1");
        return singleQuotesAndBackslashEscaped;
    }
}
