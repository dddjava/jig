package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngle;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.Usecase;

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
            for (JigMethodIdentifier jigMethodIdentifier : serviceAngle.userServiceMethods()) {
                relationText.add(jigMethodIdentifier, serviceAngle.jigMethodIdentifier());
            }
        }

        // メソッドの表示方法
        String serviceMethodText = angles.stream()
                .map(serviceAngle -> {
                    JigMethod method = serviceAngle.serviceMethod().method();
                    if (method.jigMethodIdentifier().isLambda()) {
                        return Nodes.lambda(method).asText();
                    }
                    Usecase usecase = new Usecase(serviceAngle);

                    Node useCaseNode = Nodes.usecase(usecase);

                    // 非publicは色なし
                    if (serviceAngle.isNotPublicMethod()) {
                        useCaseNode.as(NodeRole.脇役);
                    }

                    return useCaseNode.asText();
                }).collect(joining("\n"));

        // クラス名でグルーピングする
        String subgraphText = angles.stream()
                .collect(groupingBy(serviceAngle -> serviceAngle.declaringType()))
                .entrySet().stream()
                .map(entry ->
                        "subgraph \"cluster_" + entry.getKey().fullQualifiedName() + "\""
                                + "{"
                                + "style=solid;"
                                + "label=\"" + jigDocumentContext.typeTerm(entry.getKey()).title() + "\";"
                                + entry.getValue().stream()
                                .map(serviceAngle -> serviceAngle.jigMethodIdentifier().value())
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
                repositoryRelation.add(serviceAngle.serviceMethod().method().jigMethodIdentifier(), repositoryMethod.declaringType());
                repositories.add(repositoryMethod.declaringType());
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
