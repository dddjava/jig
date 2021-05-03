package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.models.applications.ServiceAngle;
import org.dddjava.jig.domain.model.models.applications.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.Usecase;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * サービスメソッド呼び出し図
 */
public class ServiceMethodCallHierarchyDiagram {

    ServiceAngles serviceAngles;

    public ServiceMethodCallHierarchyDiagram(ServiceAngles serviceAngles) {
        this.serviceAngles = serviceAngles;
    }

    public DiagramSources methodCallDotText(JigDocumentContext jigDocumentContext) {
        if (serviceAngles.none()) {
            return DiagramSource.empty();
        }

        List<ServiceAngle> angles = serviceAngles.list();

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (MethodDeclaration methodDeclaration : serviceAngle.userServiceMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
        }

        // メソッドの表示方法
        String serviceMethodText = angles.stream()
                .map(serviceAngle -> {
                    MethodDeclaration method = serviceAngle.method();
                    if (method.isLambda()) {
                        return Nodes.lambda(method).asText();
                    }
                    Usecase usecase = new Usecase(serviceAngle);

                    Node useCaseNode = Nodes.usecase(jigDocumentContext, usecase);

                    // 非publicは色なし
                    if (serviceAngle.isNotPublicMethod()) {
                        useCaseNode.notPublicMethod();
                    }

                    return useCaseNode.asText();
                }).collect(joining("\n"));

        // クラス名でグルーピングする
        String subgraphText = angles.stream()
                .collect(groupingBy(serviceAngle -> serviceAngle.method().declaringType()))
                .entrySet().stream()
                .map(entry ->
                        "subgraph \"cluster_" + entry.getKey().fullQualifiedName() + "\""
                                + "{"
                                + "style=solid;"
                                + "label=\"" + jigDocumentContext.classComment(entry.getKey()).nodeLabel() + "\";"
                                + entry.getValue().stream()
                                .map(serviceAngle -> serviceAngle.method().asFullNameText())
                                .map(text -> "\"" + text + "\";")
                                .collect(joining("\n"))
                                + "}")
                .collect(joining("\n",
                        "subgraph cluster_usecases {style=invis;",
                        "}"));

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.ServiceMethodCallHierarchyDiagram);

        String graphText = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("newrank=true;")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(relationText.asText())
                .add(subgraphText)
                .add(serviceMethodText)
                .add(requestHandlerText(angles, jigDocumentContext))
                .add(repositoryText(angles))
                .toString();
        return DiagramSource.createDiagramSource(documentName, graphText);
    }

    /**
     * リクエストハンドラ（Controllerのメソッド）の表示とServiceMethodへの関連。リクエストハンドラは同じRankにする。
     *
     * [RequestHandlerMethod] --> [ServiceMethod]
     */
    private String requestHandlerText(List<ServiceAngle> angles, JigDocumentContext jigDocumentContext) {

        Set<MethodDeclaration> handlers = new HashSet<>();
        RelationText handlingRelation = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (MethodDeclaration handlerMethod : serviceAngle.userControllerMethods().list()) {
                handlingRelation.add(handlerMethod, serviceAngle.method());
                handlers.add(handlerMethod);
            }
        }

        StringJoiner dotTextBuilder = new StringJoiner("\n");
        // Controllerのクラスでグルーピング
        dotTextBuilder.add("subgraph cluster_controllers{")
                .add("rank=same;")
                .add("style=invis;");
        Map<TypeIdentifier, List<MethodDeclaration>> handlerMap = handlers.stream()
                .collect(groupingBy(MethodDeclaration::declaringType));
        handlerMap.forEach((handlerType, v) -> {
            String requestHandlerMethods = v.stream()
                    .map(method -> controllerNodeOf(method))
                    .map(Node::asText)
                    .collect(joining("\n"));

            dotTextBuilder
                    .add("subgraph \"cluster_" + handlerType.fullQualifiedName() + "\" {")
                    .add("label=\"" + jigDocumentContext.classComment(handlerType).asTextOrDefault(handlerType.asSimpleText()) + "\";")
                    // 画面の色と合わせる
                    .add("style=solid;")
                    .add("bgcolor=lightgrey;")
                    .add(requestHandlerMethods)
                    .add("}");
        });
        dotTextBuilder.add("}");

        return dotTextBuilder
                .add("{")
                .add("edge [style=dashed];")
                .add(handlingRelation.asText())
                .add("}")
                .toString();
    }

    /**
     * リポジトリの表示とServiceMethodからの関連。リポジトリは同じRankにする。
     *
     * [ServiceMethod] --> [Repository]
     */
    private String repositoryText(List<ServiceAngle> angles) {
        Set<TypeIdentifier> repositories = new HashSet<>();
        RelationText repositoryRelation = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (JigMethod repositoryMethod : serviceAngle.usingRepositoryMethods().list()) {
                repositoryRelation.add(serviceAngle.method(), repositoryMethod.declaration().declaringType());
                repositories.add(repositoryMethod.declaration().declaringType());
            }
        }
        String repositoryTypes = repositories.stream()
                .map(repository -> Node.typeOf(repository).other().label(repository.asSimpleText()))
                .map(Node::asText)
                .collect(joining("\n"));

        return new StringJoiner("\n")
                .add("{rank=same;").add(repositoryTypes).add("}")
                .add("{edge [style=dashed];").add(repositoryRelation.asUniqueText()).add("}")
                .toString();
    }

    private static Node controllerNodeOf(MethodDeclaration methodDeclaration) {
        return new Node(methodDeclaration.asFullNameText())
                .screenNode()
                .label(methodDeclaration.methodSignature().methodName());
    }
}
