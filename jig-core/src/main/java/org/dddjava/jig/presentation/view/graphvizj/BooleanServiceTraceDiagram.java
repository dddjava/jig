package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.threelayer.services.ServiceAngle;
import org.dddjava.jig.domain.model.threelayer.services.ServiceAngles;

import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

public class BooleanServiceTraceDiagram implements DotTextEditor<ServiceAngles> {

    private final JapaneseNameFinder japaneseNameFinder;
    private final MethodNodeLabelStyle methodNodeLabelStyle;

    public BooleanServiceTraceDiagram(JapaneseNameFinder japaneseNameFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.japaneseNameFinder = japaneseNameFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
    }

    @Override
    public DotTexts edit(ServiceAngles model) {
        ServiceAngles booleanServiceAngles = model.filterReturnsBoolean();

        if (booleanServiceAngles.isEmpty()) {
            String text = new StringJoiner("\n", "digraph JIG {", "}")
                    .add("\"出力するメソッドはありません\"[shape=note]")
                    .toString();
            return new DotTexts(text);
        }

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (ServiceAngle serviceAngle : booleanServiceAngles.list()) {
            for (MethodDeclaration methodDeclaration : serviceAngle.userServiceMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
            for (MethodDeclaration methodDeclaration : serviceAngle.userControllerMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
        }

        // booleanサービスメソッドの表示方法
        String booleanServiceMethodsText = booleanServiceAngles.list().stream()
                .map(angle -> {
                    MethodDeclaration method = angle.method();
                    Node node = Node.of(method);
                    if (method.isLambda()) {
                        node.label("(lambda)").lambda();
                    } else {
                        // ラベルに和名をつける
                        node.label(japaneseNameLineOf(method) + methodNodeLabelStyle.typeNameAndMethodName(method, japaneseNameFinder));
                    }
                    return node.asText();
                }).collect(joining("\n"));


        // 使用メソッドのラベル
        String userApplicationMethodsText = booleanServiceAngles.userServiceMethods().list().stream()
                // booleanメソッドを除く
                .filter(userMethod -> booleanServiceAngles.notContains(userMethod))
                .map(userMethod -> Node.of(userMethod).label(methodNodeLabelStyle.typeNameAndMethodName(userMethod, japaneseNameFinder)).asText())
                .collect(joining("\n"));
        String userControllerMethodsText = booleanServiceAngles.userControllerMethods().list().stream()
                .map(userMethod -> Node.of(userMethod).label(methodNodeLabelStyle.typeNameAndMethodName(userMethod, japaneseNameFinder)).asText())
                .collect(joining("\n"));


        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("rankdir=LR;")
                .add("node [shape=box,style=filled,color=lightgoldenrod];")
                .add(relationText.asText())
                .add("{")
                .add("node [shape=none,style=none,fontsize=30];")
                .add("edge [arrowhead=none];")
                .add("\"Controller Method\" -> \"Service Method\" -> \"boolean Service Method\";")
                .add("}")
                .add("{").add("rank=same;").add("\"boolean Service Method\"").add("/* labelText */").add(booleanServiceMethodsText).add("}")
                .add("{").add("rank=same;").add("\"Service Method\"").add("/* userApplicationMethodsText */").add(userApplicationMethodsText).add("}")
                .add("{").add("rank=same;").add("\"Controller Method\"").add("/* userControllerMethodsText */").add(userControllerMethodsText).add("}")
                .toString();

        return new DotTexts(graphText);
    }

    private String japaneseNameLineOf(MethodDeclaration method) {
        String japaneseName = japaneseNameFinder.find(method.identifier()).japaneseName().summarySentence();
        return japaneseName.isEmpty() ? "" : japaneseName + "\n";
    }
}
