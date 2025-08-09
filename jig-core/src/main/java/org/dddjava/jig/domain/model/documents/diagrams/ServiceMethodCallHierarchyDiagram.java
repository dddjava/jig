package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngle;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.usecases.Usecase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;

/**
 * サービスメソッド呼び出し図
 */
public class ServiceMethodCallHierarchyDiagram implements DiagramSourceWriter {

    ServiceAngles serviceAngles;

    public ServiceMethodCallHierarchyDiagram(ServiceAngles serviceAngles) {
        this.serviceAngles = serviceAngles;
    }

    @Override
    public int write(Consumer<DiagramSource> diagramSourceWriteProcess) {
        List<ServiceAngle> angles = serviceAngles.list();
        if (angles.isEmpty()) {
            return 0;
        }

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (JigMethodId jigMethodId : serviceAngle.userServiceMethods()) {
                relationText.add(jigMethodId, serviceAngle.jigMethodId());
            }
        }

        // メソッドの表示方法
        String serviceMethodText = angles.stream()
                .map(serviceAngle -> {
                    JigMethod method = serviceAngle.serviceMethod().method();
                    if (method.jigMethodId().isLambda()) {
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

        String subgraphText = serviceAngles.streamAndMap((jigType, serviceAngleList) ->
                        "subgraph \"cluster_" + jigType.fqn() + "\""
                                + "{"
                                + "style=solid;"
                                + "label=\"" + jigType.label() + "\";"
                                + serviceAngleList.stream()
                                .map(serviceAngle -> serviceAngle.jigMethodId().value())
                                .map(text -> "\"" + text + "\";")
                                .collect(joining("\n"))
                                + "}")
                .collect(joining("\n"));

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
        diagramSourceWriteProcess.accept(DiagramSource.createDiagramSourceUnit(documentName, graphText));
        return 1;
    }

    /**
     * リポジトリの表示とServiceMethodからの関連。リポジトリは同じRankにする。
     *
     * [ServiceMethod] --> [Repository]
     */
    private String repositoryText(List<ServiceAngle> angles) {
        Set<TypeId> repositories = new HashSet<>();
        RelationText repositoryRelation = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (JigMethod repositoryMethod : serviceAngle.usingRepositoryMethods().list()) {
                repositoryRelation.add(serviceAngle.serviceMethod().method().jigMethodId(), repositoryMethod.declaringType());
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
