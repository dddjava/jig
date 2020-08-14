package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.jigdocument.stationery.LinkPrefix;

public class JigProperties {

    final OutputOmitPrefix outputOmitPrefix;
    final String businessRulePattern;

    final String applicationPattern;
    final String infrastructurePattern;
    final String presentationPattern;

    final LinkPrefix linkPrefix;

    public JigProperties(String businessRulePattern, String applicationPattern, String infrastructurePattern, String presentationPattern, OutputOmitPrefix outputOmitPrefix, LinkPrefix linkPrefix) {
        this.businessRulePattern = businessRulePattern;
        this.infrastructurePattern = infrastructurePattern;
        this.outputOmitPrefix = outputOmitPrefix;
        this.linkPrefix = linkPrefix;
        this.applicationPattern = applicationPattern;
        this.presentationPattern = presentationPattern;
    }

    public OutputOmitPrefix getOutputOmitPrefix() {
        return outputOmitPrefix;
    }

    public String getBusinessRulePattern() {
        return businessRulePattern;
    }

    public LinkPrefix linkPrefix() {
        return linkPrefix;
    }

    public String getInfrastructurePattern() {
        return infrastructurePattern;
    }
}
