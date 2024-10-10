package org.dddjava.jig.domain.model.identifier.type;

import org.assertj.core.api.Assertions;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TypeIdentifiersTest {

    @ParameterizedTest
    @MethodSource
    void test(List<String> identifiers, String simpleText) {
        TypeIdentifiers sut = identifiers.stream().map(TypeIdentifier::new).collect(TypeIdentifiers.collector());

        assertThat(sut.asSimpleText()).isEqualTo(simpleText);
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
        TypeIdentifier hoge = new TypeIdentifier(fullName);

        assertThat(hoge.asSimpleText()).isEqualTo(simpleText);
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
        TypeIdentifier hoge = new TypeIdentifier(fullName);

        Assertions.assertThat(hoge.packageIdentifier()).isEqualTo(PackageIdentifier.valueOf(packageName));
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