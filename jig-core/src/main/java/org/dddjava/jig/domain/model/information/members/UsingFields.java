package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;

import java.util.Collection;

/**
 * メソッドが使用しているフィールド
 */
public record UsingFields(Collection<JigFieldIdentifier> fieldIds) {

    static UsingFields from(Instructions instructions) {
        return new UsingFields(instructions.fieldReferenceStream().toList());
    }
}
