package org.dddjava.jig.domain.model.knowledge.module;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * パッケージ
 */
public record JigPackages(Collection<JigPackage> jigPackages) {

    public List<JigPackage> listPackage() {
        return jigPackages.stream()
                .sorted(Comparator.comparing(JigPackage::fqn))
                .toList();
    }
}
