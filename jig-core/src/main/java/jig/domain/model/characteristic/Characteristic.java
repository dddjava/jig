package jig.domain.model.characteristic;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.specification.ClassDescriptor;
import jig.domain.model.specification.Specification;

import java.util.Arrays;

public enum Characteristic {
    SERVICE {
        @Override
        boolean isAnnotation(ClassDescriptor descriptor) {
            return "Lorg/springframework/stereotype/Service;".equals(descriptor.toString());
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
        boolean isAnnotation(ClassDescriptor descriptor) {
            return "Lorg/springframework/stereotype/Repository;".equals(descriptor.toString());
        }
    },
    MAPPER {
        @Override
        boolean isAnnotation(ClassDescriptor descriptor) {
            return "Lorg/apache/ibatis/annotations/Mapper;".equals(descriptor.toString());
        }
    },
    ENUM {
        @Override
        boolean matches(Specification specification) {
            return specification.isEnum();
        }
    },
    ENUM_PARAMETERIZED {
    },
    ENUM_POLYMORPHISM {
    },
    ENUM_BEHAVIOUR {
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

        specification.annotationDescriptors.forEach(descriptor -> {
            Arrays.stream(values()).forEach(c -> c.annotation(descriptor, specification, repository));
        });

        specification.methodSpecifications.forEach(methodDescriptor -> {
            if (repository.has(specification.identifier, MAPPER)) {
                repository.register(methodDescriptor.identifier.toIdentifier(), MAPPER_METHOD);
            }
        });

        if (specification.parentIdentifier.equals(new Identifier(Enum.class))) {
            if (specification.canExtend()) {
                // finalでないenumは多態
                repository.register(specification.identifier, Characteristic.ENUM_POLYMORPHISM);
            } else if (specification.hasField()) {
                // フィールドがあるenum
                repository.register(specification.identifier, Characteristic.ENUM_PARAMETERIZED);
            } else if (specification.hasMethod()) {
                repository.register(specification.identifier, Characteristic.ENUM_BEHAVIOUR);
            }
        }
    }

    boolean matches(Specification specification) {
        return false;
    }

    private void registerSpecific(Specification specification, CharacteristicRepository repository) {
        if (matches(specification)) repository.register(specification.identifier, this);
    }

    private void annotation(ClassDescriptor descriptor, Specification specification, CharacteristicRepository repository) {
        if (isAnnotation(descriptor)) repository.register(specification.identifier, this);
    }

    boolean isAnnotation(ClassDescriptor descriptor) {
        return false;
    }
}
