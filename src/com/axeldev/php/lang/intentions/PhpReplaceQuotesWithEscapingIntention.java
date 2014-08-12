package com.axeldev.php.lang.intentions;

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

/**
 * Created by asdf
 */
public class PhpReplaceQuotesWithEscapingIntention extends PsiElementBaseIntentionAction {

    public static final String FAMILY_NAME = "Replace quotes";
    public static final String INTENTION_NAME = "Replace quotes with escaping";

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
        return isPhpStringLiteralSingleQuote(psiElement);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (!isPhpStringLiteralSingleQuote(psiElement)) return;
        PsiElement parentPsi = psiElement.getParent();
        if (!(parentPsi instanceof StringLiteralExpression)) return;
        String stringLiteralContent = getPhpSingleQuotedStringRealContent(psiElement);
        StringLiteralExpression phpDoubleQuotedStringLiteralPsi = getPhpDoubleQuotedStringLiteralPsiFromText(psiElement.getProject(), stringLiteralContent);
        parentPsi.replace(phpDoubleQuotedStringLiteralPsi);
    }

    private boolean isPhpStringLiteralSingleQuote(PsiElement psiElement) {
        ASTNode astNode = psiElement.getNode();
        return astNode != null && astNode.getElementType() == PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE;
    }

    private String getPhpSingleQuotedStringRealContent(PsiElement psiElement) {
        String phpStringLiteralText = psiElement.getText();
        String unescapedContent = phpStringLiteralText.substring(1, phpStringLiteralText.length() - 1);
        @SuppressWarnings("UnnecessaryLocalVariable")
        String escapedContent = unescapedContent.replace("\\\\", "\\").replace("\\'", "'");
        return escapedContent;
    }

    private StringLiteralExpression getPhpDoubleQuotedStringLiteralPsiFromText(Project project, String stringContent) {
        String escapedPhpDoubleQuoteStringContent = EscapeForPhpDoubleQuotedString(stringContent);
        String phpStringLiteralText = "\"" + escapedPhpDoubleQuoteStringContent + "\"";
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteralText);
    }

    private String EscapeForPhpDoubleQuotedString(String text) {
        // all allowed escape sequences in a double quoted string must be escaped with a backslash
        // see http://php.net/manual/en/language.types.string.php#language.types.string.syntax.double
        String escapeSequencesEscaped = text.replaceAll("(\\\\(?:n|r|t|v|e|f|\\\\|\\$|\"|[0-7]{1,3}|x[0-9A-Fa-f]{1,2}))", "\\\\$1");
        String doubleQuotesEscaped = escapeSequencesEscaped.replace("\"", "\\\"");
        @SuppressWarnings("UnnecessaryLocalVariable")
        String variablesEscaped = doubleQuotesEscaped.replace("$", "\\$");
        return variablesEscaped;
    }
}
