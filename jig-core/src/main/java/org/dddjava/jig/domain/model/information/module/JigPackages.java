package org.dddjava.jig.domain.model.information.module;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public record JigPackages(Collection<JigPackage> jigPackages) {

    public List<JigPackage> listPackage() {
        return jigPackages.stream()
                .sorted(Comparator.comparing(JigPackage::fqn))
                .toList();
    }
}
