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

import java.util.*;

public class PhpReplaceQuotesWithConcatenationIntention extends PsiElementBaseIntentionAction {

    public static final String FAMILY_NAME                  = "Replace quotes";
    public static final String INTENTION_NAME_NO_VARS       = "Replace quotes with unescaping";
    public static final String INTENTION_NAME_EMBEDDED_VARS = "Replace quotes with unescaping and variable concatenation";
    public static final char   CHAR_VERTICAL_TAB            = (char) 11;
    public static final char   CHAR_ESC                     = (char) 27;
    public static final char   CHAR_NEWLINE                 = '\n';
    public static final char   CHAR_CARRIAGE_RETURN         = '\r';
    public static final char   CHAR_TAB                     = '\t';
    public static final char   CHAR_FORM_FEED               = '\f';
    public static final char   CHAR_BACKSLASH               = '\\';
    public static final char   CHAR_DOUBLE_QUOTE            = '"';
    public static final char   CHAR_SINGLE_QUOTE            = '\'';
    public static final char   CHAR_LEFT_SQUARE_BRACKET     = '[';
    public static final char   CHAR_RIGHT_SQUARE_BRACKET    = ']';
    public static final char   CHAR_DOT                     = '.';
    public static final char   CHAR_LCASE_E                 = 'e';
    public static final char   CHAR_LCASE_F                 = 'f';
    public static final char   CHAR_LCASE_N                 = 'n';
    public static final char   CHAR_LCASE_R                 = 'r';
    public static final char   CHAR_LCASE_T                 = 't';
    public static final char   CHAR_LCASE_V                 = 'v';
    public static final char   CHAR_LCASE_X                 = 'x';
    public static final String REGEXP_CHAR_IS_OCTAL         = "[0-7]";
    public static final String REGEXP_CHAR_IS_HEX           = "[0-9A-Fa-f]";

    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (!PhpWorkaroundUtil.isIntentionAvailable(psiElement)) return false;
        PsiElement stringLiteralExpression = getPhpDoubleQuotedStringExpression(psiElement);
        if (stringLiteralExpression == null || isPhpDoubleQuotedEmptyString(psiElement)) return false;
        String intentionText = stringLiteralExpression.getNode().getChildren(null).length > 1 ? INTENTION_NAME_EMBEDDED_VARS : INTENTION_NAME_NO_VARS;
        this.setText(intentionText);
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiElement stringLiteralExpression = getPhpDoubleQuotedStringExpression(psiElement);
        if (stringLiteralExpression == null) return;
        PsiElement singleQuoteExpression = convertPhpDoubleQuotedStringToSingleQuoteAndExpressionConcatenation(project, psiElement, stringLiteralExpression);
        if (singleQuoteExpression == null) return;
        stringLiteralExpression.replace(singleQuoteExpression);
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

    private boolean isPhpDoubleQuotedEmptyString(PsiElement psiElement) {
        return psiElement.getText().equals("\"\"");
    }

    private PsiElement convertPhpDoubleQuotedStringToSingleQuoteAndExpressionConcatenation(Project project, PsiElement psiElement, PsiElement stringLiteralExpression) {
        ASTNode stringExpressionAstNode = stringLiteralExpression.getNode();
        if (stringExpressionAstNode == null) return null;
        ASTNode[] stringLiteralExpressionPieces = stringExpressionAstNode.getChildren(null);
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
                    pieceType == PhpTokenTypes.chRDOUBLE_QUOTE) { continue; }
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

