package org.dddjava.jig.domain.model.information.core;


import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * コアドメインの判定条件
 */
public class CoreDomainCondition {

    public static final String DEFAULT_DOMAIN_PATTERN = ".+\\.domain\\.(model|type)\\..+";

    private final Pattern businessRulePattern;

    public CoreDomainCondition(Optional<String> domainPattern) {
        this.businessRulePattern = Pattern.compile(domainPattern.orElse(DEFAULT_DOMAIN_PATTERN));
    }

    private boolean isCoreDomain(JigType jigType) {
        if (jigType.isCompilerGenerated()) return false;

        String fqn = jigType.id().fqn();
        if (fqn.endsWith(".package-info")) return false;
        return businessRulePattern.matcher(fqn).matches();
    }

    boolean isCoreDomainPackage(PackageId packageId) {
        String fqn = packageId.asText();
        // dummyがダサいが今の指定はクラス名に対する正規表現なのでパッケージにマッチさせようとしたら何かつける必要がある
        return businessRulePattern.matcher(fqn + ".Dummy").matches();
    }

    public CoreDomainJigTypes coreDomainJigTypes(JigTypes jigTypes) {
        var coreJigTypes = jigTypes.filter(this::isCoreDomain);
        return new CoreDomainJigTypes(coreJigTypes, this);
    }
}
