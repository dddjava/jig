package org.dddjava.jig.gradle;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JigConfig {

    String modelPattern = "";

    List<String> documentTypes = new ArrayList<>();
    // exclude は Gradle 拡張固有の利便機能。JigReportsTask が include から減算して最終リストを
    // 解決してからコアへ渡す。jig-core / CLI / properties に exclude の概念は無い（意図的）。
    List<String> documentTypesExclude = new ArrayList<>();

    String outputDirectory = "";

    public List<String> getDocumentTypesExclude() {
        return documentTypesExclude;
    }

    public void setDocumentTypesExclude(List<String> documentTypesExclude) {
        this.documentTypesExclude = documentTypesExclude;
    }

    public String getModelPattern() {
        return modelPattern;
    }

    public void setModelPattern(String modelPattern) {
        this.modelPattern = modelPattern;
    }

    public List<String> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<String> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        if (!Paths.get(outputDirectory).isAbsolute()) {
            throw new IllegalArgumentException("outputDirectoryは絶対パスを指定してください");
        }
        this.outputDirectory = outputDirectory;
    }
}
