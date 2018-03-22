package jig.domain.model.characteristic;

import jig.domain.model.specification.Specification;

import java.util.Arrays;

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
    MAPPER_METHOD;

    public static void register(CharacteristicRepository repository, Specification specification) {
        Arrays.stream(values()).forEach(c -> c.registerSpecific(specification, repository));

        specification.methodSpecifications.forEach(methodDescriptor -> {
            if (repository.has(specification.identifier, MAPPER)) {
                repository.register(methodDescriptor.identifier.toIdentifier(), MAPPER_METHOD);
            }
        });
    }

    boolean matches(Specification specification) {
        return false;
    }

    private void registerSpecific(Specification specification, CharacteristicRepository repository) {
        if (matches(specification)) repository.register(specification.identifier, this);
    }
}
