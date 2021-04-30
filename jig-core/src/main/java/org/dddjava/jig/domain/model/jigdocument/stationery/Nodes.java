package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethod;
import org.dddjava.jig.domain.model.jigmodel.services.Usecase;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;

public enum Nodes {
    ユースケース_ハンドラ {
        @Override
        Node node(String identifier, String label, String tooltip) {
            return new Node(identifier)
                    .shape("ellipse")
                    .label(label)
                    .tooltip(tooltip)
                    .style("filled")
                    .handlerMethod();
        }
    },
    ユースケース_その他 {
        @Override
        Node node(String identifier, String label, String tooltip) {
            return new Node(identifier)
                    .shape("ellipse")
                    .label(label)
                    .tooltip(tooltip)
                    .style("filled")
                    .normalColor();
        }
    },
    ラムダ {
        @Override
        Node node(String identifier, String label, String tooltip) {
            return new Node(identifier)
                    .label("(lambda)")
                    .lambda();
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

    abstract Node node(String identifier, String label, String tooltip);
}
