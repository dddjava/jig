package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.implementation.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageDepth;

public class JigProperties {
    BusinessRuleCondition businessRuleCondition;

    OutputOmitPrefix outputOmitPrefix;

    PackageDepth depth;

    boolean jigDebugMode;

    public JigProperties(BusinessRuleCondition businessRuleCondition, OutputOmitPrefix outputOmitPrefix, PackageDepth depth, boolean jigDebugMode) {
        this.businessRuleCondition = businessRuleCondition;
        this.outputOmitPrefix = outputOmitPrefix;
        this.depth = depth;
        this.jigDebugMode = jigDebugMode;
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
