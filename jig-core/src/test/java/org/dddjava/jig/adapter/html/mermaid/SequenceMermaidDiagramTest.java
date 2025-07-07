package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequenceMermaidDiagramTest {

    @Test
    void シーケンス図作成() {

        var instructions = new Instructions(List.of(
                new MethodCall(TypeId.valueOf("CalleeClass"), "method", List.of(), TypeId.valueOf("ReturnType"))
        ));

        var jigMethodDeclaration = new JigMethodDeclaration(
                new JigMethodHeader(JigMethodId.from(
                        TypeId.valueOf("CallerClass"),
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

    @Test
    void コンストラクタ呼び出しのシーケンス図作成() {
        // Create instructions with a constructor call
        var instructions = new Instructions(List.of(
                new MethodCall(TypeId.valueOf("CalleeClass"), "<init>", List.of(TypeId.valueOf("Param1"), TypeId.valueOf("Param2")), TypeId.valueOf("void"))
        ));

        var jigMethodDeclaration = new JigMethodDeclaration(
                new JigMethodHeader(JigMethodId.from(
                        TypeId.valueOf("CallerClass"),
                        "callerMethod",
                        List.of()),
                        JigMemberOwnership.INSTANCE,
                        null),
                instructions
        );

        String actual = SequenceMermaidDiagram.mermaidSequenceDiagram(jigMethodDeclaration);

        assertEquals("""
                        sequenceDiagram
                            CallerClass ->>+ CalleeClass: new CalleeClass(Param1, Param2)
                            deactivate CalleeClass
                        """,
                actual
        );
    }

    @Test
    void Java標準ライブラリのクラスを除外したシーケンス図作成() {
        // Create instructions with calls to both regular and Java standard library classes
        var instructions = new Instructions(List.of(
                // Regular class call
                new MethodCall(TypeId.valueOf("CalleeClass"), "method", List.of(), TypeId.valueOf("ReturnType")),
                // Java standard library class call (ArrayList)
                new MethodCall(TypeId.from(ArrayList.class), "add", List.of(TypeId.valueOf("Object")), TypeId.valueOf("boolean"))
        ));

        var jigMethodDeclaration = new JigMethodDeclaration(
                new JigMethodHeader(JigMethodId.from(
                        TypeId.valueOf("CallerClass"),
                        "callerMethod",
                        List.of()),
                        JigMemberOwnership.INSTANCE,
                        null),
                instructions
        );

        String actual = SequenceMermaidDiagram.mermaidSequenceDiagram(jigMethodDeclaration);

        // Only the regular class call should be included in the diagram
        assertEquals("""
                        sequenceDiagram
                            CallerClass ->>+ CalleeClass: method()
                            CalleeClass -->>- CallerClass: ReturnType
                        """,
                actual
        );
    }
}
