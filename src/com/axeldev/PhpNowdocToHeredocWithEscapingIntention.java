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

public class PhpNowdocToHeredocWithEscapingIntention extends PsiElementBaseIntentionAction {

    private static final String FAMILY_NAME    = "Convert HEREDOC/NOWDOC";
    public static final  String INTENTION_NAME = "Convert NOWDOC to HEREDOC with escaping";

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
        /* search for the closest nowdoc or heredoc and show the intention only if the closest one is a nowdoc, this way
         * we avoid showing intentions whose target is not clear */
        StringLiteralExpression stringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, EnumSet.of(PhpStringUtil.StringType.Nowdoc, PhpStringUtil.StringType.Heredoc));
        return stringLiteralExpression != null && PhpStringUtil.isPhpNowdoc(stringLiteralExpression) && !PhpStringUtil.isPhpEmptyNowdoc(stringLiteralExpression);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        StringLiteralExpression nowdocStringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, PhpStringUtil.StringType.Nowdoc);
        if (nowdocStringLiteralExpression == null) return;
        StringLiteralExpression heredocStringLiteralExpression = convertPhpNowdocToHeredoc(nowdocStringLiteralExpression);
        if (heredocStringLiteralExpression == null) return;
        nowdocStringLiteralExpression.replace(heredocStringLiteralExpression);
    }

    private StringLiteralExpression convertPhpNowdocToHeredoc(StringLiteralExpression stringLiteralExpression) {
        String stringContent = PhpStringUtil.getPhpNowdocUnescapedContent(stringLiteralExpression);
        String heredocIdentifier = PhpStringUtil.getPhpNowdocIdentifier(stringLiteralExpression);
        return PhpStringUtil.createPhpHeredocPsiFromContent(stringLiteralExpression.getProject(), stringContent, heredocIdentifier);
    }

}
