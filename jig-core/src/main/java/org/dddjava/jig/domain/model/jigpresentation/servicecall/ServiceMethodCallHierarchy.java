package org.dddjava.jig.domain.model.jigpresentation.servicecall;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigdocument.*;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;
import org.dddjava.jig.domain.model.jigpresentation.documentation.RelationText;
import org.dddjava.jig.domain.model.jigpresentation.usecase.UseCase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * サービスメソッド呼び出し
 */
public class ServiceMethodCallHierarchy {

    List<ServiceAngle> list;

    public ServiceMethodCallHierarchy(List<ServiceAngle> list) {
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
                    UseCase useCase = new UseCase(serviceAngle);

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
                                + "label=\"" + aliasLineOf(entry.getKey(), aliasFinder) + entry.getKey().asSimpleText() + "\";"
                                + entry.getValue().stream()
                                .map(serviceAngle -> serviceAngle.method().asFullNameText())
                                .map(text -> "\"" + text + "\";")
                                .collect(joining("\n"))
                                + "}")
                .collect(joining("\n"));

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.ServiceMethodCallHierarchyDiagram);

        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(relationText.asText())
                .add(subgraphText)
                .add(serviceMethodText)
                .add(requestHandlerText(angles))
                .add(repositoryText(angles))
                .add(legendText(jigDocumentContext))
                .toString();
        return DiagramSource.createDiagramSource(documentName, graphText);
    }


    /**
     * 凡例
     */
    private String legendText(JigDocumentContext jigDocumentContext) {
        return new StringJoiner("\n", "subgraph cluster_legend {", "}")
                .add("label=" + jigDocumentContext.label("legend") + ";")
                .add(new Node(jigDocumentContext.label("handler_method")).handlerMethod().asText())
                .add(jigDocumentContext.label("other_method") + ";")
                .add(new Node(jigDocumentContext.label("not_public_method")).notPublicMethod().asText())
                .add(new Node("lambda").lambda().asText())
                .add(new Node(jigDocumentContext.label("controller_method")).other().asText())
                .add(new Node(jigDocumentContext.label("repository_type")).other().asText())
                .toString();
    }

    /**
     * リクエストハンドラ（Controllerのメソッド）の表示とServiceMethodへの関連。リクエストハンドラは同じRankにする。
     *
     * [RequestHandlerMethod] --> [ServiceMethod]
     */
    private String requestHandlerText(List<ServiceAngle> angles) {
        Set<MethodDeclaration> handlers = new HashSet<>();
        RelationText handlingRelation = new RelationText();
        for (ServiceAngle serviceAngle : angles) {
            for (MethodDeclaration handlerMethod : serviceAngle.userControllerMethods().list()) {
                handlingRelation.add(handlerMethod, serviceAngle.method());
                handlers.add(handlerMethod);
            }
        }
        String requestHandlerMethods = handlers.stream()
                .map(handler -> Node.of(handler).other().label(handler.asSimpleText()))
                .map(Node::asText)
                .collect(joining("\n"));
        return new StringJoiner("\n")
                .add("{rank=same;").add(requestHandlerMethods).add("}")
                .add("{edge [style=dashed];").add(handlingRelation.asText()).add("}")
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
