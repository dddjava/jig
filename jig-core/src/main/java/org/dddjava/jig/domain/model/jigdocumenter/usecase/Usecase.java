package org.dddjava.jig.domain.model.jigdocumenter.usecase;

import org.dddjava.jig.domain.model.jigdocumenter.stationery.Node;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethod;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.util.List;
import java.util.Optional;

/**
 * ユースケース
 */
public class Usecase {

    ServiceMethod serviceMethod;
    UsecaseCategory usecaseCategory;

    public Usecase(ServiceAngle serviceAngle) {
        this.serviceMethod = serviceAngle.serviceMethod();
        this.usecaseCategory = UsecaseCategory.resolver(serviceAngle);
    }

    public List<TypeIdentifier> internalUsingTypes() {
        return serviceMethod.internalUsingTypes();
    }

    public Optional<TypeIdentifier> primaryType() {
        return serviceMethod.primaryType();
    }

    public List<TypeIdentifier> requireTypes() {
        return serviceMethod.requireTypes();
    }

    public String usecaseIdentifier() {
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
        Node node = new Node(usecaseIdentifier())
                .shape("ellipse")
                .label(useCaseLabel(aliasFinder))
                .tooltip(serviceMethod.methodDeclaration().asSimpleTextWithDeclaringType())
                .style("filled");
        return usecaseCategory.handler() ? node.handlerMethod() : node.normalColor();
    }
}
