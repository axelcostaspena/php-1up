package com.axeldev;

import com.google.common.base.Function;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.PhpWorkaroundUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;

public class PhpHeredocToNowdocWithEscapingIntention extends PsiElementBaseIntentionAction {

    private static final String FAMILY_NAME                               = "Convert HEREDOC/NOWDOC";
    public static final String INTENTION_NAME_NO_VARS       = "Convert HEREDOC to NOWDOC with escaping";
    public static final String INTENTION_NAME_EMBEDDED_VARS = "Convert HEREDOC to NOWDOC with escaping and variable concatenation";
    private static final String MESSAGE_HEREDOC_CONTAINS_DELIMITER_ITSELF = "Cannot perform refactoring.\nUnescaped heredoc content contains the heredoc delimiter itself.";
    private static final char   CHAR_NEWLINE                              = '\n';
    private static final char   CHAR_SINGLE_QUOTE                         = '\'';
    private static final char   CHAR_DOT                                  = '.';

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
        if (stringLiteralExpression == null || !PhpStringUtil.isPhpHeredoc(stringLiteralExpression) || PhpStringUtil.isPhpEmptyHeredoc(stringLiteralExpression)) return false;
        String intentionText = PhpStringUtil.isPhpComplexHeredoc(stringLiteralExpression) ? INTENTION_NAME_EMBEDDED_VARS :
            INTENTION_NAME_NO_VARS;
        this.setText(intentionText);
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        try {
            StringLiteralExpression heredocStringLiteralExpression = PhpStringUtil.findPhpStringLiteralExpression(psiElement, PhpStringUtil.StringType.Heredoc);
            if (heredocStringLiteralExpression == null) return;
            PhpExpression nowdocStringLiteralExpression = convertPhpHeredocToNowdocAndVariablesConcatenation(heredocStringLiteralExpression);
            if (nowdocStringLiteralExpression == null) return;
            heredocStringLiteralExpression.replace(nowdocStringLiteralExpression);
        } catch (PhpHeredocToNowdocWithEscapingIntentionException ex) {
            try {
                HintManager.getInstance().showErrorHint(editor, ex.getMessage());
            }
            // silent npe at com.intellij.codeInsight.hint.HintManagerImpl when launching intention from unit test
            catch (NullPointerException ignored) {}
        }
    }

    private PhpExpression convertPhpHeredocToNowdocAndVariablesConcatenation(StringLiteralExpression stringLiteralExpression) throws PhpHeredocToNowdocWithEscapingIntentionException {
        final String heredocIdentifier = PhpStringUtil.getPhpHeredocIdentifier(stringLiteralExpression);
        final List<String> stringFragmentsWhichContainHeredocIdentifier = PhpStringUtil.mapPhpHeredocContent(stringLiteralExpression, new Function<String, String>() {
            @Override
            public String apply(String rawFragmentContent) {
                String unescapedContent = PhpStringUtil.unescapePhpHeredocContent(rawFragmentContent);
                String escapedContent = PhpStringUtil.escapePhpNowdocContent(unescapedContent);
                return PhpStringUtil.checkEscapedNowdocContentContainsIdentifierItself(escapedContent, heredocIdentifier) ? "invalid" : null;
            }
        }, null);
        if (stringFragmentsWhichContainHeredocIdentifier.size() > 0) {
            throw new PhpHeredocToNowdocWithEscapingIntentionException(MESSAGE_HEREDOC_CONTAINS_DELIMITER_ITSELF);
        }
        final String heredocStart = "<<<" + CHAR_SINGLE_QUOTE + heredocIdentifier + CHAR_SINGLE_QUOTE + CHAR_NEWLINE;
        // append newline to avoid PHP concatenation operator on the same line invalidating the nowdoc end tag
        final String heredocEnd = CHAR_NEWLINE + heredocIdentifier + CHAR_NEWLINE;
        List<String> stringAndVariableList = PhpStringUtil.mapPhpHeredocContent(stringLiteralExpression, new Function<String, String>() {
            @Override
            public String apply(String rawFragmentContent) {
                String unescapedContent = PhpStringUtil.unescapePhpHeredocContent(rawFragmentContent);
                String singleQuoteContentEscaped = PhpStringUtil.escapePhpNowdocContent(unescapedContent);
                return heredocStart + singleQuoteContentEscaped + heredocEnd;
            }
        }, new Function<ASTNode, String>() {
            @Override
            public String apply(ASTNode embeddedExpression) {
                return PhpStringUtil.cleanupStringEmbeddedExpression(embeddedExpression);
            }
        });
        final String concatenationSequence = " " + CHAR_DOT + " ";
        // trim the concatenation expression to remove unneeded trailing newline character after nowdoc literal
        String stringAndExpressionConcatenation = StringUtils.join(stringAndVariableList, concatenationSequence).trim();
        if (stringAndExpressionConcatenation.isEmpty()) return null;
        return PhpPsiElementFactory.createPhpPsiFromText(stringLiteralExpression.getProject(), PhpExpression.class, stringAndExpressionConcatenation);
    }

    private class PhpHeredocToNowdocWithEscapingIntentionException extends Exception {
        public PhpHeredocToNowdocWithEscapingIntentionException(String message) {
            super(message);
        }
    }
}
