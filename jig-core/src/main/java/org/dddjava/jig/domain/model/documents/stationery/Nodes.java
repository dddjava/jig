package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.applications.usecases.ServiceMethod;
import org.dddjava.jig.domain.model.knowledge.core.Usecase;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;

public class Nodes {

    public static Node usecase(JigDocumentContext jigDocumentContext, Usecase usecase) {
        return new Node(usecase.usecaseIdentifier())
                .shape("ellipse")
                .label(usecase.usecaseLabel())
                .tooltip(usecase.simpleTextWithDeclaringType())
                .as(usecase.isHandler() ? NodeRole.主役 : NodeRole.準主役)
                .url(usecase.declaringType(), jigDocumentContext, JigDocument.ApplicationSummary);
    }

    public static Node usecase(JigDocumentContext jigDocumentContext, ServiceMethod serviceMethod) {
        return new Node(serviceMethod.methodDeclaration().asFullNameText())
                .shape("ellipse")
                .label(serviceMethod.method().aliasText())
                .tooltip(serviceMethod.methodDeclaration().asSimpleTextWithDeclaringType())
                .as(NodeRole.準主役)
                .url(serviceMethod.methodDeclaration().declaringType(), jigDocumentContext, JigDocument.ApplicationSummary);
    }

    public static Node lambda(MethodDeclaration method) {
        return new Node(method.asFullNameText())
                .label("(lambda)").as(NodeRole.モブ).shape("ellipse");
    }

    public static Node businessRuleNodeOf(JigType jigType, JigDocumentContext jigDocumentContext) {
        return new Node(jigType.typeIdentifier().fullQualifiedName())
                .label(jigType.nodeLabel())
                .url(jigType.typeIdentifier(), jigDocumentContext, JigDocument.DomainSummary);
    }
}
