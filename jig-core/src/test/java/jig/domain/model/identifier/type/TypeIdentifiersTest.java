package jig.domain.model.identifier.type;

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
}