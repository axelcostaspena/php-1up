package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class PhpReplaceSingleQuotesWithEscapingIntention extends PsiElementBaseIntentionAction {

    private static final String FAMILY_NAME    = "Replace quotes";
    public static final  String INTENTION_NAME = "Replace quotes with escaping";

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
        if (!PhpWorkaroundUtil.isIntentionAvailable(psiElement)) return false;
        /* search for the closest single quoted string or double quoted string and show the intention only if the
         * closest one is a single quoted one, this way we avoid showing intentions whose target is not clear */
        StringLiteralExpression stringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, EnumSet.of(PhpStringUtil.StringType.SingleQuotedString, PhpStringUtil.StringType.DoubleQuotedString));
        return stringLiteralExpression != null && PhpStringUtil.isPhpSingleQuotedString(stringLiteralExpression) && !PhpStringUtil.isPhpSingleQuotedEmptyString(stringLiteralExpression);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        StringLiteralExpression singleQuotedStringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, PhpStringUtil.StringType.SingleQuotedString);
        if (singleQuotedStringLiteralExpression == null) return;
        StringLiteralExpression doubleQuotedStringLiteralExpression = convertPhpSingleQuotedStringToDoubleQuotedString(singleQuotedStringLiteralExpression);
        if (doubleQuotedStringLiteralExpression == null) return;
        singleQuotedStringLiteralExpression.replace(doubleQuotedStringLiteralExpression);
    }

    private StringLiteralExpression convertPhpSingleQuotedStringToDoubleQuotedString(StringLiteralExpression stringLiteralExpression) {
        String stringContent = PhpStringUtil.getPhpSingleQuotedStringUnescapedContent(stringLiteralExpression);
        return PhpStringUtil.createPhpDoubleQuotedStringPsiFromContent(stringLiteralExpression.getProject(), stringContent);
    }

}
