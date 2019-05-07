package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition;

public class JigProperties {
    BusinessRuleCondition businessRuleCondition;

    OutputOmitPrefix outputOmitPrefix;

    public JigProperties(BusinessRuleCondition businessRuleCondition, OutputOmitPrefix outputOmitPrefix) {
        this.businessRuleCondition = businessRuleCondition;
        this.outputOmitPrefix = outputOmitPrefix;
    }

    public OutputOmitPrefix getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public BusinessRuleCondition getBusinessRuleCondition() {
        return businessRuleCondition;
    }
}
