package com.axeldev;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PhpReplaceQuotesWithConcatenationIntention extends PsiElementBaseIntentionAction {

    public static final String FAMILY_NAME                  = "Replace quotes";
    public static final String INTENTION_NAME_NO_VARS       = "Replace quotes with unescaping";
    public static final String INTENTION_NAME_EMBEDDED_VARS = "Replace quotes with unescaping and variable concatenation";

    @NotNull
    @Override
    public String getFamilyName() {
        return FAMILY_NAME;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (!PhpWorkaroundUtil.isIntentionAvailable(psiElement)) return false;
        PsiElement stringLiteralExpression = PhpStringUtil.getPhpDoubleQuotedStringExpression(psiElement);
        if (stringLiteralExpression == null || PhpStringUtil.isPhpDoubleQuotedEmptyString(psiElement)) return false;
        String intentionText = stringLiteralExpression.getNode().getChildren(null).length > 1 ? INTENTION_NAME_EMBEDDED_VARS : INTENTION_NAME_NO_VARS;
        this.setText(intentionText);
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiElement stringLiteralExpression = PhpStringUtil.getPhpDoubleQuotedStringExpression(psiElement);
        if (stringLiteralExpression == null) return;
        PsiElement singleQuoteExpression = convertPhpDoubleQuotedStringToSingleQuoteAndExpressionConcatenation(project, psiElement, stringLiteralExpression);
        if (singleQuoteExpression == null) return;
        stringLiteralExpression.replace(singleQuoteExpression);
    }

    private PsiElement convertPhpDoubleQuotedStringToSingleQuoteAndExpressionConcatenation(Project project, PsiElement psiElement, PsiElement stringLiteralExpression) {
        ASTNode stringExpressionAstNode = stringLiteralExpression.getNode();
        if (stringExpressionAstNode == null) return null;
        ASTNode[] stringLiteralExpressionPieces = stringExpressionAstNode.getChildren(null);
        if (stringLiteralExpressionPieces.length == 0) return null;
        if (stringLiteralExpressionPieces.length > 1) {
            /* the string literal expression PSI has several children when there are embedded variables or expressions,
             * so the pieces are the left double quote, one or more string pieces and embedded variables or expressions,
             * and the final right double quote */
            List<String> stringAndVariableList = new ArrayList<String>();
            for (ASTNode stringLiteralExpressionPiece : stringLiteralExpressionPieces) {
                IElementType pieceType = stringLiteralExpressionPiece.getElementType();
                // skip delimiter quotes
                if (pieceType == PhpTokenTypes.chLDOUBLE_QUOTE ||
                    pieceType == PhpTokenTypes.chRDOUBLE_QUOTE) { continue; }
                if (pieceType == PhpTokenTypes.STRING_LITERAL) {
                    // ASTNode is a piece of textual content of the string
                    String stringPieceContent = stringLiteralExpressionPiece.getText();
                    String unescapedContent = PhpStringUtil.unescapePhpDoubleQuotedStringContent(stringPieceContent);
                    String singleQuoteEscapedContent = PhpStringUtil.escapePhpSingleQuotedStringContent(unescapedContent);
                    stringAndVariableList.add(PhpStringUtil.CHAR_SINGLE_QUOTE + singleQuoteEscapedContent + PhpStringUtil.CHAR_SINGLE_QUOTE);
                } else {
                    // ASTNode is a variable or expression embedded in the string
                    String variableOrExpression = PhpStringUtil.cleanupStringEmbeddedExpression(stringLiteralExpressionPiece);
                    stringAndVariableList.add(variableOrExpression);
                }
            }
            String stringAndExpressionConcatenation = StringUtils.join(stringAndVariableList, PhpStringUtil.CHAR_DOT);
            if (stringAndExpressionConcatenation == null) return null;
            return PhpPsiElementFactory.createPhpPsiFromText(project, PhpExpression.class, stringAndExpressionConcatenation);
        } else {
            ASTNode singleStringLiteralPiece = stringLiteralExpressionPieces[0];
            String unescapedContent = PhpStringUtil.getPhpDoubleQuotedStringUnescapedContent(singleStringLiteralPiece.getPsi());
            return PhpStringUtil.createPhpSingleQuotedStringPsiFromContent(psiElement.getProject(), unescapedContent);
        }
    }

}
