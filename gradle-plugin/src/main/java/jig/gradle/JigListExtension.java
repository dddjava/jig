package jig.gradle;

public class JigListExtension {
    String outputPath = "build/reports/output.xlsx";

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
}
