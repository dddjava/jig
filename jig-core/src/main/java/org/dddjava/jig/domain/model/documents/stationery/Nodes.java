package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.models.applications.services.ServiceMethod;
import org.dddjava.jig.domain.model.models.applications.services.Usecase;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;

public class Nodes {

    public static Node usecase(JigDocumentContext jigDocumentContext, Usecase usecase) {
        return new Node(usecase.usecaseIdentifier())
                .shape("ellipse")
                .label(usecase.usecaseLabel())
                .tooltip(usecase.simpleTextWithDeclaringType())
                .as(usecase.isHandler() ? NodeRole.主役 : NodeRole.準主役)
                .url(usecase.declaringType(), jigDocumentContext);
    }

    public static Node usecase(JigDocumentContext jigDocumentContext, ServiceMethod serviceMethod) {
        return new Node(serviceMethod.methodDeclaration().asFullNameText())
                .shape("ellipse")
                .label(serviceMethod.method().aliasText())
                .tooltip(serviceMethod.methodDeclaration().asSimpleTextWithDeclaringType())
                .as(NodeRole.準主役)
                .url(serviceMethod.methodDeclaration().declaringType(), jigDocumentContext);
    }

    public static Node lambda(MethodDeclaration method) {
        return new Node(method.asFullNameText())
                .label("(lambda)").as(NodeRole.モブ).shape("ellipse");
    }

    public static Node businessRuleNodeOf(BusinessRule businessRule) {
        return new Node(businessRule.typeIdentifier().fullQualifiedName())
                .label(businessRule.nodeLabel());
    }
}
