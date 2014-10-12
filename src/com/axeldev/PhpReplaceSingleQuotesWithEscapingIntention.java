package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

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
        return PhpWorkaroundUtil.isIntentionAvailable(psiElement) && PhpStringUtil.isPhpSingleQuotedString(psiElement);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (!PhpStringUtil.isPhpSingleQuotedString(psiElement)) return;
        PsiElement parentPsi = psiElement.getParent();
        if (!(parentPsi instanceof StringLiteralExpression)) return;
        StringLiteralExpression phpDoubleQuotedStringLiteralPsi = convertPhpSingleQuotedStringToDoubleQuotedString(psiElement);
        if (phpDoubleQuotedStringLiteralPsi == null) return;
        parentPsi.replace(phpDoubleQuotedStringLiteralPsi);
    }

    private StringLiteralExpression convertPhpSingleQuotedStringToDoubleQuotedString(PsiElement psiElement) {
        String stringContent = PhpStringUtil.getPhpSingleQuotedStringUnescapedContent(psiElement);
        return PhpStringUtil.createPhpDoubleQuotedStringPsiFromContent(psiElement.getProject(), stringContent);
    }

}
