package org.dddjava.jig.domain.model.information.core;


import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.regex.Pattern;

/**
 * コアドメインの判定条件
 */
public class CoreDomainCondition {

    private final Pattern compilerGeneratedClassPattern;
    private final Pattern businessRulePattern;

    public CoreDomainCondition(String domainPattern) {
        this.compilerGeneratedClassPattern = Pattern.compile(".+\\$\\d+");
        this.businessRulePattern = Pattern.compile(domainPattern);
    }

    public boolean isCoreDomain(JigType jigType) {
        String fqn = jigType.id().fullQualifiedName();
        if (fqn.endsWith(".package-info")) return false;
        return businessRulePattern.matcher(fqn).matches()
                && !compilerGeneratedClassPattern.matcher(fqn).matches();
    }

    public CoreDomainJigTypes coreDomainJigTypes(JigTypes jigTypes) {
        var coreJigTypes = jigTypes.filter(this::isCoreDomain);
        return new CoreDomainJigTypes(coreJigTypes);
    }
}
