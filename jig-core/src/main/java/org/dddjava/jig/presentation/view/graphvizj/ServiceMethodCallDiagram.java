package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.fact.alias.AliasFinder;
import org.dddjava.jig.domain.model.richmethod.Method;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

public class ServiceMethodCallDiagram implements DotTextEditor<ServiceAngles> {

    final AliasFinder aliasFinder;
    final MethodNodeLabelStyle methodNodeLabelStyle;
    JigDocumentContext jigDocumentContext;

    public ServiceMethodCallDiagram(AliasFinder aliasFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        this.aliasFinder = aliasFinder;
        this.methodNodeLabelStyle = methodNodeLabelStyle;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(ServiceAngles serviceAngles) {
        if (serviceAngles.isEmpty()) {
            return new DotTexts(Collections.singletonList(DotText.empty()));
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
                .map(angle -> {
                    MethodDeclaration method = angle.method();
                    Node node = Node.of(method);
                    if (method.isLambda()) {
                        node.label("(lambda)").lambda();
                    } else {
                        // ラベルに別名をつける
                        node.label(aliasLineOf(method) + methodNodeLabelStyle.apply(method, aliasFinder));

                        // 非publicは色なし
                        if (angle.isNotPublicMethod()) {
                            node.notPublicMethod();
                        }

                        // ハンドラを強調
                        if (angle.usingFromController()) {
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
                                + "label=\"" + aliasLineOf(entry.getKey()) + entry.getKey().asSimpleText() + "\";"
                                + entry.getValue().stream()
                                .map(serviceAngle -> serviceAngle.method().asFullNameText())
                                .map(text -> "\"" + text + "\";")
                                .collect(joining("\n"))
                                + "}")
                .collect(joining("\n"));


        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.ServiceMethodCallHierarchyDiagram) + "\";")
                .add("rankdir=LR;")
                .add(Node.DEFAULT)
                .add(relationText.asText())
                .add(subgraphText)
                .add(serviceMethodText)
                .add(requestHandlerText(angles))
                .add(repositoryText(angles))
                .add(legendText())
                .toString();
        return new DotTexts(graphText);
    }

    /**
     * 凡例
     */
    public String legendText() {
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
    public String requestHandlerText(List<ServiceAngle> angles) {
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

    private String aliasLineOf(TypeIdentifier typeIdentifier) {
        String aliasText = aliasFinder.find(typeIdentifier).asText();
        return aliasText.isEmpty() ? "" : aliasText + "\n";
    }

    private String aliasLineOf(MethodDeclaration method) {
        String aliasText = aliasFinder.find(method.identifier()).asText();
        return aliasText.isEmpty() ? "" : aliasText + "\n";
    }
}
