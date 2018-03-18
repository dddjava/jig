package jig.domain.model.characteristic;

import jig.domain.model.thing.Name;

import java.util.List;

public enum Characteristic {
    SERVICE,
    REPOSITORY,
    DATASOURCE,
    MAPPER,
    ENUM,
    ENUM_PARAMETERIZED {
        @Override
        public boolean matches(Characteristic characteristic) {
            return ENUM == characteristic || super.matches(characteristic);
        }
    },
    ENUM_POLYMORPHISM {
        @Override
        public boolean matches(Characteristic characteristic) {
            return ENUM == characteristic || super.matches(characteristic);
        }
    },
    ENUM_BEHAVIOUR {
        @Override
        public boolean matches(Characteristic characteristic) {
            return ENUM == characteristic || super.matches(characteristic);
        }
    },
    IDENTIFIER,
    NUMBER,
    DATE,
    TERM,
    COLLECTION,
    MAPPER_METHOD;


    public boolean matches(Characteristic characteristic) {
        return this == characteristic;
    }

    public boolean architecture() {
        return this == SERVICE || this == REPOSITORY;
    }

    public static void registerTag(CharacteristicRepository characteristicRepository, Name className) {
        // TODO 各々のenumに判定させる
        if (className.value().endsWith("Repository")) {
            characteristicRepository.register(className, Characteristic.REPOSITORY);
        }
    }

    public static void registerTag(CharacteristicRepository characteristicRepository, Name className, String annotationDescriptor) {
        // TODO 各々のenumに判定させる
        switch (annotationDescriptor) {
            case "Lorg/springframework/stereotype/Service;":
                characteristicRepository.register(className, Characteristic.SERVICE);
                break;
            case "Lorg/springframework/stereotype/Repository;":
                characteristicRepository.register(className, Characteristic.DATASOURCE);
                break;
            case "Lorg/apache/ibatis/annotations/Mapper;":
                characteristicRepository.register(className, Characteristic.MAPPER);
                break;
            default:
                break;
        }
    }

    public static void registerTag(CharacteristicRepository characteristicRepository, Name className, List<String> fieldDescriptors) {
        // TODO 各々のenumに判定させる
        if (fieldDescriptors.size() == 1) {
            String descriptor = fieldDescriptors.get(0);

            switch (descriptor) {
                case "Ljava/lang/String;":
                    characteristicRepository.register(className, IDENTIFIER);
                    break;
                case "Ljava/math/BigDecimal;":
                    characteristicRepository.register(className, NUMBER);
                    break;
                case "Ljava/util/List;":
                    characteristicRepository.register(className, COLLECTION);
                    break;
                case "Ljava/time/LocalDate;":
                    characteristicRepository.register(className, DATE);
                    break;
            }
        } else if (fieldDescriptors.size() == 2) {
            String field1 = fieldDescriptors.get(0);
            String field2 = fieldDescriptors.get(1);
            if (field1.equals(field2) && field1.equals("Ljava/time/LocalDate;")) {
                characteristicRepository.register(className, TERM);
            }
        }
    }
}
