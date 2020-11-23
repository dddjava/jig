package org.dddjava.jig.domain.model.jigmodel.jigtype.package_;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;

import java.util.List;

public class JigPackageChildren {

    JigPackage jigPackage;
    List<JigPackage> jigPackages;
    List<JigType> jigTypes;

    public JigPackageChildren(JigPackage jigPackage, List<JigPackage> jigPackages, List<JigType> jigTypes) {
        this.jigPackage = jigPackage;
        this.jigPackages = jigPackages;
        this.jigTypes = jigTypes;
    }

    public List<JigPackage> packages() {
        return jigPackages;
    }

    public List<JigType> types() {
        return jigTypes;
    }

    public boolean hasChild() {
        return !jigPackages.isEmpty() || !jigTypes.isEmpty();
    }
}
