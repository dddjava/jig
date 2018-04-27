package org.dddjava.jig.gradle;

public class JigListExtension {
    String outputDirectory = "build/reports";
    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public void setOutputOmitPrefix(String outputOmitPrefix) {
        this.outputOmitPrefix = outputOmitPrefix;
    }

}
