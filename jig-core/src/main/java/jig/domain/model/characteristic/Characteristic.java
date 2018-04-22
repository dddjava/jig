package jig.domain.model.characteristic;

import jig.domain.model.specification.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Characteristic {
    CONTROLLER {
        @Override
        boolean matches(Specification specification) {
            return specification.hasAnnotation("org.springframework.stereotype.Controller")
                    || specification.hasAnnotation("org.springframework.web.bind.annotation.RestController");
        }
    },
    SERVICE {
        @Override
        boolean matches(Specification specification) {
            return specification.hasAnnotation("org.springframework.stereotype.Service");
        }
    },
    REPOSITORY {
        @Override
        boolean matches(Specification specification) {
            return specification.isRepository();
        }
    },
    DATASOURCE {
        @Override
        boolean matches(Specification specification) {
            return specification.hasAnnotation("org.springframework.stereotype.Repository");
        }
    },
    MAPPER {
        @Override
        boolean matches(Specification specification) {
            return specification.hasAnnotation("org.apache.ibatis.annotations.Mapper");
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
            return specification.isEnum() && specification.hasInstanceMethod();
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
            return specification.hasOnlyOneFieldAndFieldTypeIs(String.class);
        }
    },
    NUMBER {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs(BigDecimal.class);
        }
    },
    DATE {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs(LocalDate.class);
        }
    },
    TERM {
        @Override
        boolean matches(Specification specification) {
            return specification.hasTwoFieldsAndFieldTypeAre(LocalDate.class);
        }
    },
    COLLECTION {
        @Override
        boolean matches(Specification specification) {
            return specification.hasOnlyOneFieldAndFieldTypeIs(List.class);
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

    public static TypeCharacteristics resolveCharacteristics(Specification specification) {
        return new TypeCharacteristics(
                specification.typeIdentifier(),
                Arrays.stream(values())
                        .filter(characteristic -> characteristic.matches(specification))
                        .collect(Collectors.toSet()));
    }
}
