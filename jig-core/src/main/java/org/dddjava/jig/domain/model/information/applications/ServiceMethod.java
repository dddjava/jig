package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.data.types.TypeIds;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingFields;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.List;
import java.util.Optional;

/**
 * サービスメソッド
 */
public record ServiceMethod(JigMethod method, CallerMethods callerMethods) {

    public static ServiceMethod from(JigMethod jigMethod, CallerMethodsFactory callerMethodsFactory) {
        return new ServiceMethod(
                jigMethod,
                callerMethodsFactory.callerMethodsOf(jigMethod.jigMethodId())
        );
    }

    public boolean isPublic() {
        return method.isPublic();
    }

    public UsingFields methodUsingFields() {
        return method.usingFields();
    }

    public UsingMethods usingMethods() {
        return method.usingMethods();
    }

    public JigMethod method() {
        return method;
    }

    public TypeId declaringType() {
        return method().declaringType();
    }

    public List<TypeId> internalUsingTypes() {
        return usingMethods().invokedMethodStream()
                .flatMap(invokedMethod -> invokedMethod.streamAssociatedTypes())
                .filter(typeId -> !typeId.isJavaLanguageType())
                .filter(typeId -> primaryType().filter(primaryType -> primaryType.equals(typeId)).isEmpty())
                .filter(typeId -> !requireTypes().contains(typeId))
                .distinct()
                .toList();
    }

    public Optional<TypeId> primaryType() {
        // 戻り値型が主要な関心
        TypeId typeId = method().methodReturnTypeReference().id();
        if (typeId.isVoid()) return Optional.empty();
        return Optional.of(typeId);
    }

    public List<TypeId> requireTypes() {
        return method.jigMethodDeclaration().argumentStream()
                .map(JigTypeReference::id)
                // primaryTypeは除く
                .filter(argumentType -> primaryType().filter(primaryType -> primaryType.equals(argumentType)).isEmpty())
                .toList();
    }

    public TypeIds usingTypes() {
        return method().usingTypes();
    }
}
