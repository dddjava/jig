package jig.domain.model.characteristic;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.specification.ClassDescriptor;
import jig.domain.model.specification.Specification;
import org.objectweb.asm.Opcodes;

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
        boolean isClassName(Identifier identifier) {
            return identifier.value().endsWith("Repository");
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
    ENUM,
    ENUM_PARAMETERIZED {
    },
    ENUM_POLYMORPHISM {
    },
    ENUM_BEHAVIOUR {
    },
    IDENTIFIER,
    NUMBER,
    DATE,
    TERM,
    COLLECTION,
    MAPPER_METHOD;

    public static void register(CharacteristicRepository repository, Specification specification) {
        Arrays.stream(values()).forEach(c -> c.className(specification, repository));

        specification.annotationDescriptors.forEach(descriptor -> {
            Arrays.stream(values()).forEach(c -> c.annotation(descriptor, specification, repository));
        });

        specification.methodSpecifications.forEach(methodDescriptor -> {
            if (repository.has(specification.identifier, MAPPER)) {
                repository.register(methodDescriptor.identifier.toIdentifier(), MAPPER_METHOD);
            }
        });

        if (specification.parentIdentifier.equals(new Identifier(Enum.class))) {
            if ((specification.classAccess & Opcodes.ACC_FINAL) == 0) {
                // finalでないenumは多態
                repository.register(specification.identifier, Characteristic.ENUM_POLYMORPHISM);
            } else if (!specification.fieldDescriptors.isEmpty()) {
                // フィールドがあるenum
                repository.register(specification.identifier, Characteristic.ENUM_PARAMETERIZED);
            } else if (!specification.methodSpecifications.isEmpty()) {
                repository.register(specification.identifier, Characteristic.ENUM_BEHAVIOUR);
            }
            repository.register(specification.identifier, Characteristic.ENUM);
        } else {
            // TODO 各々のenumに判定させる
            if (specification.fieldDescriptors.size() == 1) {
                String descriptor = specification.fieldDescriptors.get(0).toString();

                switch (descriptor) {
                    case "Ljava/lang/String;":
                        repository.register(specification.identifier, IDENTIFIER);
                        break;
                    case "Ljava/math/BigDecimal;":
                        repository.register(specification.identifier, NUMBER);
                        break;
                    case "Ljava/util/List;":
                        repository.register(specification.identifier, COLLECTION);
                        break;
                    case "Ljava/time/LocalDate;":
                        repository.register(specification.identifier, DATE);
                        break;
                }
            } else if (specification.fieldDescriptors.size() == 2) {
                String field1 = specification.fieldDescriptors.get(0).toString();
                String field2 = specification.fieldDescriptors.get(1).toString();
                if (field1.equals(field2) && field1.equals("Ljava/time/LocalDate;")) {
                    repository.register(specification.identifier, TERM);
                }
            }
        }
    }

    private void className(Specification specification, CharacteristicRepository repository) {
        if (isClassName(specification.identifier)) repository.register(specification.identifier, this);
    }

    boolean isClassName(Identifier identifier) {
        return false;
    }

    private void annotation(ClassDescriptor descriptor, Specification specification, CharacteristicRepository repository) {
        if (isAnnotation(descriptor)) repository.register(specification.identifier, this);
    }

    boolean isAnnotation(ClassDescriptor descriptor) {
        return false;
    }
}
