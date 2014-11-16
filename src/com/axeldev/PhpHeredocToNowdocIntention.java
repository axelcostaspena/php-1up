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
        if (!PhpWorkaroundUtil.isIntentionAvailable(psiElement)) return false;
        /* search for the closest heredoc or nowdoc and show the intention only if the closest one is an heredoc, this
         * way we avoid showing intentions whose target is not clear */
        StringLiteralExpression stringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, EnumSet.of(PhpStringUtil.StringType.Heredoc, PhpStringUtil.StringType.Nowdoc));
        return stringLiteralExpression != null && PhpStringUtil.isPhpHeredoc(stringLiteralExpression);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        StringLiteralExpression heredocStringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, PhpStringUtil.StringType.Heredoc);
        if (heredocStringLiteralExpression == null) return;
        StringLiteralExpression nowdocStringLiteralExpression = convertPhpHeredocToNowdoc(heredocStringLiteralExpression);
        if (nowdocStringLiteralExpression == null) return;
        heredocStringLiteralExpression.replace(nowdocStringLiteralExpression);
    }

     private StringLiteralExpression convertPhpHeredocToNowdoc(StringLiteralExpression stringLiteralExpression) {
        String rawStringContent = PhpStringUtil.getPhpHeredocContent(stringLiteralExpression);
        String escapedStringContent = rawStringContent.replaceAll("\\\\\\\\", "\\\\");
        String heredocIdentifier = PhpStringUtil.getPhpHeredocIdentifier(stringLiteralExpression);
        return PhpStringUtil.createPhpNowdocPsiFromEscapedContent(stringLiteralExpression.getProject(), escapedStringContent, heredocIdentifier);
    }

}
