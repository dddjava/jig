package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.members.JigMethod;
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

    private final ServiceAngles serviceAngles;

    public ServiceMethodCallHierarchyDiagram(ServiceAngles serviceAngles) {
        this.serviceAngles = serviceAngles;
    }

    private static Node usecaseNode(Usecase usecase) {
        return new Node(usecase.usecaseIdentifier())
                .shape("ellipse")
                .label(usecase.usecaseLabel())
                .tooltip(usecase.simpleTextWithDeclaringType())
                .as(usecase.isHandler() ? NodeRole.主役 : NodeRole.準主役)
                .url(usecase.declaringType(), JigDocument.ApplicationSummary);
    }

    // TODO ダイアグラムにlambdaが表示されてもいいことないので、インライン化して廃止する
    private static Node lambdaNode(JigMethod method) {
        return new Node(method.jigMethodId().value())
                .label("(lambda)").as(NodeRole.モブ).shape("ellipse");
    }

    @Override
    public int write(Consumer<DiagramSource> diagramSourceWriteProcess) {
        List<Usecase> angles = serviceAngles.list();
        if (angles.isEmpty()) {
            return 0;
        }

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (Usecase usecase : angles) {
            for (JigMethodId jigMethodId : usecase.userServiceMethods()) {
                relationText.add(jigMethodId, usecase.jigMethodId());
            }
        }

        // メソッドの表示方法
        String serviceMethodText = angles.stream()
                .map(usecase -> {
                    JigMethod method = usecase.serviceMethod().method();
                    if (method.jigMethodId().isLambda()) {
                        return lambdaNode(method).dotText();
                    }
                    Node useCaseNode = usecaseNode(usecase);

                    // 非publicは色なし
                    if (usecase.isNotPublicMethod()) {
                        useCaseNode.as(NodeRole.脇役);
                    }

                    return useCaseNode.dotText();
                }).collect(joining("\n"));

        String subgraphText = serviceAngles.streamAndMap((jigType, serviceAngleList) ->
                        "subgraph \"cluster_" + jigType.fqn() + "\""
                                + "{"
                                + "style=solid;"
                                + "label=\"" + jigType.label() + "\";"
                                + serviceAngleList.stream()
                                .map(usecase -> usecase.jigMethodId().value())
                                .map(text -> "\"" + text + "\";")
                                .collect(joining("\n"))
                                + "}")
                .collect(joining("\n"));

        DocumentName documentName = DocumentName.of(JigDocument.ServiceMethodCallHierarchyDiagram);

        String dotText = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("newrank=true;")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(relationText.dotText())
                .add(subgraphText)
                .add(serviceMethodText)
                .add(repositoryText(angles))
                .toString();
        diagramSourceWriteProcess.accept(DiagramSource.createDiagramSourceUnit(documentName, dotText));
        return 1;
    }

    /**
     * リポジトリの表示とServiceMethodからの関連。リポジトリは同じRankにする。
     *
     * [ServiceMethod] --> [Repository]
     */
    private String repositoryText(List<Usecase> angles) {
        Set<TypeId> repositories = new HashSet<>();
        RelationText repositoryRelation = new RelationText();
        for (Usecase usecase : angles) {
            for (JigMethod repositoryMethod : usecase.usingRepositoryMethods().list()) {
                repositoryRelation.add(usecase.serviceMethod().method().jigMethodId(), repositoryMethod.declaringType());
                repositories.add(repositoryMethod.declaringType());
            }
        }
        String repositoryTypes = repositories.stream()
                .map(repository -> Node.typeOf(repository).as(NodeRole.モブ).label(repository.asSimpleText()))
                .map(Node::dotText)
                .collect(joining("\n"));

        return new StringJoiner("\n")
                .add("{rank=same;").add(repositoryTypes).add("}")
                .add("{edge [style=dashed];").add(repositoryRelation.uniqueDotText()).add("}")
                .toString();
    }
}
