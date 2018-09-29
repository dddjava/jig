package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;

public class JigProperties {
    BusinessRuleCondition businessRuleCondition;

    RepositoryPattern repositoryPattern;

    OutputOmitPrefix outputOmitPrefix;

    PackageDepth depth;

    boolean jigDebugMode;

    public JigProperties(BusinessRuleCondition businessRuleCondition, RepositoryPattern repositoryPattern, OutputOmitPrefix outputOmitPrefix, PackageDepth depth, boolean jigDebugMode) {
        this.businessRuleCondition = businessRuleCondition;
        this.repositoryPattern = repositoryPattern;
        this.outputOmitPrefix = outputOmitPrefix;
        this.depth = depth;
        this.jigDebugMode = jigDebugMode;
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

    public BusinessRuleCondition getBusinessRuleCondition() {
        return businessRuleCondition;
    }
}
