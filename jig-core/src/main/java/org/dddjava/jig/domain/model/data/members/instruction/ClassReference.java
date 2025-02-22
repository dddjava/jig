package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

public record ClassReference(TypeIdentifier typeIdentifier) implements Instruction {
}
