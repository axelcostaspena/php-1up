package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

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
        return PhpWorkaroundUtil.isIntentionAvailable(psiElement) && PhpStringUtil.isPhpNowdoc(psiElement);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (!PhpStringUtil.isPhpNowdoc(psiElement)) return;
        PsiElement parentPsi = psiElement.getParent();
        if (!(parentPsi instanceof StringLiteralExpression)) return;
        StringLiteralExpression phpDoubleQuotedStringLiteralPsi = convertPhpNowDocToHeredoc(psiElement);
        if (phpDoubleQuotedStringLiteralPsi == null) return;
        parentPsi.replace(phpDoubleQuotedStringLiteralPsi);
    }

    private StringLiteralExpression convertPhpNowDocToHeredoc(PsiElement psiElement) {
        String stringContent = PhpStringUtil.getPhpNowdocUnescapedContent(psiElement);
        return PhpStringUtil.createPhpHeredocPsiFromContent(psiElement.getProject(), stringContent, "EOT");
    }

}
