package jig.gradle;

public class JigPackageDiagramExtension {
    String outputDirectory = "build/reports";
    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";
    int depth = -1;

    String getOutputDirectory() {
        return outputDirectory;
    }

    void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    String getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    void setOutputOmitPrefix(String outputOmitPrefix) {
        this.outputOmitPrefix = outputOmitPrefix;
    }

    int getDepth() {
        return depth;
    }

    void setDepth(int depth) {
        this.depth = depth;
    }
}
