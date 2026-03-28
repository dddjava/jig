package org.dddjava.jig.domain.model.information.core;


import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public CoreDomainJigTypes coreDomainJigTypes(JigTypes jigTypes) {
        var coreJigTypes = jigTypes.filter(this::isCoreDomain);
        return new CoreDomainJigTypes(coreJigTypes);
    }

    /**
     * コアドメイン型が属するパッケージのルート候補を返す。
     * パッケージフィルタのデフォルト値として利用する。
     */
    public List<String> domainPackageFilterCandidates(JigTypes jigTypes) {
        // コアドメイン型のパッケージFQNを収集
        Set<String> domainPackages = jigTypes.stream()
                .filter(this::isCoreDomain)
                .map(jigType -> {
                    String fqn = jigType.id().fqn();
                    int lastDot = fqn.lastIndexOf('.');
                    return lastDot > 0 ? fqn.substring(0, lastDot) : fqn;
                })
                .collect(Collectors.toSet());

        // 最小セット: 他のパッケージの子パッケージを除外
        Set<String> minimal = domainPackages.stream()
                .filter(pkg -> domainPackages.stream()
                        .noneMatch(other -> !other.equals(pkg) && pkg.startsWith(other + ".")))
                .collect(Collectors.toSet());

        // 各最小パッケージの親パッケージをフィルタ候補とする
        return minimal.stream()
                .filter(pkg -> pkg.contains("."))
                .map(pkg -> pkg.substring(0, pkg.lastIndexOf('.')))
                .distinct()
                .sorted()
                .toList();
    }
}
