package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.stationery.Node;
import org.dddjava.jig.domain.model.documents.stationery.NodeRole;
import org.dddjava.jig.domain.model.documents.stationery.Nodes;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngle;
import org.dddjava.jig.domain.model.knowledge.core.Usecase;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * ユースケースと愉快な仲間たち
 */
class CompositeUsecases {
    Usecase usecase;
    Set<TypeIdentifier> controllerTypes;

    public CompositeUsecases(ServiceAngle serviceAngle) {
        this.usecase = new Usecase(serviceAngle);
        this.controllerTypes = serviceAngle.userControllerTypeIdentifiers();
    }

    public String dotText(JigDocumentContext jigDocumentContext) {
        String usecaseIdentifier = usecase.usecaseIdentifier();

        StringBuilder sb = new StringBuilder()
                .append(Nodes.usecase(jigDocumentContext, usecase).asText());

        Set<TypeIdentifier> otherTypes = new HashSet<>();

        // 戻り値へのEdge
        Optional<TypeIdentifier> primaryType = usecase.primaryType();
        primaryType.ifPresent(typeIdentifier -> {
                    sb.append(String.format("\"%s\" -> \"%s\"[style=bold];\n", typeIdentifier.fullQualifiedName(), usecaseIdentifier));
                    otherTypes.add(typeIdentifier);
                }
        );

        // 引数へのEdge
        for (TypeIdentifier requireType : usecase.requireTypes()) {
            sb.append(String.format("\"%s\" -> \"%s\"[style=dashed];\n", usecaseIdentifier, requireType.fullQualifiedName()));
            otherTypes.add(requireType);
        }

        // 内部使用クラスへのEdge
        for (TypeIdentifier usingType : usecase.internalUsingTypes()) {
            sb.append(String.format("\"%s\" -> \"%s\"[style=dotted];\n", usecaseIdentifier, usingType.fullQualifiedName()));
            otherTypes.add(usingType);
        }

        // Usecaseが使用しているクラスのNode
        for (TypeIdentifier otherType : otherTypes) {
            ClassComment classComment = jigDocumentContext.classComment(otherType);
            sb.append(
                    new Node(otherType.fullQualifiedName())
                            .label(classComment.asTextOrIdentifierSimpleText())
                            .tooltip(otherType.asSimpleText())
                            .as(NodeRole.脇役)
                            .asText()
            );
        }

        // controllerのNodeおよびedge
        for (TypeIdentifier controllerType : controllerTypes) {
            ClassComment classComment = jigDocumentContext.classComment(controllerType);
            sb.append(
                    new Node(controllerType.fullQualifiedName())
                            .label(classComment.asTextOrIdentifierSimpleText())
                            .tooltip(controllerType.asSimpleText())
                            .as(NodeRole.モブ)
                            .asText()
            );

            // dotted, headあり
            sb.append(String.format("\"%s\" -> \"%s\"[style=dotted];\n", controllerType.fullQualifiedName(), usecaseIdentifier));
        }

        return sb.toString();
    }
}
