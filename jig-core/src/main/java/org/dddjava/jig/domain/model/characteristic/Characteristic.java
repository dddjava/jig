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
 * モデルの特徴
 */
public enum Characteristic {
    CONTROLLER {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.hasAnnotation("org.springframework.stereotype.Controller")
                    || implementation.hasAnnotation("org.springframework.web.bind.annotation.RestController");
        }
    },
    SERVICE {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.hasAnnotation("org.springframework.stereotype.Service");
        }
    },
    REPOSITORY {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isRepository();
        }
    },
    DATASOURCE {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.hasAnnotation("org.springframework.stereotype.Repository");
        }
    },
    MAPPER {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.hasAnnotation("org.apache.ibatis.annotations.Mapper");
        }
    },
    ENUM {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isEnum();
        }
    },
    ENUM_BEHAVIOUR {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isEnum() && implementation.hasInstanceMethod();
        }
    },
    ENUM_PARAMETERIZED {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isEnum() && implementation.hasField();
        }
    },
    ENUM_POLYMORPHISM {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isEnum() && implementation.canExtend();
        }
    },
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
    },
    MODEL {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isModel();
        }
    };

    boolean matches(Implementation implementation) {
        return false;
    }

    public static TypeCharacteristics resolveCharacteristics(Implementation implementation) {
        return new TypeCharacteristics(
                implementation.typeIdentifier(),
                Arrays.stream(values())
                        .filter(characteristic -> characteristic.matches(implementation))
                        .collect(Collectors.toSet()));
    }
}
