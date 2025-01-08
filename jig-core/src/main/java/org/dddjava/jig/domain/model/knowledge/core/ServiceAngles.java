package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.models.applications.inputs.Entrypoint;
import org.dddjava.jig.domain.model.models.applications.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.models.applications.outputs.RepositoryMethods;
import org.dddjava.jig.domain.model.models.applications.usecases.ServiceMethod;
import org.dddjava.jig.domain.model.models.applications.usecases.ServiceMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    public static ServiceAngles from(ServiceMethods serviceMethods, Entrypoint entrypoint, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            MethodDeclarations usingMethods = serviceMethod.usingMethods().methodDeclarations();

            MethodDeclarations userServiceMethods = serviceMethod.callerMethods().methodDeclarations().filter(methodDeclaration -> serviceMethods.contains(methodDeclaration));
            MethodDeclarations usingServiceMethods = usingMethods.filter(methodDeclaration -> serviceMethods.contains(methodDeclaration));
            RepositoryMethods usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods);
            ServiceAngle serviceAngle = new ServiceAngle(serviceMethod, usingRepositoryMethods, usingServiceMethods, entrypoint.collectEntrypointMethodOf(serviceMethod.callerMethods()), userServiceMethods);
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

    public static List<Map.Entry<String, Function<ServiceAngle, Object>>> reporter(JigDocumentContext jigDocumentContext) {
        // TODO jigDocumentContext を使わずに名前解決をできるようにしたい
        return List.of(
                Map.entry("パッケージ名", item -> item.serviceMethod().declaringType().packageIdentifier().asText()),
                Map.entry("クラス名", item -> item.serviceMethod().declaringType().asSimpleText()),
                Map.entry("メソッドシグネチャ", item -> item.method().asSignatureSimpleText()),
                Map.entry("メソッド戻り値の型", item -> item.method().methodReturn().asSimpleText()),
                Map.entry("イベントハンドラ", item -> item.usingFromController() ? "◯" : ""),
                Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.serviceMethod().declaringType()).asText()),
                Map.entry("メソッド別名", item -> item.serviceMethod().method().aliasTextOrBlank()),
                Map.entry("メソッド戻り値の型の別名", item ->
                        jigDocumentContext.classComment(item.serviceMethod().method().declaration().methodReturn().typeIdentifier()).asText()
                ),
                Map.entry("メソッド引数の型の別名", item ->
                        item.serviceMethod().method().declaration().methodSignature().listArgumentTypeIdentifiers().stream()
                                .map(jigDocumentContext::classComment)
                                .map(ClassComment::asText)
                                .collect(Collectors.joining(", ", "[", "]"))
                ),
                Map.entry("使用しているフィールドの型", item -> item.usingFields().typeIdentifiers().asSimpleText()),
                Map.entry("分岐数", item -> item.serviceMethod().method().decisionNumber().intValue()),
                Map.entry("使用しているサービスのメソッド", item -> item.usingServiceMethods().asSignatureAndReturnTypeSimpleText()),
                Map.entry("使用しているリポジトリのメソッド", item -> item.usingRepositoryMethods().asSimpleText()),
                Map.entry("null使用", item -> item.useNull() ? "◯" : ""),
                Map.entry("stream使用", item -> item.useStream() ? "◯" : "")
        );
    }
}
