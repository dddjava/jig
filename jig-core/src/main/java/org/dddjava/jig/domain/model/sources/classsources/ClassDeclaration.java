package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.sources.JigTypeBuilder;

public record ClassDeclaration(JigTypeBuilder jigTypeBuilder, JigTypeHeader jigTypeHeader) {
    public JigType build() {
        return jigTypeBuilder.build(jigTypeHeader);
    }
}
