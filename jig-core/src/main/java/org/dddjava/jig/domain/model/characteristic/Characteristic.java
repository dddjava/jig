package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * モデルの特徴
 */
public enum Characteristic {
    CONTROLLER {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.hasAnnotation("org.springframework.stereotype.Controller")
                    || implementation.hasAnnotation("org.springframework.web.bind.annotation.RestController");
        }
    },
    SERVICE {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.hasAnnotation("org.springframework.stereotype.Service");
        }
    },
    REPOSITORY {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isRepository();
        }
    },
    DATASOURCE {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.hasAnnotation("org.springframework.stereotype.Repository");
        }
    },
    MAPPER {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.hasAnnotation("org.apache.ibatis.annotations.Mapper");
        }
    },
    ENUM {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isEnum();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_BEHAVIOUR {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isEnum() && implementation.hasInstanceMethod();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_PARAMETERIZED {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isEnum() && implementation.hasField();
        }
    },
    // TODO characteristicじゃなくす
    ENUM_POLYMORPHISM {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isEnum() && implementation.canExtend();
        }
    },
    MODEL {
        @Override
        boolean matches(Implementation implementation) {
            return implementation.isModel();
        }
    };

    boolean matches(Implementation implementation) {
        return false;
    }

    public static TypeCharacteristics resolveCharacteristics(Implementation implementation) {
        return new TypeCharacteristics(
                implementation.typeIdentifier(),
                Arrays.stream(values())
                        .filter(characteristic -> characteristic.matches(implementation))
                        .collect(Collectors.toSet()));
    }
}
