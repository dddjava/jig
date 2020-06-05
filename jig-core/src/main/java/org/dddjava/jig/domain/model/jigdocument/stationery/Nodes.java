package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethod;
import org.dddjava.jig.domain.model.jigmodel.services.Usecase;

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

    public static Node usecase(AliasFinder aliasFinder, Usecase usecase) {
        return (usecase.isHandler() ? ユースケース_ハンドラ : ユースケース_その他).node(
                usecase.usecaseIdentifier(),
                usecase.usecaseLabel(aliasFinder),
                usecase.simpleTextWithDeclaringType()
        );
    }

    public static Node usecase(AliasFinder aliasFinder, ServiceMethod serviceMethod) {
        return ユースケース_その他.node(
                serviceMethod.methodDeclaration().asFullNameText(),
                aliasFinder.methodText(serviceMethod.methodDeclaration().identifier()),
                serviceMethod.methodDeclaration().asSimpleTextWithDeclaringType()
        );
    }

    public static Node lambda(MethodDeclaration method) {
        return ラムダ.node(method.asFullNameText(), "", "");
    }

    abstract Node node(String identifier, String label, String tooltip);
}
