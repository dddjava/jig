package jig.domain.model.identifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class IdentifierAsTextTest {

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

    @Test
    void methodIdentifier_asText() {

        MethodIdentifier methodIdentifier = new MethodIdentifier(
                new TypeIdentifier("hoge.fuga.Piyo"),
                new MethodSignature(
                        "abc",
                        Stream.of("a.Aaa", "b.Bbb", "a.Aaa")
                                .map(TypeIdentifier::new)
                                .collect(Collectors.toList())));

        assertThat(methodIdentifier.asFullText()).isEqualTo("hoge.fuga.Piyo.abc(a.Aaa,b.Bbb,a.Aaa)");
        assertThat(methodIdentifier.asSimpleText()).isEqualTo("abc(Aaa,Bbb,Aaa)");
    }
}