package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigdocument.Node;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethod;

import java.util.List;
import java.util.Optional;

/**
 * ユースケース
 */
public class UseCase {

    ServiceMethod serviceMethod;
    boolean useController;

    public UseCase(ServiceAngle serviceAngle) {
        this.serviceMethod = serviceAngle.serviceMethod();
        this.useController = serviceAngle.usingFromController();
    }

    List<TypeIdentifier> internalUsingTypes() {
        return serviceMethod.internalUsingTypes();
    }

    Optional<TypeIdentifier> primaryType() {
        return serviceMethod.primaryType();
    }

    List<TypeIdentifier> requireTypes() {
        return serviceMethod.requireTypes();
    }

    public String useCaseIdentifier() {
        return serviceMethod.methodDeclaration().asFullNameText();
    }

    public String useCaseLabel(AliasFinder aliasFinder) {
        MethodAlias methodAlias = aliasFinder.find(serviceMethod.methodDeclaration().identifier());
        return methodAlias.asTextOrDefault(serviceMethod.methodDeclaration().declaringType().asSimpleText() + "\\n" + serviceMethod.methodDeclaration().methodSignature().methodName());
    }

    public String dotText(AliasFinder aliasFinder) {
        return node(aliasFinder).asText();
    }

    public Node node(AliasFinder aliasFinder) {
        Node node = new Node(useCaseIdentifier())
                .shape("ellipse")
                .label(useCaseLabel(aliasFinder))
                .tooltip(serviceMethod.methodDeclaration().asSimpleTextWithDeclaringType())
                .style("filled");
        return useController ? node.handlerMethod() : node.normalColor();
    }
}
