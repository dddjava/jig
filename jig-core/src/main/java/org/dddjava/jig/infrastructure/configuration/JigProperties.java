package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;

public class JigProperties {
    ModelPattern modelPattern;

    RepositoryPattern repositoryPattern;

    OutputOmitPrefix outputOmitPrefix;

    PackageDepth depth;

    boolean jigDebugMode;

    public JigProperties(ModelPattern modelPattern, RepositoryPattern repositoryPattern, OutputOmitPrefix outputOmitPrefix, PackageDepth depth, boolean jigDebugMode) {
        this.modelPattern = modelPattern;
        this.repositoryPattern = repositoryPattern;
        this.outputOmitPrefix = outputOmitPrefix;
        this.depth = depth;
        this.jigDebugMode = jigDebugMode;
    }

    public ModelPattern getModelPattern() {
        return modelPattern;
    }

    public RepositoryPattern getRepositoryPattern() {
        return repositoryPattern;
    }

    public OutputOmitPrefix getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public PackageDepth getDepth() {
        return depth;
    }

    public boolean jigDebugMode() {
        return jigDebugMode;
    }
}
