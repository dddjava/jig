package org.dddjava.jig.domain.model.information.core;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CoreDomainにフィルタリングされたJigTypes
 */
public record CoreDomainJigTypes(JigTypes jigTypes, CoreDomainCondition coreDomainCondition) {
    public boolean empty() {
        return jigTypes().empty();
    }

    /**
     * パッケージフィルタのデフォルト値として使用するパッケージ候補を返す。
     */
    public List<String> domainPackageRoots() {
        // コアドメイン型のパッケージFQNを収集
        Set<PackageId> domainPackages = jigTypes.stream()
                .map(JigType::packageId)
                .collect(Collectors.toSet());

        // 最小セット: 他のパッケージの子パッケージを除外
        Set<PackageId> minimal = domainPackages.stream()
                .filter(current -> domainPackages.stream()
                        .noneMatch(other -> current.isSubpackageOf(other)))
                .collect(Collectors.toSet());

        // この時点では以下のように domain.model 以下のパッケージが並んでいる可能性がある
        // - xx.domain.model.hoge
        // - xx.domain.model.fuga
        // - xx.domain.model.piyo

        return minimal.stream()
                .map(this::rootCoreDomainPackageOf)
                .distinct()
                .sorted(Comparable::compareTo)
                .map(PackageId::asText)
                .toList();
    }

    private PackageId rootCoreDomainPackageOf(PackageId domainPackage) {
        var optParentPackageId = domainPackage.parentIfExist();
        if (optParentPackageId.isEmpty()) {
            return domainPackage;
        }
        var parentPackageId = optParentPackageId.orElseThrow();
        if (coreDomainCondition.isCoreDomainPackage(parentPackageId)) {
            // 再帰する
            return rootCoreDomainPackageOf(parentPackageId);
        }
        return domainPackage;
    }
}
