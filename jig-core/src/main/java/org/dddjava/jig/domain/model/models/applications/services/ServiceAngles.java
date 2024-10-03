package org.dddjava.jig.domain.model.models.applications.services;

import org.dddjava.jig.domain.model.models.applications.backends.DatasourceMethods;
import org.dddjava.jig.domain.model.models.applications.backends.RepositoryMethods;
import org.dddjava.jig.domain.model.models.applications.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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


    public Map<String, List<String>> collectRelation() {
        var map = new HashMap<String, List<String>>();
        for (ServiceAngle serviceAngle : list) {
            map.put(serviceAngle.method().asSimpleText(),
                    serviceAngle.usingServiceMethods().list().stream().map(MethodDeclaration::asSimpleText).toList());
        }
        return map;
    }

    public List<String> usings(String key) {
        return list.stream().filter(serviceAngle -> serviceAngle.method().asSimpleText().equals(key))
                .findAny()
                .map(serviceAngle -> serviceAngle.usingServiceMethods().list().stream().map(MethodDeclaration::asSimpleText).toList())
                .orElseGet(List::of);
    }

    private void extracted(String key, StringJoiner relations, Set<String> stopper) {
        if (stopper.contains(key)) {
            return;
        }
        stopper.add(key);
        var usings = usings(key);
        relations.add(usings.stream()
                .map(using -> "%s --> %s".formatted(key, using))
                .collect(Collectors.joining("\n")));
        for (var using : usings) {
            extracted(using, relations, stopper);
        }
    }

    public String mermaidText(String key) {
        Set<String> targets = new HashSet<>();

        var relations = new StringJoiner("\n");
        extracted(key, relations, targets);

        Function<String, String> escape = string -> string
//                .replace('(', '（')
//                .replace(')', '）')
                ;

        var labels = list().stream()
                // 処理したものだけラベル出力
                .filter(serviceAngle -> targets.contains(serviceAngle.method().asSimpleText()))
                .map(serviceAngle -> "%s[\"%s\"]".formatted(
                        serviceAngle.method().asSimpleText(),
                        escape.apply(serviceAngle.serviceMethod().method().labelTextOrLambda())
                ))
                .collect(Collectors.joining("\n"));

        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");
        mermaidText.add(relations.toString());
        mermaidText.add(labels);

        return mermaidText.toString();
    }
}
