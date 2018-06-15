package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MethodDeclarationTest {

    @Test
    void methodIdentifier_asText() {

        MethodDeclaration methodDeclaration = new MethodDeclaration(new TypeIdentifier("hoge.fuga.Piyo"), new MethodSignature(
                "abc",
                new Arguments(Stream.of("a.Aaa", "b.Bbb", "a.Aaa").map(TypeIdentifier::new).collect(Collectors.toList()))), new MethodReturn(new TypeIdentifier("hoge.fuga.Foo")));

        assertThat(methodDeclaration.asFullNameText()).isEqualTo("hoge.fuga.Piyo.abc(a.Aaa, b.Bbb, a.Aaa)");
        assertThat(methodDeclaration.asSignatureSimpleText()).isEqualTo("abc(Aaa, Bbb, Aaa)");
    }
}