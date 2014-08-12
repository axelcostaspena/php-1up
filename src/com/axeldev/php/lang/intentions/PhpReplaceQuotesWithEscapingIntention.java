package com.axeldev.php.lang.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpExpressionCodeFragment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Created by asdf
 */
public class PhpReplaceQuotesWithEscapingIntention extends PsiElementBaseIntentionAction {

    public static final String FAMILY_NAME = "Replace quotes";
    public static final String INTENTION_NAME = "Replace quotes with escaping";
    private static Pattern stringDoubleQuoteContentEscapeCompiledPattern;

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
        PsiFile containingFile = psiElement.getContainingFile();
        //System.out.println(containingFile.getClass());
        if (containingFile instanceof PhpExpressionCodeFragment) {
            return false;
        }
        return isPhpStringLiteralSingleQuote(psiElement);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (!isPhpStringLiteralSingleQuote(psiElement)) return;
        PsiElement parentPsiElement = psiElement.getParent();
        if (!(parentPsiElement instanceof StringLiteralExpression)) return;
        String stringLiteralRealContent = getPhpStringLiteralSingleQuoteRealContent(psiElement);
        StringLiteralExpression phpStringLiteralDoubleQuotePsi = getPhpStringLiteralDoubleQuotePsiFromText(psiElement.getProject(), stringLiteralRealContent);
        parentPsiElement.replace(phpStringLiteralDoubleQuotePsi);
    }

    private boolean isPhpStringLiteralSingleQuote(PsiElement psiElement) {
        ASTNode astNode = psiElement.getNode();
        return astNode != null && astNode.getElementType() == PhpTokenTypes.STRING_LITERAL_SINGLE_QUOTE;
    }

    private String getPhpStringLiteralSingleQuoteRealContent(PsiElement psiElement) {
        String phpStringLiteral = psiElement.getText();
        String stringLiteralUnescapedContent = phpStringLiteral.substring(1, phpStringLiteral.length() - 1);
        String stringLiteralEscapedContent = stringLiteralUnescapedContent.replace("\\\\", "\\").replace("\\'", "'");
        return stringLiteralEscapedContent;
    }

    private StringLiteralExpression getPhpStringLiteralDoubleQuotePsiFromText(Project project, String stringContent) {
        String escapedPhpDoubleQuoteStringContent = EscapePhpStringDoubleQuoteContent(stringContent);
        String phpStringLiteral = "\"" + escapedPhpDoubleQuoteStringContent + "\"";
        return PhpPsiElementFactory.createPhpPsiFromText(project, StringLiteralExpression.class, phpStringLiteral);
    }

    private String EscapePhpStringDoubleQuoteContent(String text) {
        if (stringDoubleQuoteContentEscapeCompiledPattern == null) {
            stringDoubleQuoteContentEscapeCompiledPattern = Pattern.compile("(\\\\(?:n|r|t|v|e|f|\\\\|\\$|\"|[0-7]{1,3}|x[0-9A-Fa-f]{1,2}))");
        }
        return stringDoubleQuoteContentEscapeCompiledPattern.matcher(text).replaceAll("\\$1");
    }
}
