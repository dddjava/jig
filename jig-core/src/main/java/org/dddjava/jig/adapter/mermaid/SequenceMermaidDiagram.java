package org.dddjava.jig.adapter.mermaid;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;

import java.util.stream.Collectors;

/**
 * メソッド呼び出しのシーケンス図を生成する
 */
public class SequenceMermaidDiagram {

    public static String textFor(JigMethod jigMethod) {
        return mermaidSequenceDiagram(jigMethod.jigMethodDeclaration());
    }

    /**
     * メソッド定義からMermaid形式のシーケンス図を生成する
     */
    public static String mermaidSequenceDiagram(JigMethodDeclaration jigMethodDeclaration) {
        StringBuilder sb = new StringBuilder();
        sb.append("sequenceDiagram\n");

        TypeId caller = jigMethodDeclaration.declaringTypeId();

        jigMethodDeclaration.instructions().methodCallStream()
                // Java標準ライブラリのクラスを除外
                .filter(methodCall -> !methodCall.isJSL())
                .forEach(methodCall -> {
            TypeId callee = methodCall.methodOwner();
            String methodName = methodCall.methodName();
            TypeId returnType = methodCall.returnType();

            // Format arguments
            String args = methodCall.argumentTypes().isEmpty() ?
                    "()" :
                    "(" + methodCall.argumentTypes().stream()
                            .map(TypeId::asSimpleText)
                            .collect(Collectors.joining(", ")) + ")";

            // Add call line
            sb.append("    ").append(caller.asSimpleText())
                    .append(" ->>+ ").append(callee.asSimpleText())
                    .append(": ");

            // Format method name - for constructors, use "new ClassName" instead of "<init>"
            if (methodCall.isConstructor()) {
                sb.append("new ").append(callee.asSimpleText());
            } else {
                sb.append(methodName);
            }

            sb.append(args).append("\n");

            // For void return types, use deactivate instead of return arrow
            if (returnType.asSimpleText().equals("void")) {
                sb.append("    deactivate ").append(callee.asSimpleText()).append("\n");
            } else {
                // Add return line for non-void return types
                sb.append("    ").append(callee.asSimpleText())
                        .append(" -->>- ").append(caller.asSimpleText())
                        .append(": ").append(returnType.asSimpleText()).append("\n");
            }
        });

        return sb.toString();
    }
}
