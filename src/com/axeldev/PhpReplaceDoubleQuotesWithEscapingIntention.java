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
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
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
        /* search for the closest double quoted string or single quoted string and show the intention only if the
         * closest one is a double quoted one, this way we avoid showing intentions whose target is not clear */
        StringLiteralExpression stringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, EnumSet.of(PhpStringUtil.StringType.DoubleQuotedString, PhpStringUtil.StringType.SingleQuotedString));
        if (stringLiteralExpression == null || !PhpStringUtil.isPhpDoubleQuotedString(stringLiteralExpression) || PhpStringUtil.isPhpDoubleQuotedEmptyString(stringLiteralExpression)) return false;
        String intentionText = PhpStringUtil.isPhpDoubleQuotedComplexString(stringLiteralExpression) ? INTENTION_NAME_EMBEDDED_VARS : INTENTION_NAME_NO_VARS;
        this.setText(intentionText);
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        StringLiteralExpression doubleQuotedStringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, PhpStringUtil.StringType.DoubleQuotedString);
        if (doubleQuotedStringLiteralExpression == null) return;
        PhpExpression singleQuotedStringLiteralExpression = convertPhpDoubleQuotedStringToSingleQuotedStringsAndVariablesConcatenation(doubleQuotedStringLiteralExpression);
        if (singleQuotedStringLiteralExpression == null) return;
        doubleQuotedStringLiteralExpression.replace(singleQuotedStringLiteralExpression);
    }

    private PhpExpression convertPhpDoubleQuotedStringToSingleQuotedStringsAndVariablesConcatenation(StringLiteralExpression stringLiteralExpression) {
        if (PhpStringUtil.isPhpDoubleQuotedComplexString(stringLiteralExpression)) {
            List<String> stringAndVariableList = PhpStringUtil.mapPhpDoubleQuotedComplexStringContent(stringLiteralExpression, new Function<String, String>() {
                @Override
                public String apply(String rawFragmentContent) {
                    String unescapedContent = PhpStringUtil.unescapePhpDoubleQuotedStringContent(rawFragmentContent);
                    String singleQuoteContentEscaped = PhpStringUtil.escapePhpSingleQuotedStringContent(unescapedContent);
                    return CHAR_SINGLE_QUOTE + singleQuoteContentEscaped + CHAR_SINGLE_QUOTE;
                }
            }, new Function<ASTNode, String>() {
                @Override
                public String apply(ASTNode embeddedExpression) {
                    return PhpStringUtil.cleanupStringEmbeddedExpression(embeddedExpression);
                }
            });
            String stringAndExpressionConcatenation = StringUtils.join(stringAndVariableList, " " + CHAR_DOT + " ");
            if (stringAndExpressionConcatenation == null || stringAndExpressionConcatenation.isEmpty()) return null;
            return PhpPsiElementFactory.createPhpPsiFromText(stringLiteralExpression.getProject(), PhpExpression.class, stringAndExpressionConcatenation);
        } else {
            String unescapedContent = PhpStringUtil.getPhpDoubleQuotedSimpleStringUnescapedContent(stringLiteralExpression);
            return PhpStringUtil.createPhpSingleQuotedStringPsiFromContent(stringLiteralExpression.getProject(), unescapedContent);
        }
    }

}
