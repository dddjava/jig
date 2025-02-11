package org.dddjava.jig.domain.model.knowledge.validations;

import jakarta.validation.constraints.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import testing.TestSupport;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationsTest {

    record TypeAndDescription(String type, String description) {
    }

    public static Stream<Arguments> name() {
        return Stream.of(
                Arguments.arguments("nonNullField", List.of(
                        new TypeAndDescription("NotNull", "[]"))),
                Arguments.arguments("minAndMax", List.of(
                        new TypeAndDescription("Min", "[value=12]"), new TypeAndDescription("Max", "[value=345]"))),
                Arguments.arguments("email()", List.of(
                        new TypeAndDescription("Email", "[message=めっせーじ, regexp=.+@.+, flags={Flag.CANON_EQ}]")))
        );
    }

    @MethodSource
    @ParameterizedTest
    void name(String memberName, List<TypeAndDescription> expected) {
        var jigType = TestSupport.buildJigType(MySut.class);

        var sut = Validations.validationAnnotatedMembers(jigType);

        var validations = sut.filter(it -> it.memberName().equals(memberName)).toList();

        assertEquals(expected.size(), validations.size());

        for (var i = 0; i < validations.size(); i++) {
            var validation = validations.get(i);
            assertEquals(expected.get(i).type(), validation.annotationType().asSimpleName());
            assertEquals(expected.get(i).description(), validation.annotationDescription());
        }
    }

    private static class MySut {
        @NotNull
        String nonNullField;

        @Min(12)
        @Max(345)
        int minAndMax;

        @Email(message = "めっせーじ", regexp = ".+@.+", flags = Pattern.Flag.CANON_EQ)
        String email() {
            return null;
        }
    }
}