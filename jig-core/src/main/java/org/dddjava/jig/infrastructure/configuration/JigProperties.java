package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.jigdocument.stationery.LinkPrefix;

public class JigProperties {

    OutputOmitPrefix outputOmitPrefix;
    String businessRulePattern;
    LinkPrefix linkPrefix;

    public JigProperties(String businessRulePattern, OutputOmitPrefix outputOmitPrefix) {
        this(businessRulePattern, outputOmitPrefix, LinkPrefix.disable());
    }

    public JigProperties(String businessRulePattern, OutputOmitPrefix outputOmitPrefix, LinkPrefix linkPrefix) {
        this.businessRulePattern = businessRulePattern;
        this.outputOmitPrefix = outputOmitPrefix;
        this.linkPrefix = linkPrefix;
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

}
