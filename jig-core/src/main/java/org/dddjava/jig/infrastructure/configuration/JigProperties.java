package org.dddjava.jig.infrastructure.configuration;

public class JigProperties {

    OutputOmitPrefix outputOmitPrefix;
    String businessRulePattern;

    public JigProperties(String businessRulePattern, OutputOmitPrefix outputOmitPrefix) {
        this.businessRulePattern = businessRulePattern;
        this.outputOmitPrefix = outputOmitPrefix;
    }

    public OutputOmitPrefix getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public String getBusinessRulePattern() {
        return businessRulePattern;
    }
}
