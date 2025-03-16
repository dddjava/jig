package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;

import java.util.Collection;

/**
 * ASMで読んだクラスの情報
 */
public record ClassDeclaration(JigTypeHeader jigTypeHeader,
                               Collection<JigFieldHeader> jigFieldHeaders,
                               Collection<JigMethodDeclaration> jigMethodDeclarations) {
}
