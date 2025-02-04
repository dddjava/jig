package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.sources.JigMemberBuilder;

public record ClassDeclaration(JigMemberBuilder jigMemberBuilder, JigTypeHeader jigTypeHeader) {
    public JigType build() {
        return jigMemberBuilder.build(jigTypeHeader);
    }
}
