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
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PhpStringUtil {

    private static final String MESSAGE_NOWDOC_CONTAINS_DELIMITER_ITSELF = "Error on creating NOWDOC string literal.\nNOWDOC content contains the NOWDOC delimiter itself.";

    private static final char CHAR_VERTICAL_TAB         = (char) 11;
    private static final char CHAR_ESC                  = (char) 27;
    private static final char CHAR_NEWLINE              = '\n';
    private static final char CHAR_CARRIAGE_RETURN      = '\r';
    private static final char CHAR_TAB                  = '\t';
    private static final char CHAR_FORM_FEED            = '\f';
    private static final char CHAR_BACKSLASH            = '\\';
    private static final char CHAR_DOUBLE_QUOTE         = '"';
    private static final char CHAR_SINGLE_QUOTE         = '\'';
    private static final char CHAR_LEFT_SQUARE_BRACKET  = '[';
    private static final char CHAR_RIGHT_SQUARE_BRACKET = ']';
    private static final char CHAR_DOLLAR               = '$';
    private static final char CHAR_LCASE_E              = 'e';
    private static final char CHAR_LCASE_F              = 'f';
    private static final char CHAR_LCASE_N              = 'n';
    private static final char CHAR_LCASE_R              = 'r';
    private static final char CHAR_LCASE_T              = 't';
    private static final char CHAR_LCASE_V              = 'v';
    private static final char CHAR_LCASE_X              = 'x';

    private static final String REGEXP_CHAR_IS_OCTAL     = "[0-7]";
    private static final String REGEXP_CHAR_IS_HEX       = "[0-9A-Fa-f]";
    private static final String REGEXP_PHP_IDENTIFIER    = "[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*";
    private static final String REGEXP_PHP_OCTAL_INTEGER = "\\A0[0-9]+\\z";

    /**
     * Represents the four types of string literals existing on PHP.
     *
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string">The PHP documentation:
     * Strings</a>
     */
    public static enum StringType {
        /**
         * Represents the double quoted PHP string literal type.
         *
         * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double">The PHP
         * documentation: Strings, double quoted</a>
         */
        DoubleQuotedString,
        /**
         * Represents the single quoted PHP string literal type.
         *
         * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.single">The PHP
         * documentation: Strings, single quotes</a>
         */
        SingleQuotedString,
        /**
         * Represents the heredoc PHP string literal type.
         *
         * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.heredoc">The
         * PHP documentation: Strings, heredoc syntax</a>
         */
        Heredoc,
        /**
         * Represents the nowdoc quoted PHP string literal type.
         *
         * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.nowdoc">The PHP
         * documentation: Strings, nowdoc syntax</a>
         */
        Nowdoc
    }

    /**
     * Simple contract implementation of Function which receives a String and immediately returns it.
     */
    private static final Function<String, String> stringContract = new Function<String, String>() {
        @Override
        public String apply(String string) {
            return string;
        }
    };

    /**
     * Implementation of Function which receives an ASTNode and returns its text.
     */
    private static final Function<ASTNode, String> astNodeGetText = new Function<ASTNode, String>() {
        @Override
        public String apply(ASTNode astNode) {
            return astNode.getText();
        }
    };

    /**
     * Walks up the PSI element tree from the provided element until it finds an element which satisfies the specified
     * PHP string literal type.
     *
     * @param psiElement The PsiElement from which to start the search.
     * @param stringType A StringType value specifying the target PHP string literal type to match.
     * @return The mathing element if found any, or null of the document root is reached wihtout success or the
     * document is not suitable for PHP element searching.
     */
    static StringLiteralExpression findPhpStringLiteralExpression(PsiElement psiElement, StringType stringType) {
        return findPhpStringLiteralExpression(psiElement, EnumSet.of(stringType));
    }

    /**
     * Walks up the PSI element tree from the provided element until it finds an element which satisfies one of the
     * specified PHP string literal types.
     *
     * @param psiElement    The PsiElement from which to start the search.
     * @param stringTypeSet A set of StringType values specifying the target PHP string literal types to match.
     * @return The mathing element if found any, or null of the document root is reached wihtout success or the
     * document is not suitable for PHP element searching.
     */
    static StringLiteralExpression findPhpStringLiteralExpression(PsiElement psiElement, EnumSet<StringType> stringTypeSet) {
        if (psiElement instanceof PhpFile) return null;
        if (psiElement instanceof StringLiteralExpression) {
            StringLiteralExpression stringLiteralExpression = (StringLiteralExpression) psiElement;
            ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
            IElementType firstChildNodeType = firstChildNode.getElementType();
            if (firstChildNodeType == PhpTokenTypes.STRING_LITERAL || firstChildNodeType == PhpTokenTypes.chLDOUBLE_QUOTE) {
                if (stringTypeSet.contains(StringType.DoubleQuotedString)) {
                    return stringLiteralExpression;
                }
            } else if (firstChildNodeType == PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE) {
                if (stringTypeSet.contains(StringType.SingleQuotedString)) {
                    return stringLiteralExpression;
                }
            } else if (firstChildNodeType == PhpTokenTypes.HEREDOC_START) {
                boolean isNowdoc = firstChildNode.getText().contains(Character.toString(CHAR_SINGLE_QUOTE)),
                    isHeredoc = !isNowdoc;
                if ((stringTypeSet.contains(StringType.Heredoc) && isHeredoc) ||
                    (stringTypeSet.contains(StringType.Nowdoc) && isNowdoc)) {
                    return stringLiteralExpression;
                }
            }
        }
        PsiElement parentPsi = psiElement.getParent();
        return parentPsi != null ? findPhpStringLiteralExpression(parentPsi, stringTypeSet) : null;
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP double quoted string literal.
     *
     * @param stringLiteralExpression The string literal whose type will be checked.
     * @return True if the element is a double quoted string literal, false if it's of another type of string literal.
     */
    static boolean isPhpDoubleQuotedString(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        return firstChildNodeType == PhpTokenTypes.STRING_LITERAL || firstChildNodeType == PhpTokenTypes.chLDOUBLE_QUOTE;
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP double quoted string literal and has no contents.
     *
     * @param stringLiteralExpression The string literal whose type and contents will be checked.
     * @return True if the element is an empty double quoted string literal, false if it has any contents or it's of
     * another type of string literal.
     */
    static boolean isPhpDoubleQuotedEmptyString(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        return firstChildNodeType == PhpTokenTypes.STRING_LITERAL && stringLiteralExpression.getText().equals("\"\"");
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP double quoted string literal which contains any embedded
     * variable or expression.
     *
     * @param stringLiteralExpression The string literal whose type and contents will be checked.
     * @return True if the element is a double quoted string literal and has some embedded expression inside, false if
     * its contents are plain or empty, or if it's of another type of string literal.
     */
    static boolean isPhpDoubleQuotedComplexString(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        return firstChildNodeType == PhpTokenTypes.chLDOUBLE_QUOTE;
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP single quoted string literal.
     *
     * @param stringLiteralExpression The string literal whose type will be checked.
     * @return True if the element is a single quoted string literal, false if it's of another type of string literal.
     */
    static boolean isPhpSingleQuotedString(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        return firstChildNodeType == PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE;
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP single quoted string literal and has no contents.
     *
     * @param stringLiteralExpression The string literal whose type and contents will be checked.
     * @return True if the element is an empty single quoted string literal, false if it has any contents or it's of
     * another type of string literal.
     */
    static boolean isPhpSingleQuotedEmptyString(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        return firstChildNodeType == PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE && stringLiteralExpression.getText().equals("''");
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP heredoc string literal.
     *
     * @param stringLiteralExpression The string literal whose type will be checked.
     * @return True if the element is an heredoc string literal, false if it's of another type of string literal.
     */
    static boolean isPhpHeredoc(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        return firstChildNodeType == PhpTokenTypes.HEREDOC_START && !firstChildNode.getText().contains(Character.toString(CHAR_SINGLE_QUOTE));
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP heredoc string literal and has no contents.
     *
     * @param stringLiteralExpression The string literal whose type and contents will be checked.
     * @return True if the element is an empty heredoc string literal, false if it has any contents or it's of another
     * type of string literal.
     */
    static boolean isPhpEmptyHeredoc(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        if (firstChildNodeType != PhpTokenTypes.HEREDOC_START || firstChildNode.getText().contains(Character.toString(CHAR_SINGLE_QUOTE))) {
            return false;
        }
        ASTNode astNode = stringLiteralExpression.getNode();
        TokenSet tokenSetNonHeredocDelimiters = TokenSet.create(PhpTokenTypes.HEREDOC_START, PhpTokenTypes.HEREDOC_END);
        return astNode.getChildren(null).length == astNode.getChildren(tokenSetNonHeredocDelimiters).length;
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP heredoc string literal which contains any embedded
     * variable or expression.
     *
     * @param stringLiteralExpression The string literal whose type and contents will be checked.
     * @return True if the element is a heredoc string literal and has some embedded expression inside, false if its
     * contents are plain or empty, or if it's of another type of string literal.
     */
    static boolean isPhpComplexHeredoc(StringLiteralExpression stringLiteralExpression) {
        if (!isPhpHeredoc(stringLiteralExpression)) return false;
        ASTNode astNode = stringLiteralExpression.getNode();
        TokenSet tokenSetNonEmbeddedExpressionContents = TokenSet.create(PhpTokenTypes.HEREDOC_START, PhpTokenTypes.HEREDOC_END, PhpTokenTypes.HEREDOC_CONTENTS, PhpTokenTypes.ESCAPE_SEQUENCE);
        // heredoc has expressions embedded if there are more children than only delimiters and text content children
        return astNode.getChildren(null).length > astNode.getChildren(tokenSetNonEmbeddedExpressionContents).length;
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP nowdos string literal.
     *
     * @param stringLiteralExpression The string literal whose type will be checked.
     * @return True if the element is a nowdos string literal, false if it's of another type of string literal.
     */
    static boolean isPhpNowdoc(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        return firstChildNodeType == PhpTokenTypes.HEREDOC_START && firstChildNode.getText().contains(Character.toString(CHAR_SINGLE_QUOTE));
    }

    /**
     * Checks wether a StringLiteralExpression represents a PHP nowdoc string literal and has no contents.
     *
     * @param stringLiteralExpression The string literal whose type and contents will be checked.
     * @return True if the element is an empty nowdoc string literal, false if it has any contents or it's of
     * another type of string literal.
     */
    static boolean isPhpEmptyNowdoc(StringLiteralExpression stringLiteralExpression) {
        ASTNode firstChildNode = stringLiteralExpression.getFirstChild().getNode();
        IElementType firstChildNodeType = firstChildNode.getElementType();
        if (firstChildNodeType != PhpTokenTypes.HEREDOC_START || !firstChildNode.getText().contains(Character.toString(CHAR_SINGLE_QUOTE))) {
            return false;
        }
        ASTNode astNode = stringLiteralExpression.getNode();
        TokenSet tokenSetNonHeredocDelimiters = TokenSet.create(PhpTokenTypes.HEREDOC_START, PhpTokenTypes.HEREDOC_END);
        return astNode.getChildren(null).length == astNode.getChildren(tokenSetNonHeredocDelimiters).length;
    }

    /**
     * Gets the string content fragment on a PHP double quoted string literal.
     * @param stringLiteralExpression The subject double quoted string literal.
     * @return The text resulting from removing the delimiter quotes around the string literal content.
     */
    static String getPhpDoubleQuotedStringContent(StringLiteralExpression stringLiteralExpression) {
        String phpStringLiteral = stringLiteralExpression.getText();
        return phpStringLiteral.substring(1, phpStringLiteral.length() - 1);
    }

    /**
     * Gets the string content fragment on a PHP double quoted string literal and unescapes it. This method is intended
     * for use on string literals without embedded variables or expressions only; embedded expressions will be discarded
     * from the resulting content.
     * @param stringLiteralExpression The subject double quoted string literal.
     * @return The text resulting from removing the delimiter quotes around the string literal content and unescaping
     * the special sequences the string may contain according to the language string parsing specification.
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double">The PHP
     * documentation: Strings, double quoted</a>
     */
    static String getPhpDoubleQuotedSimpleStringUnescapedContent(StringLiteralExpression stringLiteralExpression) {
        String escapedContent = getPhpDoubleQuotedStringContent(stringLiteralExpression);
        return unescapePhpDoubleQuotedStringContent(escapedContent);
    }

    /**
     * Gets the child nodes of a PHP double quoted string StringLiteralExpression and maps them to a List of String
     * values. Allows to specify a callback function for processing string literal fragments, and another for embedded
     * variables and expressions. Delimiter double quotes are omitted since their presence is constant.
     *
     * @param stringLiteralExpression  The PHP double quoted string literal whose nodes are intended to map.
     * @param stringFragmentMapper     A Function implementation which processes the content of the string literal
     *                                 fragment from the PHP string. Any fragment which lead to a null return value
     *                                 will be omitted from the result.
     * @param embeddedExpressionMapper A Function implementation which gets a String from the ASTNode of any variable or
     *                                 expression embedded on the PHP string. Any node which lead to a null return value
     *                                 will be omitted from the result.
     * @return A List of String objects containing the results of sequentially applying the PHP string pieces to the
     * provided Function implementations as determined by the node type.
     */
    public static List<String> mapPhpDoubleQuotedComplexStringContent(StringLiteralExpression stringLiteralExpression, Function<String, String> stringFragmentMapper, Function<ASTNode, String> embeddedExpressionMapper) {
        ASTNode astNode = stringLiteralExpression.getNode();
        if (astNode == null) return null;
        ASTNode[] children = astNode.getChildren(null);
        // if string has single node then it isn't complex
        if (children.length <= 1) return null;
        List<String> map = new ArrayList<String>();
        for (ASTNode childNode : children) {
            IElementType pieceType = childNode.getElementType();
            // skip delimiter quotes
            if (pieceType == PhpTokenTypes.chLDOUBLE_QUOTE || pieceType == PhpTokenTypes.chRDOUBLE_QUOTE) continue;
            if (pieceType == PhpTokenTypes.STRING_LITERAL) {
                // the ASTNode is a piece of textual content of the string
                if (stringFragmentMapper != null) {
                    String stringFragmentResult = stringFragmentMapper.apply(childNode.getText());
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

    /**
     * Gets the string content fragment on a PHP single quoted string literal.
     * @param stringLiteralExpression The subject single quoted string literal.
     * @return The text resulting from removing the delimiter quotes around the string literal content.
     */
    static String getPhpSingleQuotedStringContent(StringLiteralExpression stringLiteralExpression) {
        String phpStringLiteralText = stringLiteralExpression.getText();
        return phpStringLiteralText.substring(1, phpStringLiteralText.length() - 1);
    }

    /**
     * Gets the string content fragment on a PHP single quoted string literal and unescapes it.
     * @param stringLiteralExpression The subject single quoted string literal.
     * @return The text resulting from removing the delimiter quotes around the string literal content and unescaping
     * the special sequences the string may contain according to the language string parsing specification.
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.single">The PHP
     * documentation: Strings, single quoted</a>
     */
    static String getPhpSingleQuotedStringUnescapedContent(StringLiteralExpression stringLiteralExpression) {
        String escapedContent = getPhpSingleQuotedStringContent(stringLiteralExpression);
        return unescapePhpSingleQuotedStringContent(escapedContent);
    }

    /**
     * Gets the string content fragment on a PHP heredoc string literal.
     * @param stringLiteralExpression The subject heredoc string literal.
     * @return The text resulting from removing the heredoc delimiters around the string literal content.
     */
    static String getPhpHeredocContent(StringLiteralExpression stringLiteralExpression) {
        List<String> stringContentFragments = mapPhpHeredocContent(stringLiteralExpression, stringContract, astNodeGetText);
        return StringUtils.join(stringContentFragments, null);
    }

    /**
     * Gets the string content fragment on a PHP heredoc string literal which is guaranteed to contain no embedded
     * expressions.
     * @param stringLiteralExpression The subject heredoc string literal.
     * @return The text resulting from removing the heredoc delimiters around the string literal content.
     */
    private static String getPhpSimpleHeredocContent(StringLiteralExpression stringLiteralExpression) {
        List<String> stringContentFragments = mapPhpHeredocContent(stringLiteralExpression, stringContract, null);
        return StringUtils.join(stringContentFragments, null);
    }

    /**
     * Gets the string content fragment on a PHP heredoc string literal and unescapes it. This method is intended for
     * use on heredocs without embedded variables or expressions only; embedded expressions will be discarded from
     * the resulting content.
     * @param stringLiteralExpression The subject heredoc string literal.
     * @return The text resulting from removing the heredoc delimiters around the string literal content and unescaping
     * the special sequences the string may contain according to the language string parsing specification.
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.heredoc">The PHP
     * documentation: Strings, heredoc</a>
     */
    static String getPhpSimpleHeredocUnescapedContent(StringLiteralExpression stringLiteralExpression) {
        String escapedContent = getPhpSimpleHeredocContent(stringLiteralExpression);
        return unescapePhpHeredocContent(escapedContent);
    }

    /**
     * Gets the child nodes of a PHP heredoc StringLiteralExpression and maps them to a List of String values. Allows
     * to specify a callback function for processing string literal fragments, and another for embedded variables and
     * expressions. Delimiter identifiers are omitted since their presence is constant.
     *
     * @param stringLiteralExpression               The PHP heredoc string literal whose nodes are intended to map.
     * @param stringFragmentMapper     A Function implementation which processes the content of the string literal
     *                                 fragment from the PHP string. Any fragment which lead to a null return value
     *                                 will be omitted from the result.
     * @param embeddedExpressionMapper A Function implementation which gets a String from the ASTNode of any variable or
     *                                 expression embedded on the PHP heredoc. Any node which lead to a null return
     *                                 value will be omitted from the result.
     * @return A List of String objects containing the results of sequentially applying the PHP string pieces to the
     * provided Function implementations as determined by the node type.
     */
    public static List<String> mapPhpHeredocContent(StringLiteralExpression stringLiteralExpression, Function<String, String> stringFragmentMapper, Function<ASTNode, String> embeddedExpressionMapper) {
        ASTNode astNode = stringLiteralExpression.getNode();
        if (astNode == null) return null;
        ASTNode[] children = astNode.getChildren(null);
        StringBuilder stringFragmentAndEscapeSequenceBuffer = new StringBuilder();
        List<String> map = new ArrayList<String>();
        for (ASTNode childNode : children) {
            IElementType pieceType = childNode.getElementType();
            // skip delimiter quotes
            if (pieceType == PhpTokenTypes.HEREDOC_START || pieceType == PhpTokenTypes.HEREDOC_END) continue;
            if (pieceType == PhpTokenTypes.HEREDOC_CONTENTS || pieceType == PhpTokenTypes.ESCAPE_SEQUENCE) {
                // the ASTNode is a piece of textual content of the string
                /* cummulate textual fragments since PHP OpenApi separates escape sequences from plain text - only in
                 * heredocs */
                stringFragmentAndEscapeSequenceBuffer.append(childNode.getText());
            } else {
                // the ASTNode is a variable or expression embedded in the string
                // before processing the expression, process the cummulated string fragment if it has any contents
                if (stringFragmentMapper != null && !stringFragmentAndEscapeSequenceBuffer.toString().isEmpty()) {
                    String stringFragmentResult = stringFragmentMapper.apply(stringFragmentAndEscapeSequenceBuffer.toString());
                    if (stringFragmentResult != null) {
                        map.add(stringFragmentResult);
                    }
                }
                // empty the textual content buffer
                stringFragmentAndEscapeSequenceBuffer.setLength(0);
                // process the embedded expression
                if (embeddedExpressionMapper != null) {
                    String embeddedExpressionResult = embeddedExpressionMapper.apply(childNode);
                    if (embeddedExpressionResult != null) {
                        map.add(embeddedExpressionResult);
                    }
                }
            }
        }
        // after looping all nodes, process the cummulated string content once more, if any
        if (stringFragmentMapper != null && !stringFragmentAndEscapeSequenceBuffer.toString().isEmpty()) {
            String stringFragmentResult = stringFragmentMapper.apply(stringFragmentAndEscapeSequenceBuffer.toString());
            if (stringFragmentResult != null) {
                map.add(stringFragmentResult);
            }
        }
        return map;
    }

    /**
     * Gets the string content fragment on a PHP nowdoc string literal.
     * @param stringLiteralExpression The subject nowdoc string literal.
     * @return The text resulting from removing the nowdoc delimiters around the string literal content.
     */
    static String getPhpNowdocContent(StringLiteralExpression stringLiteralExpression) {
        return stringLiteralExpression.getNode().getChildren(TokenSet.create(PhpTokenTypes.HEREDOC_CONTENTS))[0].getText();
    }

    /**
     * Gets the string content fragment on a PHP nowdoc string literal and unescapes it.
     * @param stringLiteralExpression The subject nowdoc string literal.
     * @return The text resulting from removing the nowdoc delimiters around the string literal content and unescaping
     * the special sequences the string may contain according to the language string parsing specification.
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.nowdoc">The PHP
     * documentation: Strings, nowdoc</a>
     */
    static String getPhpNowdocUnescapedContent(StringLiteralExpression stringLiteralExpression) {
        String escapedContent = getPhpNowdocContent(stringLiteralExpression);
        return unescapePhpNowdocContent(escapedContent);
    }

    /**
     * Gets the identifier part of the heredoc delimiter on an heredoc string literal.
     * @param stringLiteralExpression The subject heredoc string literal.
     * @return The identifier of the heredoc delimiter or null if the provided string literal isn't of the right type.
     */
    static String getPhpHeredocIdentifier(StringLiteralExpression stringLiteralExpression) {
        return getPhpHeredocOrNowdocIdentifier(stringLiteralExpression);
    }

    /**
     * Gets the identifier part of the heredoc or nowdoc delimiter on an heredoc or nowdoc string literal.
     * @param stringLiteralExpression The subject heredoc or nowdoc string literal.
     * @return The identifier of the heredoc or nowdoc delimiter or null if the provided string literal isn't of the
     * right type.
     */
    private static String getPhpHeredocOrNowdocIdentifier(StringLiteralExpression stringLiteralExpression) {
        String heredocStart = stringLiteralExpression.getNode().getChildren(TokenSet.create(PhpTokenTypes.HEREDOC_START))[0].getText();
        Pattern phpIdentifierPattern = Pattern.compile(REGEXP_PHP_IDENTIFIER);
        Matcher phpIdentifierMatcher = phpIdentifierPattern.matcher(heredocStart);
        return phpIdentifierMatcher.find() ? phpIdentifierMatcher.group(0) : null;
    }

    /**
     * Gets the identifier part of the nowdoc delimiter on an nowdoc string literal.
     * @param stringLiteralExpression The subject nowdoc string literal.
     * @return The identifier of the nowdoc delimiter or null if the provided string literal isn't of the right type.
     */
    static String getPhpNowdocIdentifier(StringLiteralExpression stringLiteralExpression) {
        return getPhpHeredocOrNowdocIdentifier(stringLiteralExpression);
    }

    /**
     * Unescape a text fragment coming from a PHP double quoted string literal's content in order to get its real value.
     * @param escapedContent The original text fragment.
     * @return The text resulting from unescaping the special sequences the string may contain according to the language
     * string parsing specification.
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double">The PHP
     * documentation: Strings, double quoted</a>
     */
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

    /**
     * Unescape a text fragment coming from a PHP single quoted string literal's content in order to get its real value.
     * @param escapedContent The original text fragment.
     * @return The text resulting from unescaping the special sequences the string may contain according to the language
     * string parsing specification.
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.single">The PHP
     * documentation: Strings, single quoted</a>
     */
    static String unescapePhpSingleQuotedStringContent(String escapedContent) {
        return escapedContent
            .replace("\\\\", Character.toString(CHAR_BACKSLASH))
            .replace("\\'", Character.toString(CHAR_SINGLE_QUOTE));
    }

    /**
     * Unescape a text fragment coming from a PHP heredoc string literal's content in order to get its real value.
     * @param escapedContent The original text fragment.
     * @return The text resulting from unescaping the special sequences the string may contain according to the language
     * string parsing specification.
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.heredoc">The PHP
     * documentation: Strings, heredoc</a>
     */
    static String unescapePhpHeredocContent(String escapedContent) {
        return unescapePhpDoubleQuotedLikeStringContent(escapedContent, false);
    }

    /**
     * Unescape a text fragment coming from a PHP nowdoc string literal's content in order to get its real value.
     * @param escapedContent The original text fragment.
     * @return The text resulting from unescaping the special sequences the string may contain according to the language
     * string parsing specification.
     * @see <a href="http://php.net/manual/en/language.types.string.php#language.types.string.syntax.nowdoc">The PHP
     * documentation: Strings, nowdoc</a>
     */
    static String unescapePhpNowdocContent(String escapedContent) {
        // nowdoc string literals' content is exact, no escaping is possible
        return escapedContent;
    }

    static String escapePhpDoubleQuotedStringContent(String unescapedContent) {
        // all allowed escape sequences in a double quoted string must be escaped with a backslash
        // see http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double
        String escapeSequencesEscaped = unescapedContent.replaceAll("(\\\\(?=n|r|t|v|e|f|\\\\|\\$|[0-7]{1,3}|x[0-9A-Fa-f]{1,2}|\\z))", "\\\\$1");
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
        String escapeSequencesEscaped = unescapedContent.replaceAll("(\\\\(?=n|r|t|v|e|f|\\\\|\\$|[0-7]{1,3}|x[0-9A-Fa-f]{1,2}))", "\\\\$1");
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

    public static boolean checkEscapedHeredocContentContainsIdentifierItself(String escapedContent, String
        heredocIdentifier) {
        return checkEscapedHeredocOrNowdocContentContainsIdentifierItself(escapedContent, heredocIdentifier);
    }

    private static boolean checkEscapedHeredocOrNowdocContentContainsIdentifierItself(String escapedContent, String heredocOrNowdocIdentifier) {
        return escapedContent.matches("(?ms).*?^" + heredocOrNowdocIdentifier + "$.*");
    }

    public static boolean checkEscapedNowdocContentContainsIdentifierItself(String escapedContent, String
        nowdocIdentifier) {
        return checkEscapedHeredocOrNowdocContentContainsIdentifierItself(escapedContent, nowdocIdentifier);
    }

    static StringLiteralExpression createPhpDoubleQuotedStringPsiFromContent(Project project, String unescapedContent) {
        String escapedContent = escapePhpDoubleQuotedStringContent(unescapedContent);
        return createPhpDoubleQuotedStringPsiFromEscapedContent(project, escapedContent);
    }

    static StringLiteralExpression createPhpDoubleQuotedStringPsiFromEscapedContent(Project project, String escapedContent) {
        String phpStringLiteral = CHAR_DOUBLE_QUOTE + escapedContent + CHAR_DOUBLE_QUOTE;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

    static StringLiteralExpression createPhpSingleQuotedStringPsiFromContent(Project project, String unescapedContent) {
        String escapedContent = escapePhpSingleQuotedStringContent(unescapedContent);
        return createPhpSingleQuotedStringPsiFromEscapedContent(project, escapedContent);
    }

    static StringLiteralExpression createPhpSingleQuotedStringPsiFromEscapedContent(Project project, String escapedContent) {
        String phpStringLiteral = CHAR_SINGLE_QUOTE + escapedContent + CHAR_SINGLE_QUOTE;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

    static StringLiteralExpression createPhpHeredocPsiFromContent(Project project, String unescapedContent, String heredocIdentifier) {
        String escapedContent = escapePhpHeredocContent(unescapedContent);
        return createPhpHeredocPsiFromEscapedContent(project, escapedContent, heredocIdentifier);
    }

    static StringLiteralExpression createPhpHeredocPsiFromEscapedContent(Project project, String escapedContent, String heredocIdentifier) {
        String phpStringLiteral = "<<<" + heredocIdentifier + CHAR_NEWLINE + escapedContent + CHAR_NEWLINE + heredocIdentifier;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

    static StringLiteralExpression createPhpNowdocPsiFromContent(Project project, String unescapedContent, String nowdocIdentifier) throws PhpStringUtilOperationException {
        String escapedContent = escapePhpNowdocContent(unescapedContent);
        /* if the nowdoc identifier itself matches an exact line inside the escaped content, nowdoc will be cropped and
         * following contents will be lost */
        if (checkEscapedNowdocContentContainsIdentifierItself(escapedContent, nowdocIdentifier)) {
            throw new PhpStringUtilOperationException(MESSAGE_NOWDOC_CONTAINS_DELIMITER_ITSELF);
        }
        return createPhpNowdocPsiFromEscapedContent(project, escapedContent, nowdocIdentifier);
    }

    static StringLiteralExpression createPhpNowdocPsiFromEscapedContent(Project project, String escapedContent, String nowdocIdentifier) {
        String phpStringLiteral = "<<<" + CHAR_SINGLE_QUOTE + nowdocIdentifier + CHAR_SINGLE_QUOTE + CHAR_NEWLINE + escapedContent + CHAR_NEWLINE + nowdocIdentifier;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

}
