package com.axeldev;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Php1UpLightCodeInsightFixtureTestCase extends LightCodeInsightFixtureTestCase {

    private static class MyDescriptor extends DefaultLightProjectDescriptor {
        @Override
        public Sdk getSdk() {
            return JavaSdk.getInstance().createJdk("1.7", new File(getProjectRootPath(), "mockJDK-1.7").getPath(), false);
        }
    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new MyDescriptor();
    }

    @Override
    protected String getTestDataPath() {
        File sourceRoot = getProjectRootPath();
        return new File(new File(sourceRoot, "testData"), getClass().getName()).getPath();
    }

    private static File getProjectRootPath() {
        String testPath = PathManager.getJarPathForClass(PhpHeredocToNowdocIntention.class);
        return new File(testPath, "../../..");
    }

    protected void launchPhpIntentionTest(String testName, String intentionName) {
        myFixture.configureByFile("before" + testName + ".php");
        IntentionAction intention = myFixture.getAvailableIntention(intentionName);
        if (intention == null) {
            throw new AssertionError("Intention \"" + intentionName + "\" is not available at specified document position");
        }
        myFixture.launchAction(intention);
        myFixture.checkResultByFile("after" + testName + ".php");
    }

}
