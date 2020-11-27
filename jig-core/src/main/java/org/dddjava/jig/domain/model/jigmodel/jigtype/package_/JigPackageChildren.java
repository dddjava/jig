package org.dddjava.jig.domain.model.jigmodel.jigtype.package_;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;

import java.util.List;

public class JigPackageChildren {

    JigPackage jigPackage;
    List<JigPackage> subPackages;
    List<JigType> jigTypes;

    public JigPackageChildren(JigPackage jigPackage, List<JigPackage> subPackages, List<JigType> jigTypes) {
        this.jigPackage = jigPackage;
        this.subPackages = subPackages;
        this.jigTypes = jigTypes;
    }

    public List<JigPackage> packages() {
        return subPackages;
    }

    public List<JigType> types() {
        return jigTypes;
    }

    public boolean hasChild() {
        return !subPackages.isEmpty() || !jigTypes.isEmpty();
    }
}
