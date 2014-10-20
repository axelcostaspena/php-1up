package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class PhpHeredocToNowdocIntention extends PsiElementBaseIntentionAction {

    private static final String FAMILY_NAME    = "Convert HEREDOC/NOWDOC";
    public static final  String INTENTION_NAME = "Convert HEREDOC to NOWDOC (may change semantics)";

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
        return PhpWorkaroundUtil.isIntentionAvailable(psiElement) && PhpStringUtil.isPhpHeredoc(psiElement) && !PhpStringUtil.isPhpComplexHeredoc(psiElement);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (!PhpStringUtil.isPhpHeredoc(psiElement)) return;
        PsiElement parentPsi = psiElement.getParent();
        if (!(parentPsi instanceof StringLiteralExpression)) return;
        StringLiteralExpression phpDoubleQuotedStringLiteralPsi = convertPhpHeredocToNowdoc(psiElement);
        if (phpDoubleQuotedStringLiteralPsi == null) return;
        parentPsi.replace(phpDoubleQuotedStringLiteralPsi);
    }

    private StringLiteralExpression convertPhpHeredocToNowdoc(PsiElement psiElement) {
        String rawStringContent = PhpStringUtil.getPhpSimpleHeredocContent(psiElement);
        String escapedStringContent = rawStringContent.replaceAll("\\\\\\\\", "\\\\");
        String heredocIdentifier = PhpStringUtil.getPhpHeredocIdentifier(psiElement);
        return PhpStringUtil.createPhpNowdocPsiFromEscapedContent(psiElement.getProject(), escapedStringContent, heredocIdentifier);
    }

}
