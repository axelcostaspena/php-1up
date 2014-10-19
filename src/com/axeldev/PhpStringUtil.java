package com.axeldev;

import com.google.common.base.Function;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PhpStringUtil {
    private static final String MESSAGE_HEREDOC_CONTAINS_DELIMITER_ITSELF = "Cannot perform refactoring.\nUnescaped heredoc content contains the heredoc delimiter itself.";
    private static final char   CHAR_VERTICAL_TAB                         = (char) 11;
    private static final char   CHAR_ESC                                  = (char) 27;
    private static final char   CHAR_NEWLINE                              = '\n';
    private static final char   CHAR_CARRIAGE_RETURN                      = '\r';
    private static final char   CHAR_TAB                                  = '\t';
    private static final char   CHAR_FORM_FEED                            = '\f';
    private static final char   CHAR_BACKSLASH                            = '\\';
    private static final char   CHAR_DOUBLE_QUOTE                         = '"';
    private static final char   CHAR_SINGLE_QUOTE                         = '\'';
    private static final char   CHAR_LEFT_SQUARE_BRACKET                  = '[';
    private static final char   CHAR_RIGHT_SQUARE_BRACKET                 = ']';
    private static final char   CHAR_DOLLAR                               = '$';
    private static final char   CHAR_LCASE_E                              = 'e';
    private static final char   CHAR_LCASE_F                              = 'f';
    private static final char   CHAR_LCASE_N                              = 'n';
    private static final char   CHAR_LCASE_R                              = 'r';
    private static final char   CHAR_LCASE_T                              = 't';
    private static final char   CHAR_LCASE_V                              = 'v';
    private static final char   CHAR_LCASE_X                              = 'x';
    private static final String REGEXP_CHAR_IS_OCTAL                      = "[0-7]";
    private static final String REGEXP_CHAR_IS_HEX                        = "[0-9A-Fa-f]";
    private static final String REGEXP_PHP_IDENTIFIER                     = "[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*";
    private static final String REGEXP_PHP_OCTAL_INTEGER                  = "\\A0[0-9]+\\z";

    static boolean isPhpDoubleQuotedEmptyString(PsiElement psiElement) {
        return psiElement.getText().equals("\"\"");
    }

    static boolean isPhpDoubleQuotedComplexString(PsiElement psiElement) {
        ASTNode astNode = psiElement.getNode();
        if (astNode == null) return false;
        ASTNode[] children = astNode.getChildren(null);
        return children != null && children.length > 1;
    }

    static boolean isPhpSingleQuotedString(PsiElement psiElement) {
        ASTNode astNode = psiElement.getNode();
        return astNode != null && astNode.getElementType() == PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE;
    }

    static boolean isPhpHeredoc(PsiElement psiElement) {
        ASTNode astNode = psiElement.getNode();
        if (astNode == null) return false;
        ASTNode parent = astNode.getTreeParent();
        if (parent == null) return false;
        ASTNode firstChild = parent.getFirstChildNode();
        return firstChild.getElementType() == PhpTokenTypes.HEREDOC_START && !firstChild.getText().contains(Character.toString(CHAR_SINGLE_QUOTE));
    }

    static boolean isPhpHeredocWithEmbeddedExpression(PsiElement psiElement) {
        if (!isPhpHeredoc(psiElement)) return false;
        ASTNode astNode = psiElement.getNode();
        ASTNode parent = astNode.getTreeParent();
        TokenSet tokenSetNonEmbeddedExpressionContents = TokenSet.create(PhpTokenTypes.HEREDOC_START, PhpTokenTypes.HEREDOC_END, PhpTokenTypes.HEREDOC_CONTENTS, PhpTokenTypes.ESCAPE_SEQUENCE);
        // heredoc has expressions embedded if there are more children than only delimiters and text content children
        return parent.getChildren(null).length > parent.getChildren(tokenSetNonEmbeddedExpressionContents).length;
    }

    static boolean isPhpNowdoc(PsiElement psiElement) {
        ASTNode astNode = psiElement.getNode();
        if (astNode == null) return false;
        ASTNode parent = astNode.getTreeParent();
        if (parent == null) return false;
        ASTNode firstChild = parent.getFirstChildNode();
        return firstChild.getElementType() == PhpTokenTypes.HEREDOC_START && firstChild.getText().contains(Character.toString(CHAR_SINGLE_QUOTE));
    }

    static PsiElement getPhpDoubleQuotedStringExpression(PsiElement psiElement) {
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

    static String getPhpDoubleQuotedSimpleStringUnescapedContent(PsiElement psiElement) {
        String phpStringLiteral = psiElement.getText();
        String escapedContent = phpStringLiteral.substring(1, phpStringLiteral.length() - 1);
        return unescapePhpDoubleQuotedStringContent(escapedContent);
    }

    /**
     * Gets the child nodes of a PHP double quoted string psiElement and maps them to a List of String values. Allows to
     * specify a callback function for processing string literal fragments, and other for embedded variables and
     * expressions. Delimiter double quotes are omitted since their presence is constant.
     *
     * @param psiElement               The PHP double quoted string literal whose nodes are wanted to map
     * @param stringFragmentMapper     A Function implementation which gets a String from the ASTNode of any string
     *                                 literal fragment on the PHP string. Any node which lead to a null return value
     *                                 will be omitted from the result.
     * @param embeddedExpressionMapper A Function implementation which gets a String from the ASTNode of any variable or
     *                                 expression embedded on the PHP string. Any node which lead to a null return value
     *                                 will be omitted from the result.
     * @return A List of String objects containing the results of sequentially applying the PHP string pieces to the
     * provided Function implementations as determined by the node type.
     */
    public static List<String> mapPhpDoubleQuotedComplexStringContent(PsiElement psiElement, Function<ASTNode, String> stringFragmentMapper, Function<ASTNode, String> embeddedExpressionMapper) {
        ASTNode astNode = psiElement.getNode();
        if (astNode == null) return null;
        ASTNode[] children = astNode.getChildren(null);
        // complex strings always have more than one child node
        if (children.length <= 1) return null;
        List<String> map = new ArrayList<String>();
        for (ASTNode childNode : children) {
            IElementType pieceType = childNode.getElementType();
            // skip delimiter quotes
            if (pieceType == PhpTokenTypes.chLDOUBLE_QUOTE || pieceType == PhpTokenTypes.chRDOUBLE_QUOTE) continue;
            if (pieceType == PhpTokenTypes.STRING_LITERAL) {
                // the ASTNode is a piece of textual content of the string
                if (stringFragmentMapper != null) {
                    String stringFragmentResult = stringFragmentMapper.apply(childNode);
                    if (stringFragmentResult != null) {
                        map.add(stringFragmentResult);
                    }
                }
            } else {
                // the ASTNode is a variable or expression embedded in the string
                if (embeddedExpressionMapper != null) {
                    String embeddedExpressionResult = embeddedExpressionMapper.apply(childNode);
                    if (embeddedExpressionResult != null) {
                        map.add(embeddedExpressionResult);
                    }
                }
            }
        }
        return map;
    }

    static String getPhpSingleQuotedStringUnescapedContent(PsiElement psiElement) {
        String phpStringLiteralText = psiElement.getText();
        String escapedContent = phpStringLiteralText.substring(1, phpStringLiteralText.length() - 1);
        return unescapePhpSingleQuotedStringContent(escapedContent);
    }

    static String getPhpHeredocUnescapedContent(PsiElement psiElement) {
        PsiElement parentPsi = psiElement.getParent();
        List<String> stringContentFragments = mapPhpHeredocContent(parentPsi, new Function<ASTNode, String>() {
            @Override
            public String apply(ASTNode astNode) {
                return astNode.getText();
            }
        }, null);
        return unescapePhpHeredocContent(StringUtils.join(stringContentFragments, null));
    }

    /**
     * Gets the child nodes of a PHP heredoc psiElement and maps them to a List of String values. Allows to specify a
     * callback function for processing string literal fragments, and other for embedded variables and expressions.
     * Delimiter identifiers are omitted since their presence is constant.
     *
     * @param psiElement               The PHP heredoc literal whose nodes are wanted to map
     * @param stringFragmentMapper     A Function implementation which gets a String from the ASTNode of any string
     *                                 literal fragment on the PHP heredoc. Escape sequences are processed as separate
     *                                 string literal fragments because of PHP Open API implementation. Any node which
     *                                 lead to a null return value will be omitted from the result.
     * @param embeddedExpressionMapper A Function implementation which gets a String from the ASTNode of any variable or
     *                                 expression embedded on the PHP heredoc. Any node which lead to a null return
     *                                 value will be omitted from the result.
     * @return A List of String objects containing the results of sequentially applying the PHP string pieces to the
     * provided Function implementations as determined by the node type.
     */
    public static List<String> mapPhpHeredocContent(PsiElement psiElement, Function<ASTNode, String> stringFragmentMapper, Function<ASTNode, String> embeddedExpressionMapper) {
        ASTNode astNode = psiElement.getNode();
        if (astNode == null) return null;
        ASTNode[] children = astNode.getChildren(null);
        // complex strings always have more than one child node
        if (children.length <= 1) return null;
        List<String> map = new ArrayList<String>();
        for (ASTNode childNode : children) {
            IElementType pieceType = childNode.getElementType();
            // skip delimiter quotes
            if (pieceType == PhpTokenTypes.HEREDOC_START || pieceType == PhpTokenTypes.HEREDOC_END) continue;
            if (pieceType == PhpTokenTypes.HEREDOC_CONTENTS || pieceType == PhpTokenTypes.ESCAPE_SEQUENCE) {
                // the ASTNode is a piece of textual content of the string
                if (stringFragmentMapper != null) {
                    String stringFragmentResult = stringFragmentMapper.apply(childNode);
                    if (stringFragmentResult != null) {
                        map.add(stringFragmentResult);
                    }
                }
            } else {
                // the ASTNode is a variable or expression embedded in the string
                if (embeddedExpressionMapper != null) {
                    String embeddedExpressionResult = embeddedExpressionMapper.apply(childNode);
                    if (embeddedExpressionResult != null) {
                        map.add(embeddedExpressionResult);
                    }
                }
            }
        }
        return map;
    }

    static String getPhpNowdocUnescapedContent(PsiElement psiElement) {
        String escapedContent = psiElement.getParent().getNode().getChildren(TokenSet.create(PhpTokenTypes.HEREDOC_CONTENTS))[0].getText();
        return unescapePhpNowdocContent(escapedContent);
    }

    static String getPhpHeredocIdentifier(PsiElement psiElement) {
        return getPhpHeredocOrNowdocIdentifier(psiElement);
    }

    static String getPhpNowdocIdentifier(PsiElement psiElement) {
        return getPhpHeredocOrNowdocIdentifier(psiElement);
    }

    private static String getPhpHeredocOrNowdocIdentifier(PsiElement psiElement) {
        String heredocStart = psiElement.getParent().getNode().getChildren(TokenSet.create(PhpTokenTypes.HEREDOC_START))[0].getText();
        Pattern phpIdentifierPattern = Pattern.compile(REGEXP_PHP_IDENTIFIER);
        Matcher phpIdentifierMatcher = phpIdentifierPattern.matcher(heredocStart);
        return phpIdentifierMatcher.find() ? phpIdentifierMatcher.group(0) : null;
    }

    static String unescapePhpDoubleQuotedStringContent(String escapedContent) {
        return unescapePhpDoubleQuotedLikeStringContent(escapedContent, true);
    }

    private static String unescapePhpDoubleQuotedLikeStringContent(String escapedContent, boolean doubleQuotedIsEscaped) {
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
                            case CHAR_DOLLAR:
                                unescapedContentBuffer.append(currentChar);
                                break;
                            case CHAR_DOUBLE_QUOTE:
                                if (!doubleQuotedIsEscaped) {
                                    unescapedContentBuffer.append(CHAR_BACKSLASH);
                                }
                                unescapedContentBuffer.append(currentChar);
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

    static String unescapePhpSingleQuotedStringContent(String escapedContent) {
        return escapedContent
            .replace("\\\\", Character.toString(CHAR_BACKSLASH))
            .replace("\\'", Character.toString(CHAR_SINGLE_QUOTE));
    }

    static String unescapePhpHeredocContent(String escapedContent) {
        return unescapePhpDoubleQuotedLikeStringContent(escapedContent, false);
    }

    static String unescapePhpNowdocContent(String escapedContent) {
        // nowdoc string literals' content is exact, no escaping is possible
        return escapedContent;
    }

    static String escapePhpDoubleQuotedStringContent(String unescapedContent) {
        // all allowed escape sequences in a double quoted string must be escaped with a backslash
        // see http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double
        String escapeSequencesEscaped = unescapedContent.replaceAll("(\\\\(?:n|r|t|v|e|f|\\\\|\\$|'|[0-7]{1,3}|x[0-9A-Fa-f]{1,2}|\\z))", "\\\\$1");
        // a PHP variable identifier is defined by the regexp `[a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*`
        // see http://php.net/manual/en/language.variables.basics.php
        String variablesEscaped = escapeSequencesEscaped
            .replaceAll("(\\$[a-zA-Z_\\x7f-\\xff])", "\\\\$1")
            .replaceAll("\\{\\$", "{\\\\\\$");
        return variablesEscaped.replace(Character.toString(CHAR_DOUBLE_QUOTE), "\\\"");
    }

    static String escapePhpSingleQuotedStringContent(String unescapedContent) {
        return unescapedContent.replaceAll("('|\\\\(?=')|\\\\\\z)", "\\\\$1");
    }

    static String escapePhpHeredocContent(String unescapedContent) {
        // all escape sequences allowed in a double quoted string are valid but the double quote itself
        /* see http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double and
         * http://php.net/manual/en/language.types.string.php#language.types.string.syntax.heredoc */
        String escapeSequencesEscaped = unescapedContent.replaceAll("(\\\\(?:n|r|t|v|e|f|\\\\|\\$|'|[0-7]{1,3}|x[0-9A-Fa-f]{1,2}|\\z))", "\\\\$1");
        // a PHP variable identifier is defined by the regexp `[a-zA-Z_\x7f-\xff][a-zA-Z0-9_\x7f-\xff]*`
        // see http://php.net/manual/en/language.variables.basics.php
        return escapeSequencesEscaped
            .replaceAll("(\\$[a-zA-Z_\\x7f-\\xff])", "\\\\$1")
            .replaceAll("\\{\\$", "{\\\\\\$");
    }

    static String escapePhpNowdocContent(String unescapedContent) {
        // nowdoc string literals' content is exact, no escaping is possible
        return unescapedContent;
    }

    static String cleanupStringEmbeddedExpression(ASTNode astNode) {
        ASTNode[] children = astNode.getChildren(null);
        if (children.length == 3 &&
            children[0].getElementType() == PhpTokenTypes.chLBRACE &&
            children[children.length - 1].getElementType() == PhpTokenTypes.chRBRACE) {
            // it's a variable or expression which was wrapped in curly braces in the string
            String expression = astNode.getText();
            // remove braces and return the expression as-is
            return expression.substring(1, expression.length() - 1);
        } else if (children[0].getPsi() instanceof ArrayAccessExpression) {
            /* It's an array access expression, and since it's the only child node it isn't wrapped in curly braces.
             * It has for sure an identifier part, a left square bracket, an index expression which may be using an
             * unquoted string identifier, and a right square bracket */
            ASTNode[] arrayAccessExpressionChildren = children[0].getChildren(null);
            String arrayIdentifier = arrayAccessExpressionChildren[0].getText();
            ASTNode arrayAccessExpressionIndex = arrayAccessExpressionChildren[2];
            ASTNode[] arrayAccessExpressionIndexChildren = arrayAccessExpressionIndex.getChildren(null);
            String arrayRawAccessIndex = arrayAccessExpressionIndex.getText();
            String arrayAccessIndex;
            /* If array access expression is not surrounded with braces and the array index is an identifier,
             * then it's using the unquoted key syntax. Surround the index with quotes.
             * See http://php.net/manual/en/language.types.string.php#language.types.string.parsing */
             /* Explicitly test for the identifier being an octal sequence, which is interpreted as an identifier by the
              * PHP parser but as an integer offset by the PhpStorm parser.
              * See https://youtrack.jetbrains.com/issue/WI-25187 */
            // TODO remove `|| arrayRawAccessIndex.matches("\\A0+[1-9]+[0-9]*\\z")` when WI-25187 gets fixed
            if (arrayAccessExpressionIndexChildren.length == 1 && (
                arrayAccessExpressionIndexChildren[0].getElementType() == PhpTokenTypes.IDENTIFIER ||
                    arrayRawAccessIndex.matches(REGEXP_PHP_OCTAL_INTEGER)
            )) {
                arrayAccessIndex = CHAR_SINGLE_QUOTE + arrayRawAccessIndex + CHAR_SINGLE_QUOTE;
            } else {
                arrayAccessIndex = arrayRawAccessIndex;
            }
            return arrayIdentifier + CHAR_LEFT_SQUARE_BRACKET + arrayAccessIndex + CHAR_RIGHT_SQUARE_BRACKET;
        } else {
            /* if expression is embedded without braces and it's not array access expression, then it's a simple
             * variable or an object property accessing */
            return astNode.getText();
        }
    }

    static StringLiteralExpression createPhpDoubleQuotedStringPsiFromContent(Project project, String unescapedContent) {
        String escapedContent = escapePhpDoubleQuotedStringContent(unescapedContent);
        String phpStringLiteral = CHAR_DOUBLE_QUOTE + escapedContent + CHAR_DOUBLE_QUOTE;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

    static StringLiteralExpression createPhpSingleQuotedStringPsiFromContent(Project project, String unescapedContent) {
        String escapedContent = escapePhpSingleQuotedStringContent(unescapedContent);
        String phpStringLiteral = CHAR_SINGLE_QUOTE + escapedContent + CHAR_SINGLE_QUOTE;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

    static StringLiteralExpression createPhpHeredocPsiFromContent(Project project, String unescapedContent, String heredocIdentifier) {
        String escapedContent = escapePhpHeredocContent(unescapedContent);
        String phpStringLiteral = "<<<" + heredocIdentifier + CHAR_NEWLINE + escapedContent + CHAR_NEWLINE + heredocIdentifier;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

    static StringLiteralExpression createPhpNowdocPsiFromContent(Project project, String unescapedContent, String nowdocIdentifier) throws PhpStringUtilOperationException {
        String escapedContent = escapePhpNowdocContent(unescapedContent);
        /* if the nowdoc identifier itself matches an exact line inside the escaped content, nowdoc will be cropped and
         * following contents will be lost */
        if (escapedContent.matches("(?ms).*?^" + nowdocIdentifier + "$.*")) {
            throw new PhpStringUtilOperationException(MESSAGE_HEREDOC_CONTAINS_DELIMITER_ITSELF);
        }
        String phpStringLiteral = "<<<" + CHAR_SINGLE_QUOTE + nowdocIdentifier + CHAR_SINGLE_QUOTE + CHAR_NEWLINE + escapedContent + CHAR_NEWLINE + nowdocIdentifier;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

}
