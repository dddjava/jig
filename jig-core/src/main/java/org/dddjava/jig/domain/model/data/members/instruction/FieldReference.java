package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * フィールドのポインタ
 *
 * もしかしたらFieldIdentifierなるものかもしれない
 */
public record FieldReference(TypeIdentifier declaringType, TypeIdentifier fieldTypeIdentifier, String name) {
}
