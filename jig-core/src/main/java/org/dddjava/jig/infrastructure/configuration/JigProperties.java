package org.dddjava.jig.infrastructure.configuration;

public class JigProperties {
    String modelPattern = ".+\\.domain\\.model\\..+";

    String repositoryPattern = ".+Repository";

    String outputOmitPrefix = ".+\\.(service|domain\\.(model|basic))\\.";

    int depth = -1;

    public String getModelPattern() {
        return modelPattern;
    }

    public void setModelPattern(String modelPattern) {
        this.modelPattern = modelPattern;
    }

    public String getRepositoryPattern() {
        return repositoryPattern;
    }

    public void setRepositoryPattern(String repositoryPattern) {
        this.repositoryPattern = repositoryPattern;
    }

    public String getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public void setOutputOmitPrefix(String outputOmitPrefix) {
        this.outputOmitPrefix = outputOmitPrefix;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
