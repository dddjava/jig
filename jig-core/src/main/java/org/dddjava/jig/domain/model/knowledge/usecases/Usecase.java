package org.dddjava.jig.domain.model.knowledge.usecases;

import org.dddjava.jig.domain.model.data.members.instruction.IfInstruction;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.instruction.SimpleInstruction;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inbound.Entrypoint;
import org.dddjava.jig.domain.model.information.inbound.InboundAdapters;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingFields;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.OutboundPortOperation;

import java.util.Collection;
import java.util.List;

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
public record Usecase(ServiceMethod serviceMethod, List<JigMethod> usingRepositoryMethods,
                      Collection<MethodCall> usingServiceMethods, Collection<JigMethodId> userServiceMethods,
                      UsecaseCategory usecaseCategory) {

    public static Usecase from(ServiceMethod serviceMethod, ServiceMethods serviceMethods, InboundAdapters inboundAdapters, OutboundAdapters outboundAdapters) {
        UsingMethods usingMethods = serviceMethod.usingMethods();

        Collection<JigMethodId> userServiceMethods = serviceMethod.callerMethods().filter(jigMethodId -> serviceMethods.contains(jigMethodId));
        Collection<MethodCall> usingServiceMethods = serviceMethod.usingMethods().invokedMethodStream()
                .filter(invokedMethod -> serviceMethods.contains(invokedMethod.jigMethodId()))
                .toList();

        List<JigMethod> usingRepositoryMethods = usingMethods.invokedMethodStream()
                .map(methodCall -> methodCall.jigMethodId())
                .distinct() // 同じメソッドの複数回呼び出しによる重複を排除
                .flatMap(jigMethodId -> outboundAdapters.findPortOperation(jigMethodId).stream())
                .map(OutboundPortOperation::jigMethod)
                .toList();

        Collection<Entrypoint> entrypointMethods = inboundAdapters.collectEntrypointMethodOf(serviceMethod.callerMethods());
        UsecaseCategory usecaseCategory = entrypointMethods.isEmpty() ? UsecaseCategory.その他 : UsecaseCategory.ハンドラ;
        return new Usecase(serviceMethod, usingRepositoryMethods, usingServiceMethods, userServiceMethods, usecaseCategory);
    }

    public boolean usingFromController() {
        return usecaseCategory.handler();
    }

    public UsingFields usingFields() {
        return serviceMethod.methodUsingFields();
    }

    public List<JigMethod> usingRepositoryMethods() {
        return usingRepositoryMethods;
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

    public JigMethodId jigMethodId() {
        return serviceMethod.method().jigMethodId();
    }

}
