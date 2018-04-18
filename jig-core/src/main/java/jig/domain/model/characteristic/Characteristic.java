package jig.domain.model.characteristic;

import jig.domain.model.specification.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public enum Characteristic {
    SERVICE {
        @Override
        boolean matches(Specification specification) {
            return specification.hasAnnotation("Lorg/springframework/stereotype/Service;");
        }
    },
    REPOSITORY {
        @Override
        boolean matches(Specification specification) {
            return specification.typeIdentifier.fullQualifiedName().endsWith("Repository");
        }
    },
    DATASOURCE {
        @Override
        boolean matches(Specification specification) {
            return specification.hasAnnotation("Lorg/springframework/stereotype/Repository;");
        }
    },
    MAPPER {
        @Override
        boolean matches(Specification specification) {
            return specification.hasAnnotation("Lorg/apache/ibatis/annotations/Mapper;");
        }
    },
    ENUM {
        @Override
        boolean matches(Specification specification) {
            return specification.isEnum();
        }
    },
    ENUM_BEHAVIOUR {
        @Override
        boolean matches(Specification specification) {
            return specification.isEnum() && specification.hasInstanceMethod();
        }
    },
    ENUM_PARAMETERIZED {
        @Override
        boolean matches(Specification specification) {
            return specification.isEnum() && specification.hasField();
        }
    },
    ENUM_POLYMORPHISM {
        @Override
        boolean matches(Specification specification) {
            return specification.isEnum() && specification.canExtend();
        }
    },
    IDENTIFIER {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs(String.class);
        }
    },
    NUMBER {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs(BigDecimal.class);
        }
    },
    DATE {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs(LocalDate.class);
        }
    },
    TERM {
        @Override
        boolean matches(Specification specification) {
            return specification.hasTwoFieldsAndFieldTypeAre(LocalDate.class);
        }
    },
    COLLECTION {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs(List.class);
        }
    },
    MODEL {
        @Override
        boolean matches(Specification specification) {
            return specification.isModel();
        }
    };

    boolean matches(Specification specification) {
        return false;
    }

    public static void register(CharacteristicRepository repository, Specification specification) {
        for (Characteristic characteristic : values()) {
            if (characteristic.matches(specification)) {
                repository.register(specification.typeIdentifier, characteristic);
            }
        }
    }
}
