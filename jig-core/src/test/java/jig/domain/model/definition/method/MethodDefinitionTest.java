package jig.domain.model.definition.method;

import jig.domain.model.identifier.type.TypeIdentifier;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MethodDefinitionTest {

    @Test
    void methodIdentifier_asText() {

        MethodDefinition methodDefinition = new MethodDefinition(
                new TypeIdentifier("hoge.fuga.Piyo"),
                new MethodSignature(
                        "abc",
                        Stream.of("a.Aaa", "b.Bbb", "a.Aaa")
                                .map(TypeIdentifier::new)
                                .collect(Collectors.toList())));

        assertThat(methodDefinition.asFullText()).isEqualTo("hoge.fuga.Piyo.abc(a.Aaa,b.Bbb,a.Aaa)");
        assertThat(methodDefinition.asSimpleText()).isEqualTo("abc(Aaa,Bbb,Aaa)");
    }
}