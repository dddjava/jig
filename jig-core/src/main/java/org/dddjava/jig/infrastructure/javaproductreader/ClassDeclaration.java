package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.unit.JigMethodDeclaration;

import java.util.Collection;

/**
 * クラス定義
 * typesとmembersをとりまとめる
 */
public record ClassDeclaration(JigTypeHeader jigTypeHeader,
                               Collection<JigFieldHeader> jigFieldHeaders,
                               Collection<JigMethodDeclaration> jigMethodDeclarations) {

}
