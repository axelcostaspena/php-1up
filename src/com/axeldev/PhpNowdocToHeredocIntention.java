package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class PhpNowdocToHeredocIntention extends PsiElementBaseIntentionAction {

    private static final String FAMILY_NAME    = "Convert HEREDOC/NOWDOC";
    public static final  String INTENTION_NAME = "Convert NOWDOC to HEREDOC (may change semantics)";

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
        StringLiteralExpression phpHeredocPsi = convertPhpNowdocToHeredoc(psiElement);
        if (phpHeredocPsi == null) return;
        parentPsi.replace(phpHeredocPsi);
    }

    private StringLiteralExpression convertPhpNowdocToHeredoc(PsiElement psiElement) {
        String rawStringContent = PhpStringUtil.getPhpNowdocContent(psiElement);
        String escapedStringContent = rawStringContent.replaceAll("\\\\", "\\\\\\\\");
        String heredocIdentifier = PhpStringUtil.getPhpNowdocIdentifier(psiElement);
        return PhpStringUtil.createPhpHeredocPsiFromEscapedContent(psiElement.getProject(), escapedStringContent, heredocIdentifier);
    }

}
