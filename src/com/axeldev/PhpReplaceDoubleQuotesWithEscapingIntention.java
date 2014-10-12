package com.axeldev;

import com.google.common.base.Function;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PhpReplaceDoubleQuotesWithEscapingIntention extends PsiElementBaseIntentionAction {

    private static final String FAMILY_NAME                  = "Replace quotes";
    public static final String INTENTION_NAME_NO_VARS       = "Replace quotes with escaping";
    public static final String INTENTION_NAME_EMBEDDED_VARS = "Replace quotes with escaping and variable concatenation";
    private static final char   CHAR_SINGLE_QUOTE         = '\'';
    private static final char   CHAR_DOT                  = '.';

    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (!PhpWorkaroundUtil.isIntentionAvailable(psiElement)) return false;
        PsiElement stringLiteralExpression = PhpStringUtil.getPhpDoubleQuotedStringExpression(psiElement);
        if (stringLiteralExpression == null || PhpStringUtil.isPhpDoubleQuotedEmptyString(psiElement)) return false;
        String intentionText = PhpStringUtil.isPhpDoubleQuotedComplexString(stringLiteralExpression) ? INTENTION_NAME_EMBEDDED_VARS : INTENTION_NAME_NO_VARS;
        this.setText(intentionText);
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiElement stringLiteralExpression = PhpStringUtil.getPhpDoubleQuotedStringExpression(psiElement);
        if (stringLiteralExpression == null) return;
        PsiElement singleQuoteExpression = convertPhpDoubleQuotedStringToSingleQuoteAndExpressionConcatenation(project, psiElement, stringLiteralExpression);
        if (singleQuoteExpression == null) return;
        stringLiteralExpression.replace(singleQuoteExpression);
    }

    private PsiElement convertPhpDoubleQuotedStringToSingleQuoteAndExpressionConcatenation(Project project, PsiElement psiElement, PsiElement stringLiteralExpression) {
        if (PhpStringUtil.isPhpDoubleQuotedComplexString(stringLiteralExpression)) {
            List<String> stringAndVariableList = PhpStringUtil.mapPhpDoubleQuotedComplexStringContent(stringLiteralExpression, new Function<ASTNode, String>() {
                @Override
                public String apply(ASTNode stringLiteralFragment) {
                    String doubleQuoteEscapedContent = stringLiteralFragment.getText();
                    String unescapedContent = PhpStringUtil.unescapePhpDoubleQuotedStringContent(doubleQuoteEscapedContent);
                    String singleQuoteEscapedContent = PhpStringUtil.escapePhpSingleQuotedStringContent(unescapedContent);
                    return CHAR_SINGLE_QUOTE + singleQuoteEscapedContent + CHAR_SINGLE_QUOTE;
                }
            }, new Function<ASTNode, String>() {
                @Override
                public String apply(ASTNode embeddedExpression) {
                    return PhpStringUtil.cleanupStringEmbeddedExpression(embeddedExpression);
                }
            });
            String stringAndExpressionConcatenation = StringUtils.join(stringAndVariableList, CHAR_DOT);
            if (stringAndExpressionConcatenation == null) return null;
            return PhpPsiElementFactory.createPhpPsiFromText(project, PhpExpression.class, stringAndExpressionConcatenation);
        } else {
            String unescapedContent = PhpStringUtil.getPhpDoubleQuotedStringUnescapedContent(stringLiteralExpression);
            return PhpStringUtil.createPhpSingleQuotedStringPsiFromContent(psiElement.getProject(), unescapedContent);
        }
    }

}
