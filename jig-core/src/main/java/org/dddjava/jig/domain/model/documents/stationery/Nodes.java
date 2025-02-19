package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.knowledge.core.Usecase;

public class Nodes {

    public static Node usecase(Usecase usecase) {
        return new Node(usecase.usecaseIdentifier())
                .shape("ellipse")
                .label(usecase.usecaseLabel())
                .tooltip(usecase.simpleTextWithDeclaringType())
                .as(usecase.isHandler() ? NodeRole.主役 : NodeRole.準主役)
                .url(usecase.declaringType(), JigDocument.ApplicationSummary);
    }

    public static Node usecase(ServiceMethod serviceMethod) {
        JigMethod jigMethod = serviceMethod.method();
        return new Node(jigMethod.jigMethodIdentifier().value())
                .shape("ellipse")
                .label(jigMethod.aliasText())
                .tooltip(jigMethod.simpleText())
                .as(NodeRole.準主役)
                .url(jigMethod.jigMethodDeclaration().declaringTypeIdentifier(), JigDocument.ApplicationSummary);
    }

    public static Node lambda(JigMethod method) {
        return new Node(method.jigMethodIdentifier().value())
                .label("(lambda)").as(NodeRole.モブ).shape("ellipse");
    }

    public static Node businessRuleNodeOf(JigType jigType) {
        return new Node(jigType.id().fullQualifiedName())
                .label(jigType.nodeLabel())
                .url(jigType.id(), JigDocument.DomainSummary);
    }
}
