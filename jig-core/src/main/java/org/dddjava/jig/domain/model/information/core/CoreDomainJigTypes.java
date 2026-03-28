package org.dddjava.jig.domain.model.information.core;

import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CoreDomainにフィルタリングされたJigTypes
 */
public record CoreDomainJigTypes(JigTypes jigTypes) {
    public boolean empty() {
        return jigTypes().empty();
    }

    /**
     * パッケージフィルタのデフォルト値として使用するパッケージ候補を返す。
     */
    public List<String> packageFilterCandidates() {
        // コアドメイン型のパッケージFQNを収集
        Set<String> domainPackages = jigTypes.stream()
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
