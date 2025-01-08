package org.dddjava.jig.domain.model.data.classes.method.instruction;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

/**
 * フィールドのポインタ
 *
 * もしかしたらFieldIdentifierなるものかもしれない
 */
public record FieldReference(TypeIdentifier declaringType, TypeIdentifier fieldTypeIdentifier, String name) {
}
