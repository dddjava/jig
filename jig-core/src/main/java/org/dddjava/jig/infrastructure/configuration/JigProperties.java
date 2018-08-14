package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;

public class JigProperties {
    ModelPattern modelPattern;

    RepositoryPattern repositoryPattern;

    OutputOmitPrefix outputOmitPrefix;

    PackageDepth depth;

    public JigProperties(ModelPattern modelPattern, RepositoryPattern repositoryPattern, OutputOmitPrefix outputOmitPrefix, PackageDepth depth) {
        this.modelPattern = modelPattern;
        this.repositoryPattern = repositoryPattern;
        this.outputOmitPrefix = outputOmitPrefix;
        this.depth = depth;
    }

    public String getModelPattern() {
        return modelPattern.pattern;
    }

    public String getRepositoryPattern() {
        return repositoryPattern.pattern;
    }

    public String getOutputOmitPrefix() {
        return outputOmitPrefix.pattern;
    }

    public int getDepth() {
        return depth.value();
    }
}
