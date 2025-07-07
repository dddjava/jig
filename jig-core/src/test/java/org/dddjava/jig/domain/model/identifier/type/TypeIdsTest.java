package org.dddjava.jig.domain.model.identifier.type;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.data.types.TypeIds;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeIdsTest {

    @ParameterizedTest
    @MethodSource
    void test(List<String> identifiers, String simpleText) {
        TypeIds sut = identifiers.stream().map(TypeId::valueOf).collect(TypeIds.collector());

        assertEquals(simpleText, sut.asSimpleText());
    }

    static Stream<Arguments> test() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), "[]"),
                Arguments.of(Collections.singletonList("a.Hoge"), "[Hoge]"),
                Arguments.of(Arrays.asList("a.Hoge", "a.Fuga"), "[Fuga, Hoge]"),
                Arguments.of(Arrays.asList("a.Hoge", "a.Fuga", "a.Fuga"), "[Fuga, Hoge]"),
                Arguments.of(Arrays.asList("a.Hoge", "a.Fuga", "b.Fuga"), "[Fuga, Fuga, Hoge]")
        );
    }

    @ParameterizedTest
    @MethodSource
    void typeIdentifier_asSimpleText(String fullName, String simpleText) {
        TypeId hoge = TypeId.valueOf(fullName);

        assertEquals(simpleText, hoge.asSimpleText());
    }

    static Stream<Arguments> typeIdentifier_asSimpleText() {
        return Stream.of(
                Arguments.of("hoge", "hoge"),
                Arguments.of("hoge.fuga", "fuga"),
                Arguments.of("hoge.fuga.Piyo", "Piyo"),
                Arguments.of("hoge.fuga$foo", "fuga$foo")
        );
    }

    @ParameterizedTest
    @MethodSource
    void typeIdentifier_packageIdentifier(String fullName, String packageName) {
        TypeId hoge = TypeId.valueOf(fullName);

        assertEquals(PackageId.valueOf(packageName), hoge.packageIdentifier());
    }

    static Stream<Arguments> typeIdentifier_packageIdentifier() {
        return Stream.of(
                Arguments.of("hoge", "(default)"),
                Arguments.of("hoge.fuga", "hoge"),
                Arguments.of("hoge.fuga.Piyo", "hoge.fuga"),
                Arguments.of("hoge.fuga$foo", "hoge")
        );
    }
}