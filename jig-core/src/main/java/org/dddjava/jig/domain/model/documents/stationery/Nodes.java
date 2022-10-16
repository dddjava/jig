package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.models.applications.services.ServiceMethod;
import org.dddjava.jig.domain.model.models.applications.services.Usecase;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;

public enum Nodes {
    ユースケース_ハンドラ {
        @Override
        Node node(String identifier, String label, String tooltip) {
            return new Node(identifier)
                    .shape("ellipse")
                    .label(label)
                    .tooltip(tooltip)
                    .as(NodeRole.スポットライト);
        }
    },
    ユースケース_その他 {
        @Override
        Node node(String identifier, String label, String tooltip) {
            return new Node(identifier)
                    .shape("ellipse")
                    .label(label)
                    .tooltip(tooltip)
                    .as(NodeRole.主役);
        }
    },
    ラムダ {
        @Override
        Node node(String identifier, String label, String tooltip) {
            return new Node(identifier)
                        .label("(lambda)").as(NodeRole.モブ).shape("ellipse");
        }
    };

    public static Node usecase(JigDocumentContext jigDocumentContext, Usecase usecase) {
        return (usecase.isHandler() ? ユースケース_ハンドラ : ユースケース_その他).node(
                usecase.usecaseIdentifier(),
                usecase.usecaseLabel(),
                usecase.simpleTextWithDeclaringType()
        ).url(usecase.declaringType(), jigDocumentContext);
    }

    public static Node usecase(JigDocumentContext jigDocumentContext, ServiceMethod serviceMethod) {
        return ユースケース_その他.node(
                serviceMethod.methodDeclaration().asFullNameText(),
                serviceMethod.method().aliasText(),
                serviceMethod.methodDeclaration().asSimpleTextWithDeclaringType()
        ).url(serviceMethod.methodDeclaration().declaringType(), jigDocumentContext);
    }

    public static Node lambda(MethodDeclaration method) {
        return ラムダ.node(method.asFullNameText(), "", "");
    }

    public static Node businessRuleNodeOf(BusinessRule businessRule) {
        Node node = new Node(businessRule.typeIdentifier().fullQualifiedName())
                .label(businessRule.nodeLabel());
        return businessRule.markedCore() ? node.as(NodeRole.スポットライト) : node;
    }

    abstract Node node(String identifier, String label, String tooltip);
}
