package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.models.applications.usecases.ServiceAngle;
import org.dddjava.jig.domain.model.models.applications.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.usecases.Usecase;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * サービスメソッド呼び出し図
 */
public class ServiceMethodCallHierarchyDiagram implements DiagramSourceWriter {

    ServiceAngles serviceAngles;

    public ServiceMethodCallHierarchyDiagram(ServiceAngles serviceAngles) {
        this.serviceAngles = serviceAngles;
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
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
                        useCaseNode.as(NodeRole.脇役);
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
                .collect(joining("\n"
                        // TODO #852 暫定対処
                        // , "subgraph cluster_usecases {style=invis;", "}"
                ));

        DocumentName documentName = DocumentName.of(JigDocument.ServiceMethodCallHierarchyDiagram);

        String graphText = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("newrank=true;")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(relationText.asText())
                .add(subgraphText)
                .add(serviceMethodText)
                .add(repositoryText(angles))
                .toString();
        return DiagramSource.createDiagramSource(documentName, graphText);
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
                .map(repository -> Node.typeOf(repository).as(NodeRole.モブ).label(repository.asSimpleText()))
                .map(Node::asText)
                .collect(joining("\n"));

        return new StringJoiner("\n")
                .add("{rank=same;").add(repositoryTypes).add("}")
                .add("{edge [style=dashed];").add(repositoryRelation.asUniqueText()).add("}")
                .toString();
    }
}
