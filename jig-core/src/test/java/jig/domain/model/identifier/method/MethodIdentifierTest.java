package jig.domain.model.identifier.method;

import jig.domain.model.identifier.type.TypeIdentifier;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MethodIdentifierTest {

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