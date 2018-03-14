package jig.domain.model.thing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NameTest {

    @ParameterizedTest
    @MethodSource
    void shortText(String fullName, String shortText) {
        Name hoge = new Name(fullName);

        assertThat(hoge.shortText()).isEqualTo(shortText);
    }

    static Stream<Arguments> shortText() {
        return Stream.of(
                Arguments.of("hoge", "hoge"),
                Arguments.of("hoge.fuga", "fuga"),
                Arguments.of("hoge.fuga.Piyo", "Piyo"),
                Arguments.of("hoge.fuga$foo", "fuga$foo"),
                Arguments.of("hoge.fuga(aaa.bbb.ccc,hoge.fuga)", "fuga(ccc,fuga)")
        );
    }
}