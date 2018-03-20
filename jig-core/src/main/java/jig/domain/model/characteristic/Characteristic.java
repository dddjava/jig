package jig.domain.model.characteristic;

import jig.domain.model.specification.ClassDescriptor;
import jig.domain.model.specification.Specification;
import jig.domain.model.thing.Name;
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
        boolean isClassName(Name name) {
            return name.value().endsWith("Repository");
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
            if (repository.has(specification.name, MAPPER)) {
                repository.register(methodDescriptor.name, MAPPER_METHOD);
            }
        });

        if (specification.parentName.equals(new Name(Enum.class))) {
            if ((specification.classAccess & Opcodes.ACC_FINAL) == 0) {
                // finalでないenumは多態
                repository.register(specification.name, Characteristic.ENUM_POLYMORPHISM);
            } else if (!specification.fieldDescriptors.isEmpty()) {
                // フィールドがあるenum
                repository.register(specification.name, Characteristic.ENUM_PARAMETERIZED);
            } else if (!specification.methodSpecifications.isEmpty()) {
                repository.register(specification.name, Characteristic.ENUM_BEHAVIOUR);
            }
            repository.register(specification.name, Characteristic.ENUM);
        } else {
            // TODO 各々のenumに判定させる
            if (specification.fieldDescriptors.size() == 1) {
                String descriptor = specification.fieldDescriptors.get(0).toString();

                switch (descriptor) {
                    case "Ljava/lang/String;":
                        repository.register(specification.name, IDENTIFIER);
                        break;
                    case "Ljava/math/BigDecimal;":
                        repository.register(specification.name, NUMBER);
                        break;
                    case "Ljava/util/List;":
                        repository.register(specification.name, COLLECTION);
                        break;
                    case "Ljava/time/LocalDate;":
                        repository.register(specification.name, DATE);
                        break;
                }
            } else if (specification.fieldDescriptors.size() == 2) {
                String field1 = specification.fieldDescriptors.get(0).toString();
                String field2 = specification.fieldDescriptors.get(1).toString();
                if (field1.equals(field2) && field1.equals("Ljava/time/LocalDate;")) {
                    repository.register(specification.name, TERM);
                }
            }
        }
    }

    private void className(Specification specification, CharacteristicRepository repository) {
        if (isClassName(specification.name)) repository.register(specification.name, this);
    }

    boolean isClassName(Name name) {
        return false;
    }

    private void annotation(ClassDescriptor descriptor, Specification specification, CharacteristicRepository repository) {
        if (isAnnotation(descriptor)) repository.register(specification.name, this);
    }

    boolean isAnnotation(ClassDescriptor descriptor) {
        return false;
    }
}
