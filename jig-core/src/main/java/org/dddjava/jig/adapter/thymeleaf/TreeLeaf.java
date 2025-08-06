package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.domain.model.information.types.JigType;

public class TreeLeaf implements TreeComponent {

    JigType jigType;

    public TreeLeaf(JigType jigType) {
        this.jigType = jigType;
    }

    @Override
    public String name() {
        return jigType.label();
    }

    @Override
    public String href() {
        return "#" + jigType.id().fullQualifiedName();
    }

    @Override
    public boolean isDeprecated() {
        return jigType.isDeprecated();
    }
}
