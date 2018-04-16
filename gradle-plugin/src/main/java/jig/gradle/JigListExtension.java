package jig.gradle;

public class JigListExtension {
    String outputPath = "build/reports/output.xlsx";
    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public void setOutputOmitPrefix(String outputOmitPrefix) {
        this.outputOmitPrefix = outputOmitPrefix;
    }

}
