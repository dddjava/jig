package org.dddjava.jig.domain.model.information.core;


import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import org.dddjava.jig.infrastructure.configuration.JigProperty;
import java.util.regex.Pattern;

/**
 * コアドメインの判定条件
 */
public class CoreDomainCondition {

    private final Pattern businessRulePattern;

    public CoreDomainCondition(String domainPattern) {
        this.businessRulePattern = Pattern.compile(domainPattern);
    }

    public static CoreDomainCondition defaultCondition() {
        return new CoreDomainCondition(JigProperty.PATTERN_DOMAIN.defaultValue());
    }

    private boolean isCoreDomain(JigType jigType) {
        if (jigType.isCompilerGenerated()) return false;

        String fqn = jigType.id().fqn();
        if (fqn.endsWith(".package-info")) return false;
        return businessRulePattern.matcher(fqn).matches();
    }

    public CoreDomainJigTypes coreDomainJigTypes(JigTypes jigTypes) {
        var coreJigTypes = jigTypes.filter(this::isCoreDomain);
        return new CoreDomainJigTypes(coreJigTypes);
    }
}
