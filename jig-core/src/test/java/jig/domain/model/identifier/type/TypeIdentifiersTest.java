package jig.domain.model.identifier.type;

import jig.domain.model.identifier.namespace.PackageIdentifier;
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
    void test(List<String> identifiers, String simpleText, String text) {
        TypeIdentifiers sut = identifiers.stream().map(TypeIdentifier::new).collect(TypeIdentifiers.collector());

        assertThat(sut.asSimpleText()).isEqualTo(simpleText);
        assertThat(sut.asText()).isEqualTo(text);
    }

    static Stream<Arguments> test() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), "", ""),
                Arguments.of(Collections.singletonList("a.hoge"), "hoge", "a.hoge"),
                Arguments.of(Arrays.asList("a.hoge", "a.fuga"), "hoge,fuga", "a.hoge,a.fuga"),
                Arguments.of(Arrays.asList("a.hoge", "a.fuga", "a.fuga"), "hoge,fuga", "a.hoge,a.fuga"),
                Arguments.of(Arrays.asList("a.hoge", "a.fuga", "b.fuga"), "hoge,fuga", "a.hoge,a.fuga,b.fuga")
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

        assertThat(hoge.packageIdentifier()).isEqualTo(new PackageIdentifier(packageName));
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