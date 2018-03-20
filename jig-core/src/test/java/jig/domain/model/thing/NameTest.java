package jig.domain.model.thing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NameTest {

    @ParameterizedTest
    @MethodSource
    void asSimpleText(String fullName, String shortText) {
        Identifier hoge = new Identifier(fullName);

        assertThat(hoge.asSimpleText()).isEqualTo(shortText);
    }

    static Stream<Arguments> asSimpleText() {
        return Stream.of(
                Arguments.of("hoge", "hoge"),
                Arguments.of("hoge.fuga", "fuga"),
                Arguments.of("hoge.fuga.Piyo", "Piyo"),
                Arguments.of("hoge.fuga$foo", "fuga$foo"),
                Arguments.of("hoge.fuga(aaa.bbb.ccc,hoge.fuga)", "fuga(ccc,fuga)")
        );
    }

    @ParameterizedTest()
    @MethodSource
    void asCompressText(String fullName, String text) {
        Identifier hoge = new Identifier(fullName);

        assertThat(hoge.asCompressText()).isEqualTo(text);
    }

    static Stream<Arguments> asCompressText() {
        return Stream.of(
                Arguments.of("hoge", "hoge"),
                Arguments.of("hoge.fuga", "h.fuga"),
                Arguments.of("hoge.fuga.Piyo", "h.f.Piyo"),
                Arguments.of("hoge.fuga$foo", "h.fuga$foo"),
                Arguments.of("hoge.fuga(aaa.bbb.ccc,hoge.fuga)", "h.fuga(a.b.ccc,h.fuga)")
        );
    }
}