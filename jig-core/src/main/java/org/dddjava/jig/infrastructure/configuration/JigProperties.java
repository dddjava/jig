package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition;

public class JigProperties {
    BusinessRuleCondition businessRuleCondition;

    OutputOmitPrefix outputOmitPrefix;

    boolean jigDebugMode;

    public JigProperties(BusinessRuleCondition businessRuleCondition, OutputOmitPrefix outputOmitPrefix, boolean jigDebugMode) {
        this.businessRuleCondition = businessRuleCondition;
        this.outputOmitPrefix = outputOmitPrefix;
        this.jigDebugMode = jigDebugMode;
    }

    public OutputOmitPrefix getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public boolean jigDebugMode() {
        return jigDebugMode;
    }

    public BusinessRuleCondition getBusinessRuleCondition() {
        return businessRuleCondition;
    }
}
