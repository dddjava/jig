package org.dddjava.jig.domain.model.jigpresentation.smell;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigdocument.*;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.MethodNodeLabelStyle;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;
import org.dddjava.jig.domain.model.jigpresentation.documentation.RelationText;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class ReturnBooleanTrace {

    List<ServiceAngle> list;

    public ReturnBooleanTrace(List<ServiceAngle> list) {
        this.list = list;
    }

    public DiagramSources returnBooleanTraceDotText(JigDocumentContext jigDocumentContext, MethodNodeLabelStyle methodNodeLabelStyle, AliasFinder aliasFinder) {
        List<ServiceAngle> collect = list.stream()
                .filter(serviceAngle -> serviceAngle.method().methodReturn().isBoolean())
                .collect(Collectors.toList());

        return methodTraceDotText(jigDocumentContext, methodNodeLabelStyle, aliasFinder);
    }

    DiagramSources methodTraceDotText(JigDocumentContext jigDocumentContext, MethodNodeLabelStyle methodNodeLabelStyle, AliasFinder aliasFinder) {

        if (list.isEmpty()) {
            return DiagramSource.empty();
        }

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (ServiceAngle serviceAngle : list) {
            for (MethodDeclaration methodDeclaration : serviceAngle.userServiceMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
            for (MethodDeclaration methodDeclaration : serviceAngle.userControllerMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
        }

        // booleanサービスメソッドの表示方法
        String booleanServiceMethodsText = list.stream()
                .map(angle -> {
                    MethodDeclaration method = angle.method();
                    Node node = Node.of(method);
                    if (method.isLambda()) {
                        node.label("(lambda)").lambda();
                    } else {
                        // ラベルに別名をつける
                        String aliasLine = aliasFinder.find(method.identifier()).asText();
                        node.label((aliasLine.isEmpty() ? "" : aliasLine + "\n") + methodNodeLabelStyle.typeNameAndMethodName(method, aliasFinder));
                    }
                    return node.asText();
                }).collect(joining("\n"));


        // 使用メソッドのラベル
        MethodDeclarations userServiceMethods = list.stream()
                .flatMap(serviceAngle1 -> serviceAngle1.userServiceMethods().list().stream())
                .distinct()
                .collect(MethodDeclarations.collector());
        String userApplicationMethodsText = userServiceMethods.list().stream()
                // booleanメソッドを除く
                .filter(userMethod -> notContains(userMethod))
                .map(userMethod -> Node.of(userMethod).label(methodNodeLabelStyle.typeNameAndMethodName(userMethod, aliasFinder)).asText())
                .collect(joining("\n"));

        MethodDeclarations userControllerMethods = list.stream()
                .flatMap(serviceAngle -> serviceAngle.userControllerMethods().list().stream())
                .distinct()
                .collect(MethodDeclarations.collector());
        String userControllerMethodsText = userControllerMethods.list().stream()
                .map(userMethod -> Node.of(userMethod).label(methodNodeLabelStyle.typeNameAndMethodName(userMethod, aliasFinder)).asText())
                .collect(joining("\n"));

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.BooleanServiceDiagram);

        String graphText = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
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

        return DiagramSource.createDiagramSource(documentName, graphText);
    }

    boolean notContains(MethodDeclaration methodDeclaration) {
        return list.stream()
                .noneMatch(serviceAngle -> serviceAngle.method().sameIdentifier(methodDeclaration));
    }
}
