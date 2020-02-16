package org.dddjava.jig.domain.model.jigmodel.applications.services;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigdocument.*;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;
import org.dddjava.jig.domain.model.jigmodel.applications.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.repositories.DatasourceMethods;
import org.dddjava.jig.presentation.view.JigDocumentContext;
import org.dddjava.jig.presentation.view.graphvizj.MethodNodeLabelStyle;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    private ServiceAngles(List<ServiceAngle> list) {
        this.list = list;
    }

    public List<ServiceAngle> list() {
        return list;
    }

    public ServiceAngles(ServiceMethods serviceMethods, MethodRelations methodRelations, ControllerMethods controllerMethods, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            list.add(new ServiceAngle(serviceMethod, methodRelations, controllerMethods, serviceMethods, datasourceMethods));
        }
        this.list = list;
    }

    boolean notContains(MethodDeclaration methodDeclaration) {
        return list.stream()
                .noneMatch(serviceAngle -> serviceAngle.method().sameIdentifier(methodDeclaration));
    }

    public DiagramSource returnBooleanTraceDotText(JigDocumentContext jigDocumentContext, MethodNodeLabelStyle methodNodeLabelStyle, AliasFinder aliasFinder) {
        List<ServiceAngle> collect = list.stream()
                .filter(serviceAngle -> serviceAngle.method().methodReturn().isBoolean())
                .collect(Collectors.toList());

        ServiceAngles booleanServiceAngles = new ServiceAngles(collect);
        return booleanServiceAngles.methodTraceDotText(jigDocumentContext, methodNodeLabelStyle, aliasFinder);
    }

    DiagramSource methodTraceDotText(JigDocumentContext jigDocumentContext, MethodNodeLabelStyle methodNodeLabelStyle, AliasFinder aliasFinder) {

        if (list.isEmpty()) {
            return DiagramSource.empty();
        }

        // メソッド間の関連
        RelationText relationText = new RelationText();
        for (ServiceAngle serviceAngle : list()) {
            for (MethodDeclaration methodDeclaration : serviceAngle.userServiceMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
            for (MethodDeclaration methodDeclaration : serviceAngle.userControllerMethods().list()) {
                relationText.add(methodDeclaration, serviceAngle.method());
            }
        }

        // booleanサービスメソッドの表示方法
        String booleanServiceMethodsText = list().stream()
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


        String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.BooleanServiceDiagram) + "\";")
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

        return new DiagramSource(DocumentName.of(JigDocument.BooleanServiceDiagram), graphText);
    }

    public DiagramSource methodCallDotText(JigDocumentContext jigDocumentContext, AliasFinder aliasFinder, MethodNodeLabelStyle methodNodeLabelStyle) {
        if (list.isEmpty()) {
            return DiagramSource.empty();
        }

        List<ServiceAngle> angles = list();

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
                        node.label(aliasLineOf(method, aliasFinder) + methodNodeLabelStyle.apply(method, aliasFinder));

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
                                + "label=\"" + aliasLineOf(entry.getKey(), aliasFinder) + entry.getKey().asSimpleText() + "\";"
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
                .add(legendText(jigDocumentContext))
                .toString();
        return new DiagramSource(DocumentName.of(JigDocument.ServiceMethodCallHierarchyDiagram), graphText);
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

    private String aliasLineOf(MethodDeclaration method, AliasFinder aliasFinder) {
        String aliasText = aliasFinder.find(method.identifier()).asText();
        return aliasText.isEmpty() ? "" : aliasText + "\n";
    }
}
