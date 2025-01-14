package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;

public class TreeLeaf implements TreeComponent {

    JigType jigType;

    public TreeLeaf(JigType jigType) {
        this.jigType = jigType;
    }

    @Override
    public String name() {
        return jigType.typeAlias().asTextOrIdentifierSimpleText();
    }

    @Override
    public String href() {
        return "#" + jigType.identifier().fullQualifiedName();
    }

    @Override
    public boolean isDeprecated() {
        return jigType.isDeprecated();
    }
}
