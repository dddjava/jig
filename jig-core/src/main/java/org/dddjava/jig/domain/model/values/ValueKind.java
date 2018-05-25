package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 値の種類
 */
public enum ValueKind {
    IDENTIFIER {
        @Override
        boolean matches(ByteCode byteCode) {
            if (byteCode.isEnum()) return false;
            return byteCode.fieldDeclarations().matches(new TypeIdentifier(String.class));
        }
    },
    NUMBER {
        @Override
        boolean matches(ByteCode byteCode) {
            return byteCode.fieldDeclarations().matches(new TypeIdentifier(BigDecimal.class));
        }
    },
    DATE {
        @Override
        boolean matches(ByteCode byteCode) {
            return byteCode.fieldDeclarations().matches(new TypeIdentifier(LocalDate.class));
        }
    },
    TERM {
        @Override
        boolean matches(ByteCode byteCode) {
            return byteCode.fieldDeclarations().matches(new TypeIdentifier(LocalDate.class), new TypeIdentifier(LocalDate.class));
        }
    },
    COLLECTION {
        @Override
        boolean matches(ByteCode byteCode) {
            FieldDeclarations fieldDeclarations = byteCode.fieldDeclarations();
            return fieldDeclarations.matches(new TypeIdentifier(List.class)) || fieldDeclarations.matches(new TypeIdentifier(Set.class));
        }
    };

    abstract boolean matches(ByteCode byteCode);
}
