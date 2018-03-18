package jig.domain.model.tag;

import jig.domain.model.thing.Name;

import java.util.List;

public enum Tag {
    SERVICE,
    REPOSITORY,
    DATASOURCE,
    MAPPER,
    ENUM,
    ENUM_PARAMETERIZED {
        @Override
        public boolean matches(Tag tag) {
            return ENUM == tag || super.matches(tag);
        }
    },
    ENUM_POLYMORPHISM {
        @Override
        public boolean matches(Tag tag) {
            return ENUM == tag || super.matches(tag);
        }
    },
    ENUM_BEHAVIOUR {
        @Override
        public boolean matches(Tag tag) {
            return ENUM == tag || super.matches(tag);
        }
    },
    IDENTIFIER,
    NUMBER,
    DATE,
    TERM,
    COLLECTION,
    MAPPER_METHOD;


    public boolean matches(Tag tag) {
        return this == tag;
    }

    public boolean architecture() {
        return this == SERVICE || this == REPOSITORY;
    }

    public static void registerTag(TagRepository tagRepository, Name className) {
        // TODO 各々のenumに判定させる
        if (className.value().endsWith("Repository")) {
            tagRepository.register(className, Tag.REPOSITORY);
        }
    }

    public static void registerTag(TagRepository tagRepository, Name className, String annotationDescriptor) {
        // TODO 各々のenumに判定させる
        switch (annotationDescriptor) {
            case "Lorg/springframework/stereotype/Service;":
                tagRepository.register(className, Tag.SERVICE);
                break;
            case "Lorg/springframework/stereotype/Repository;":
                tagRepository.register(className, Tag.DATASOURCE);
                break;
            case "Lorg/apache/ibatis/annotations/Mapper;":
                tagRepository.register(className, Tag.MAPPER);
                break;
            default:
                break;
        }
    }

    public static void registerTag(TagRepository tagRepository, Name className, List<String> fieldDescriptors) {
        // TODO 各々のenumに判定させる
        if (fieldDescriptors.size() == 1) {
            String descriptor = fieldDescriptors.get(0);

            switch (descriptor) {
                case "Ljava/lang/String;":
                    tagRepository.register(className, IDENTIFIER);
                    break;
                case "Ljava/math/BigDecimal;":
                    tagRepository.register(className, NUMBER);
                    break;
                case "Ljava/util/List;":
                    tagRepository.register(className, COLLECTION);
                    break;
                case "Ljava/time/LocalDate;":
                    tagRepository.register(className, DATE);
                    break;
            }
        } else if (fieldDescriptors.size() == 2) {
            String field1 = fieldDescriptors.get(0);
            String field2 = fieldDescriptors.get(1);
            if (field1.equals(field2) && field1.equals("Ljava/time/LocalDate;")) {
                tagRepository.register(className, TERM);
            }
        }
    }
}