    private String unescapePhpDoubleQuotedStringContent(String escapedContent) {

        CharEnumeration charEnumeration = new CharEnumeration(escapedContent.toCharArray());
        StringBuilder unescapedContentBuffer = new StringBuilder();

        // parse double string contents
        // see http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double
        if (charEnumeration.hasMoreElements()) {
            char currentChar = charEnumeration.nextElement();
            // semaphore for exiting the loop when reached end of string
            boolean endOfString = false;
            // loop while parsing the string characters
            do {
                if (currentChar == CHAR_BACKSLASH && charEnumeration.hasMoreElements()) {
                    // check if backslash is part of an escape sequence
                    currentChar = charEnumeration.nextElement();
                    if (Character.toString(currentChar).matches(REGEXP_CHAR_IS_OCTAL)) {
                        // from one to three digits will make up an octal escape sequence
                        String octalCode = String.valueOf(currentChar);
                        if (charEnumeration.hasMoreElements()) {
                            currentChar = charEnumeration.nextElement();
                            if (Character.toString(currentChar).matches(REGEXP_CHAR_IS_OCTAL)) {
                                octalCode += String.valueOf(currentChar);
                                if (charEnumeration.hasMoreElements()) {
                                    currentChar = charEnumeration.nextElement();
                                    if (Character.toString(currentChar).matches(REGEXP_CHAR_IS_OCTAL)) {
                                        octalCode += String.valueOf(currentChar);
                                    }
                                }
                            }
                        }
                        unescapedContentBuffer.append((char) Integer.parseInt(octalCode, 8));
                        // if last read character wasn't part of the octal escape sequence, loop and re process it
                        if (!Character.toString(currentChar).matches(REGEXP_CHAR_IS_OCTAL)) continue;
                    } else {
                        switch (currentChar) {
                            case CHAR_LCASE_X:
                                // check if backslash-x is part of an hex escape sequence
                                boolean moreElements = charEnumeration.hasMoreElements();
                                if (moreElements) {
                                    currentChar = charEnumeration.nextElement();
                                }
                                if (moreElements && Character.toString(currentChar).matches(REGEXP_CHAR_IS_HEX)) {
                                    // one or two hex characters will make up an hex escape sequence
                                    String hexCode = String.valueOf(currentChar);
                                    if (charEnumeration.hasMoreElements()) {
                                        currentChar = charEnumeration.nextElement();
                                        if (Character.toString(currentChar).matches(REGEXP_CHAR_IS_HEX)) {
                                            hexCode += String.valueOf(currentChar);
                                        }
                                    }
                                    unescapedContentBuffer.append((char) Integer.parseInt(hexCode, 16));
                                    /* if last read character wasn't part of the hex escape sequence, loop and re
                                     * process it */
                                    if (!Character.toString(currentChar).matches(REGEXP_CHAR_IS_HEX)) continue;
                                } else {
                                    /* since next character don't make up an hex sequence, output both the backslash
                                     * and the x, then if next character do exist, loop and re process it */
                                    unescapedContentBuffer.append(CHAR_BACKSLASH);
                                    unescapedContentBuffer.append(CHAR_LCASE_X);
                                    if (moreElements) continue;
                                }
                                break;
                            case CHAR_LCASE_N:
                                unescapedContentBuffer.append(CHAR_NEWLINE);
                                break;
                            case CHAR_LCASE_R:
                                unescapedContentBuffer.append(CHAR_CARRIAGE_RETURN);
                                break;
                            case CHAR_LCASE_T:
                                unescapedContentBuffer.append(CHAR_TAB);
                                break;
                            case CHAR_LCASE_V:
                                unescapedContentBuffer.append(CHAR_VERTICAL_TAB);
                                break;
                            case CHAR_LCASE_E:
                                unescapedContentBuffer.append(CHAR_ESC);
                                break;
                            case CHAR_LCASE_F:
                                unescapedContentBuffer.append(CHAR_FORM_FEED);
                                break;
                            case CHAR_BACKSLASH:
                                unescapedContentBuffer.append(CHAR_BACKSLASH);
                                break;
                            case CHAR_DOUBLE_QUOTE:
                                unescapedContentBuffer.append(CHAR_DOUBLE_QUOTE);
                                break;
                            default:
                                // potential escape sequence wasn't so, so output both the backslash and the character
                                unescapedContentBuffer.append(CHAR_BACKSLASH);
                                unescapedContentBuffer.append(currentChar);
                                break;
                        }
                    }
                } else {
                    unescapedContentBuffer.append(currentChar);
                }
                if (charEnumeration.hasMoreElements()) {
                    currentChar = charEnumeration.nextElement();
                } else {
                    endOfString = true;
                }
            } while (!endOfString);
        }
        return unescapedContentBuffer.toString();
    }

    private String escapePhpSingleQuotedStringContent(String text) {
        return text.replaceAll("('|\\\\(?=')|\\\\$)", "\\\\$1");
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

    private String getPhpDoubleQuotedStringUnescapedContent(PsiElement psiElement) {
        String phpStringLiteral = psiElement.getText();
        String escapedContent = phpStringLiteral.substring(1, phpStringLiteral.length() - 1);
        return unescapePhpDoubleQuotedStringContent(escapedContent);
    }
}
