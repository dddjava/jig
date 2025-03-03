package org.dddjava.jig.domain.model.data.unit;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;

import java.util.Collection;

/**
 * クラス定義
 * typesとmembersをとりまとめる
 */
public record ClassDeclaration(JigTypeHeader jigTypeHeader,
                               Collection<JigFieldHeader> jigFieldHeaders,
                               Collection<JigMethodDeclaration> jigMethodDeclarations) {

}
