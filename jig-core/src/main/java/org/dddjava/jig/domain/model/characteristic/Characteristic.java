package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

/**
 * 型の特徴
 */
public enum Characteristic {
    CONTROLLER {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            return typeByteCode.hasAnnotation("org.springframework.stereotype.Controller")
                    || typeByteCode.hasAnnotation("org.springframework.web.bind.annotation.RestController")
                    || typeByteCode.hasAnnotation("org.springframework.web.bind.annotation.ControllerAdvice");
        }
    },
    SERVICE {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            return typeByteCode.hasAnnotation("org.springframework.stereotype.Service");
        }
    },
    REPOSITORY {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            return characterizedTypeFactory.isRepository(typeByteCode);
        }
    },
    DATASOURCE {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            return typeByteCode.hasAnnotation("org.springframework.stereotype.Repository");
        }
    },
    MAPPER {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            // WET: org.dddjava.jig.infrastructure.LocalProject.isMapperClassFile
            return typeByteCode.typeIdentifier().asSimpleText().endsWith("Mapper");
        }
    },
    ENUM {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            return typeByteCode.isEnum();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_BEHAVIOUR {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            return typeByteCode.isEnum() && typeByteCode.hasInstanceMethod();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_PARAMETERIZED {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            return typeByteCode.isEnum() && typeByteCode.hasField();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_POLYMORPHISM {
        @Override
        boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
            return typeByteCode.isEnum() && typeByteCode.canExtend();
        }
    };

    boolean matches(TypeByteCode typeByteCode, CharacterizedTypeFactory characterizedTypeFactory) {
        return false;
    }

}
