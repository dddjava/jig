package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequenceMermaidDiagramTest {

    @Test
    void シーケンス図作成() {

        var instructions = new Instructions(List.of(
                new MethodCall(TypeIdentifier.valueOf("CalleeClass"), "method", List.of(), TypeIdentifier.valueOf("ReturnType"))
        ));

        var jigMethodDeclaration = new JigMethodDeclaration(
                new JigMethodHeader(JigMethodIdentifier.from(
                        TypeIdentifier.valueOf("CallerClass"),
                        "callerMethod",
                        List.of()),
                        JigMemberOwnership.INSTANCE,
                        null),
                instructions
        );

        String actual = SequenceMermaidDiagram.mermaidSequenceDiagram(jigMethodDeclaration);

        assertEquals("""
                        sequenceDiagram
                            CallerClass ->>+ CalleeClass: method()
                            CalleeClass -->>- CallerClass: ReturnType
                        """,
                actual
        );
    }
}