package org.dddjava.jig.domain.model.jigdocumenter.diagram;

import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigdocumenter.DiagramSource;
import org.dddjava.jig.domain.model.jigdocumenter.DiagramSources;
import org.dddjava.jig.domain.model.jigdocumenter.JigDocumentContext;
import org.dddjava.jig.domain.model.jigmodel.Node;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Method;
import org.dddjava.jig.domain.model.jigmodel.usecase.Usecase;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * サービスメソッド呼び出し
 */
public class ServiceMethodCallHierarchyDiagram {

    List<ServiceAngle> list;

    public ServiceMethodCallHierarchyDiagram(List<ServiceAngle> list) {
        this.list = list;
    }


    public DiagramSources methodCallDotText(JigDocumentContext jigDocumentContext, AliasFinder aliasFinder) {
        if (list.isEmpty()) {
            return DiagramSource.empty();
        }

        List<ServiceAngle> angles = list;

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
                    Node node = Node.of(method);
                    if (method.isLambda()) {
                        return node.label("(lambda)").lambda().asText();
                    }
                    Usecase useCase = new Usecase(serviceAngle);

                    Node useCaseNode = useCase.node(aliasFinder);

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
                                + "label=\"" + aliasLineOf(entry.getKey(), aliasFinder) + entry.getKey().asSimpleText() + "\";"
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
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(relationText.asText())
                .add(subgraphText)
                .add(serviceMethodText)
                .add(requestHandlerText(angles, aliasFinder))
                .add(repositoryText(angles))
                .toString();
        return DiagramSource.createDiagramSource(documentName, graphText);
    }

    /**
     * リクエストハンドラ（Controllerのメソッド）の表示とServiceMethodへの関連。リクエストハンドラは同じRankにする。
     *
     * [RequestHandlerMethod] --> [ServiceMethod]
     */
    private String requestHandlerText(List<ServiceAngle> angles, AliasFinder aliasFinder) {

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
                    .map(method -> Node.of(method)
                            .screenNode()
                            .label(method.methodSignature().methodName()))
                    .map(Node::asText)
                    .collect(joining("\n"));

            dotTextBuilder
                    .add("subgraph \"cluster_" + handlerType.fullQualifiedName() + "\" {")
                    .add("label=\"" + aliasFinder.find(handlerType).asTextOrDefault(handlerType.asSimpleText()) + "\";")
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
            for (Method repositoryMethod : serviceAngle.usingRepositoryMethods().list()) {
                repositoryRelation.add(serviceAngle.method(), repositoryMethod.declaration().declaringType());
                repositories.add(repositoryMethod.declaration().declaringType());
            }
        }
        String repositoryTypes = repositories.stream()
                .map(repository -> Node.of(repository).other().label(repository.asSimpleText()))
                .map(Node::asText)
                .collect(joining("\n"));

        return new StringJoiner("\n")
                .add("{rank=same;").add(repositoryTypes).add("}")
                .add("{edge [style=dashed];").add(repositoryRelation.asText()).add("}")
                .toString();
    }

    private String aliasLineOf(TypeIdentifier typeIdentifier, AliasFinder aliasFinder) {
        String aliasText = aliasFinder.find(typeIdentifier).asText();
        return aliasText.isEmpty() ? "" : aliasText + "\n";
    }
}
