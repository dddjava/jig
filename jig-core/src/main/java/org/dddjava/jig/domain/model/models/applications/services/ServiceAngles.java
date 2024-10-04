package org.dddjava.jig.domain.model.models.applications.services;

import org.dddjava.jig.domain.model.models.applications.backends.DatasourceMethods;
import org.dddjava.jig.domain.model.models.applications.backends.RepositoryMethods;
import org.dddjava.jig.domain.model.models.applications.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    public static ServiceAngles from(ServiceMethods serviceMethods, HandlerMethods handlerMethods, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            MethodDeclarations usingMethods = serviceMethod.usingMethods().methodDeclarations();

            HandlerMethods userHandlerMethods = handlerMethods.filter(serviceMethod.callerMethods());
            MethodDeclarations userServiceMethods = serviceMethod.callerMethods().methodDeclarations().filter(methodDeclaration -> serviceMethods.contains(methodDeclaration));
            MethodDeclarations usingServiceMethods = usingMethods.filter(methodDeclaration -> serviceMethods.contains(methodDeclaration));
            RepositoryMethods usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods);
            ServiceAngle serviceAngle = new ServiceAngle(serviceMethod, usingRepositoryMethods, usingServiceMethods, userHandlerMethods, userServiceMethods);
            list.add(serviceAngle);
        }
        return new ServiceAngles(list);
    }

    public List<ServiceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(serviceAngle -> serviceAngle.method().asFullNameText()))
                .collect(Collectors.toList());
    }

    private ServiceAngles(List<ServiceAngle> list) {
        this.list = list;
    }

    public boolean none() {
        return list.isEmpty();
    }

    private Optional<String> relationsText(MethodIdentifier methodIdentifier, Set<MethodIdentifier> stopper) {
        if (stopper.contains(methodIdentifier)) {
            // 処理済みなので抜ける。抜けないと無限ループになる。
            return Optional.empty();
        }
        stopper.add(methodIdentifier);

        var usings = list.stream()
                .filter(serviceAngle -> serviceAngle.method().identifier().equals(methodIdentifier))
                .flatMap(serviceAngle -> serviceAngle.usingServiceMethods().list().stream()
                        .map(methodDeclaration -> methodDeclaration.identifier()))
                .toList();

        var relations = new StringJoiner("\n");
        usings.stream()
                .map(using -> "%s --> %s".formatted(methodIdentifier.htmlIdText(), using.htmlIdText()))
                .forEach(relations::add);
        for (var using : usings) {
            relationsText(using, stopper).ifPresent(relations::add);
        }
        return Optional.of(relations.toString());
    }

    public String mermaidText(MethodIdentifier methodIdentifier) {
        Set<MethodIdentifier> targets = new HashSet<>();

        return relationsText(methodIdentifier, targets)
                .map(relations -> {
                    var labels = list().stream()
                            // 処理したものだけラベル出力
                            .filter(serviceAngle -> targets.contains(serviceAngle.method().identifier()))
                            .flatMap(serviceAngle -> {
                                var jigMethod = serviceAngle.serviceMethod().method();

                                var htmlIdText = jigMethod.htmlIdText();
                                String methodNode = "%s([\"%s\"])".formatted(
                                        htmlIdText,
                                        jigMethod.labelTextOrLambda()
                                );
                                return Stream.of(
                                        methodNode,
                                        "click %s \"#%s\"".formatted(htmlIdText, htmlIdText)
                                );
                            })
                            .collect(Collectors.joining("\n"));

                    var mermaidText = new StringJoiner("\n");
                    mermaidText.add("graph LR");
                    mermaidText.add(relations);
                    mermaidText.add(labels);

                    return mermaidText.toString();
                })
                .orElse("");
    }
}
