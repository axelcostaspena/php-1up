package com.axeldev;

public abstract class PhpFixtureTestCase extends BaseFixtureTestCase {

    private static final String DATA_FILES_EXTENSION = "php";

    @Override
    public String getTestDataFilesExtension() {
        return DATA_FILES_EXTENSION;
    }
}
