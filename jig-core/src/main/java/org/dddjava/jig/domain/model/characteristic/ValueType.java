package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 値オブジェクト
 */
public enum ValueType {
    IDENTIFIER {
        @Override
        boolean matches(Implementation implementation) {
            if (implementation.isEnum()) return false;
            return implementation.fieldDeclarations().matches(new TypeIdentifier(String.class));
        }
    },
    NUMBER {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.fieldDeclarations().matches(new TypeIdentifier(BigDecimal.class));
        }
    },
    DATE {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.fieldDeclarations().matches(new TypeIdentifier(LocalDate.class));
        }
    },
    TERM {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.fieldDeclarations().matches(new TypeIdentifier(LocalDate.class), new TypeIdentifier(LocalDate.class));
        }
    },
    COLLECTION {
        @Override
        boolean matches(Implementation implementation) {
            FieldDeclarations fieldDeclarations = implementation.fieldDeclarations();
            return fieldDeclarations.matches(new TypeIdentifier(List.class)) || fieldDeclarations.matches(new TypeIdentifier(Set.class));
        }
    };

    public static ValueTypes from(Implementation implementation) {
        List<ValueType> list = Arrays.stream(values())
                .filter(characteristic -> characteristic.matches(implementation))
                .collect(Collectors.toList());
        return new ValueTypes(list);
    }

    abstract boolean matches(Implementation implementation);
}
