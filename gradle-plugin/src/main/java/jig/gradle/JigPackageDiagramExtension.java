package jig.gradle;

public class JigPackageDiagramExtension {
    String packagePattern = ".*.domain.model";
    String outputDiagramName = "build/reports/output.png";
    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";
    int depth = -1;

    String getPackagePattern() {
        return packagePattern;
    }

    void setPackagePattern(String packagePattern) {
        this.packagePattern = packagePattern;
    }

    String getOutputDiagramName() {
        return outputDiagramName;
    }

    void setOutputDiagramName(String outputDiagramName) {
        this.outputDiagramName = outputDiagramName;
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
