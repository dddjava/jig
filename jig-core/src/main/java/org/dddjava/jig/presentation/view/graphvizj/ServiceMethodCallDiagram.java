package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;

import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

public class ServiceMethodCallDiagram extends GraphvizjView<ServiceAngles> {

    final JapaneseNameFinder japaneseNameFinder;

    public ServiceMethodCallDiagram(JapaneseNameFinder japaneseNameFinder) {
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    protected String graphText(ServiceAngles serviceAngles) {
        List<ServiceAngle> angles = serviceAngles.list();

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (MethodDeclaration methodDeclaration : serviceAngle.userServiceMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
        }

        // メソッドの表示方法
        String labelText = angles.stream()
                .map(angle -> {
                    MethodDeclaration method = angle.method();
                    Node node = Node.of(method);
                    if (method.isLambda()) {
                        node.label("(lambda)").lambda();
                    } else {
                        // ラベルを 和名 + method(ArgumentTypes) : ReturnType にする
                        String methodText = japaneseNameLineOf(method) + method.asSimpleTextWithReturnType();
                        node.label(methodText);

                        // 非publicは色なし
                        if (angle.methodCharacteristics().isNotPublicMethod()) {
                            node.notPublicMethod();
                        }

                        // ハンドラを強調（赤色）
                        if (angle.usingFromController().isSatisfy()) {
                            node.handlerMethod();
                        }
                    }
                    return node.asText();
                }).collect(joining("\n"));

        // クラス名でグルーピングする
        String subgraphText = angles.stream()
                .collect(groupingBy(serviceAngle -> serviceAngle.method().declaringType()))
                .entrySet().stream()
                .map(entry ->
                        "subgraph \"cluster_" + entry.getKey().fullQualifiedName() + "\""
                                + "{"
                                + "label=\"" + japaneseNameLineOf(entry.getKey()) + entry.getKey().asSimpleText() + "\";"
                                + entry.getValue().stream()
                                .map(serviceAngle -> serviceAngle.method().asFullText())
                                .map(text -> "\"" + text + "\";")
                                .collect(joining("\n"))
                                + "}")
                .collect(joining("\n"));


        // 凡例
        String legendText = new StringJoiner("\n", "subgraph cluster_legend {", "}")
                .add("label=凡例;")
                .add(new Node("ハンドラメソッド").handlerMethod().asText())
                .add("通常のメソッド;")
                .add(new Node("非publicメソッド").notPublicMethod().asText())
                .add(new Node("lambda").lambda().asText())
                .toString();

        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,color=lightgoldenrod];")
                .add(relationText.asText())
                .add(labelText)
                .add(subgraphText)
                .add(legendText)
                .toString();

        return graphText;
    }

    private String japaneseNameLineOf(TypeIdentifier typeIdentifier) {
        String japaneseName = japaneseNameFinder.find(typeIdentifier).japaneseName().summarySentence();
        return japaneseName.isEmpty() ? "" : japaneseName + "\n";
    }

    private String japaneseNameLineOf(MethodDeclaration method) {
        String japaneseName = japaneseNameFinder.find(method).japaneseName().summarySentence();
        return japaneseName.isEmpty() ? "" : japaneseName + "\n";
    }
}
