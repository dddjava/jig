package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 型の特徴
 */
public enum Characteristic {
    CONTROLLER {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return byteCode.hasAnnotation("org.springframework.stereotype.Controller")
                    || byteCode.hasAnnotation("org.springframework.web.bind.annotation.RestController");
        }
    },
    SERVICE {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return byteCode.hasAnnotation("org.springframework.stereotype.Service");
        }
    },
    REPOSITORY {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return characteristicContext.isRepository(byteCode);
        }
    },
    DATASOURCE {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return byteCode.hasAnnotation("org.springframework.stereotype.Repository");
        }
    },
    MAPPER {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            // WET: org.dddjava.jig.infrastructure.LocalProject.isMapperClassFile
            return byteCode.typeIdentifier().asSimpleText().endsWith("Mapper");
        }
    },
    ENUM {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return byteCode.isEnum();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_BEHAVIOUR {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return byteCode.isEnum() && byteCode.hasInstanceMethod();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_PARAMETERIZED {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return byteCode.isEnum() && byteCode.hasField();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_POLYMORPHISM {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return byteCode.isEnum() && byteCode.canExtend();
        }
    },
    MODEL {
        @Override
        boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
            return characteristicContext.isModel(byteCode);
        }
    };

    boolean matches(ByteCode byteCode, CharacteristicContext characteristicContext) {
        return false;
    }

    public static CharacterizedType resolveCharacteristics(ByteCode byteCode, CharacteristicContext characteristicContext) {
        return new CharacterizedType(
                byteCode.typeIdentifier(),
                Arrays.stream(values())
                        .filter(characteristic -> characteristic.matches(byteCode, characteristicContext))
                        .collect(Collectors.toSet()));
    }
}
