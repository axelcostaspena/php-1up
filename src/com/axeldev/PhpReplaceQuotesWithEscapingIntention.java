package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class PhpReplaceQuotesWithEscapingIntention extends PsiElementBaseIntentionAction {

    public static final String FAMILY_NAME = "Replace quotes";
    public static final String INTENTION_NAME = "Replace quotes with escaping";
    public static final char CHAR_DOUBLE_QUOTE = '"';
    public static final char CHAR_SINGLE_QUOTE = '\'';
    public static final char CHAR_BACKSLASH = '\\';

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
        return PhpWorkaroundUtil.isIntentionAvailable(psiElement) && isPhpSingleQuotedString(psiElement);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (!isPhpSingleQuotedString(psiElement)) return;
        PsiElement parentPsi = psiElement.getParent();
        if (!(parentPsi instanceof StringLiteralExpression)) return;
        String stringContent = getPhpSingleQuotedStringContent(psiElement);
        StringLiteralExpression phpDoubleQuotedStringLiteralPsi = createPhpDoubleQuotedStringPsiFromContent(psiElement.getProject(), stringContent);
        parentPsi.replace(phpDoubleQuotedStringLiteralPsi);
    }

    private boolean isPhpSingleQuotedString(PsiElement psiElement) {
        ASTNode astNode = psiElement.getNode();
        return astNode != null && astNode.getElementType() == PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE;
    }

    private String getPhpSingleQuotedStringContent(PsiElement psiElement) {
        String phpStringLiteralText = psiElement.getText();
        String escapedContent = phpStringLiteralText.substring(1, phpStringLiteralText.length() - 1);
        return unescapePhpSingleQuotedStringContent(escapedContent);
    }

    private String unescapePhpSingleQuotedStringContent(String escapedContent) {
        return escapedContent
            .replace("\\\\", Character.toString(CHAR_BACKSLASH))
            .replace("\\'", Character.toString(CHAR_SINGLE_QUOTE));
    }

    private StringLiteralExpression createPhpDoubleQuotedStringPsiFromContent(Project project, String unescapedContent) {
        String escapedContent = escapePhpDoubleQuotedStringContent(unescapedContent);
        String phpStringLiteral = CHAR_DOUBLE_QUOTE + escapedContent + CHAR_DOUBLE_QUOTE;
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

    private String escapePhpDoubleQuotedStringContent(String unescapedContent) {
        // all allowed escape sequences in a double quoted string must be escaped with a backslash
        // see http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double
        String escapeSequencesEscaped = unescapedContent.replaceAll("(\\\\(?:n|r|t|v|e|f|\\\\|\\$|\"|[0-7]{1,3}|x[0-9A-Fa-f]{1,2}|$))", "\\\\$1");
        String doubleQuotesEscaped = escapeSequencesEscaped.replace(Character.toString(CHAR_DOUBLE_QUOTE), "\\\"");
        return doubleQuotesEscaped.replace("$", "\\$");
    }
}
