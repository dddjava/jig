package org.dddjava.jig.domain.model.knowledge.usecases;

import org.dddjava.jig.domain.model.data.members.instruction.IfInstruction;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.instruction.SimpleInstruction;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.members.UsingFields;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.outputs.Gateways;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;

import java.util.Collection;

/**
 * ユースケース
 *
 * サービスクラスのメソッドとして実装される。
 * すべてのサービスクラスのメソッドがユースケースではないが、今のところ区別はできていない。
 *
 * すくなくとも、アダプタの接続されるポート（Controllerなどのエントリーポイントから直接呼び出されるメソッド）はユースケースであり、
 * このメソッドはUsecaseCategoryでは「ハンドラ」と識別する。
 * ハンドラはユースケースだが、ハンドラでないものもユースケースの可能性がある。実装上の区別はつけづらいので、
 * Javadocコメントの記述有無などで判断する？
 */
// TODO Usecaseにrenameする
public record Usecase(ServiceMethod serviceMethod, Gateways usingGateways,
                      Collection<MethodCall> usingServiceMethods, Collection<JigMethodId> userServiceMethods,
                      UsecaseCategory usecaseCategory) {

    public static Usecase from(ServiceMethod serviceMethod, ServiceMethods serviceMethods, InputAdapters inputAdapters, OutputImplementations outputImplementations) {
        UsingMethods usingMethods = serviceMethod.usingMethods();

        Collection<JigMethodId> userServiceMethods = serviceMethod.callerMethods().filter(jigMethodId -> serviceMethods.contains(jigMethodId));
        Collection<MethodCall> usingServiceMethods = serviceMethod.usingMethods().invokedMethodStream()
                .filter(invokedMethod -> serviceMethods.contains(invokedMethod.jigMethodId()))
                .toList();
        Gateways usingGateways = outputImplementations.repositoryMethods().filter(usingMethods);
        Collection<Entrypoint> entrypointMethods = inputAdapters.collectEntrypointMethodOf(serviceMethod.callerMethods());
        UsecaseCategory usecaseCategory = entrypointMethods.isEmpty() ? UsecaseCategory.その他 : UsecaseCategory.ハンドラ;
        return new Usecase(serviceMethod, usingGateways, usingServiceMethods, userServiceMethods, usecaseCategory);
    }

    public boolean usingFromController() {
        return usecaseCategory.handler();
    }

    public UsingFields usingFields() {
        return serviceMethod.methodUsingFields();
    }

    public Gateways usingRepositoryMethods() {
        return usingGateways;
    }

    public boolean useStream() {
        return serviceMethod.method().usingMethods().containsStreamAPI();
    }

    // TODO ミューテーションしても落ちなかったのでテストが必要
    public boolean useNull() {
        return serviceMethod.method().instructions().containsAny(instruction -> {
            if (instruction instanceof IfInstruction ifInstruction) {
                return ifInstruction.kind() == IfInstruction.Kind.NULL判定;
            }
            return instruction == SimpleInstruction.NULL参照;
        });
    }

    public boolean isNotPublicMethod() {
        return !serviceMethod.isPublic();
    }

    public JigMethodId jigMethodId() {
        return serviceMethod().method().jigMethodId();
    }

    public String usecaseIdentifier() {
        return serviceMethod.method().fqn();
    }

    public String usecaseLabel() {
        return serviceMethod.method().aliasText();
    }

    public String simpleTextWithDeclaringType() {
        return serviceMethod.method().simpleText();
    }

    public boolean isHandler() {
        return usecaseCategory.handler();
    }

    public TypeId declaringType() {
        return serviceMethod.declaringType();
    }
}
