package com.axeldev;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

public class PhpHeredocToNowdocWithEscapingIntention extends PsiElementBaseIntentionAction {

    private static final String FAMILY_NAME    = "Convert HEREDOC/NOWDOC";
    public static final  String INTENTION_NAME = "Convert HEREDOC to NOWDOC with escaping";

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
        try {
            if (!PhpStringUtil.isPhpHeredoc(psiElement)) return;
            PsiElement parentPsi = psiElement.getParent();
            if (!(parentPsi instanceof StringLiteralExpression)) return;
            StringLiteralExpression phpDoubleQuotedStringLiteralPsi = convertPhpHeredocToNowdoc(psiElement);
            if (phpDoubleQuotedStringLiteralPsi == null) return;
            parentPsi.replace(phpDoubleQuotedStringLiteralPsi);
        } catch (PhpStringUtilOperationException ex) {
            try {
                HintManager.getInstance().showErrorHint(editor, ex.getMessage());
            }
            // silent npe at com.intellij.codeInsight.hint.HintManagerImpl when launching intention from unit test
            catch (Exception ignored) {}
        }
    }

    private StringLiteralExpression convertPhpHeredocToNowdoc(PsiElement psiElement) throws PhpStringUtilOperationException {
        String stringContent = PhpStringUtil.getPhpSimpleHeredocContentUnescaped(psiElement);
        String heredocIdentifier = PhpStringUtil.getPhpHeredocIdentifier(psiElement);
        return PhpStringUtil.createPhpNowdocPsiFromContent(psiElement.getProject(), stringContent, heredocIdentifier);
    }

}
