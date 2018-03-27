package jig.domain.model.characteristic;

import jig.domain.model.specification.Specification;

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
            return specification.identifier.value().endsWith("Repository");
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
            return specification.isEnum() && specification.hasMethod();
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
            return specification.hasOnlyOneFieldAndFieldTypeIs("Ljava/lang/String;");
        }
    },
    NUMBER {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs("Ljava/math/BigDecimal;");
        }
    },
    DATE {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs("Ljava/time/LocalDate;");
        }
    },
    TERM {
        @Override
        boolean matches(Specification specification) {
            return specification.hasTwoFieldsAndFieldTypeAre("Ljava/time/LocalDate;");
        }
    },
    COLLECTION {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs("Ljava/util/List;");
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
                repository.register(specification.identifier, characteristic);
            }
        }
    }
}
