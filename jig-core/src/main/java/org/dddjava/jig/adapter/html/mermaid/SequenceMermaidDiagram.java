package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;

import java.util.stream.Collectors;

/**
 * メソッド呼び出しのシーケンス図を生成する
 */
public class SequenceMermaidDiagram {

    public String textFor(JigMethod jigMethod) {
        return mermaidSequenceDiagram(jigMethod.jigMethodDeclaration());
    }

    /**
     * メソッド定義からMermaid形式のシーケンス図を生成する
     */
    public static String mermaidSequenceDiagram(JigMethodDeclaration jigMethodDeclaration) {
        StringBuilder sb = new StringBuilder();
        sb.append("sequenceDiagram\n");

        TypeIdentifier caller = jigMethodDeclaration.declaringTypeIdentifier();

        jigMethodDeclaration.instructions().methodCallStream().forEach(methodCall -> {
            TypeIdentifier callee = methodCall.methodOwner();
            String methodName = methodCall.methodName();
            TypeIdentifier returnType = methodCall.returnType();

            // Format arguments
            String args = methodCall.argumentTypes().isEmpty() ?
                    "()" :
                    "(" + methodCall.argumentTypes().stream()
                            .map(TypeIdentifier::asSimpleText)
                            .collect(Collectors.joining(", ")) + ")";

            // Add call line
            sb.append("    ").append(caller.asSimpleText())
                    .append(" ->>+ ").append(callee.asSimpleText())
                    .append(": ").append(methodName).append(args).append("\n");

            // Add return line
            sb.append("    ").append(callee.asSimpleText())
                    .append(" -->>- ").append(caller.asSimpleText())
                    .append(": ").append(returnType.asSimpleText()).append("\n");
        });

        return sb.toString();
    }
}