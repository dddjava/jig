package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.types.TypeId;
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
    Set<TypeId> controllerTypes;

    public CompositeUsecases(ServiceAngle serviceAngle) {
        this.usecase = new Usecase(serviceAngle);
        this.controllerTypes = serviceAngle.userControllerTypeIds();
    }

    public String dotText(JigDocumentContext jigDocumentContext) {
        String usecaseIdentifier = usecase.usecaseIdentifier();

        StringBuilder sb = new StringBuilder()
                .append(Nodes.usecase(usecase).asText());

        Set<TypeId> otherTypes = new HashSet<>();

        // 戻り値へのEdge
        Optional<TypeId> primaryType = usecase.primaryType();
        primaryType.ifPresent(typeIdentifier -> {
                    sb.append(String.format("\"%s\" -> \"%s\"[style=bold];\n", typeIdentifier.fullQualifiedName(), usecaseIdentifier));
                    otherTypes.add(typeIdentifier);
                }
        );

        // 引数へのEdge
        for (TypeId requireType : usecase.requireTypes()) {
            sb.append(String.format("\"%s\" -> \"%s\"[style=dashed];\n", usecaseIdentifier, requireType.fullQualifiedName()));
            otherTypes.add(requireType);
        }

        // 内部使用クラスへのEdge
        for (TypeId usingType : usecase.internalUsingTypes()) {
            sb.append(String.format("\"%s\" -> \"%s\"[style=dotted];\n", usecaseIdentifier, usingType.fullQualifiedName()));
            otherTypes.add(usingType);
        }

        // Usecaseが使用しているクラスのNode
        for (TypeId otherType : otherTypes) {
            var term = jigDocumentContext.typeTerm(otherType);
            sb.append(
                    new Node(otherType.fullQualifiedName())
                            .label(term.title())
                            .tooltip(otherType.asSimpleText())
                            .as(NodeRole.脇役)
                            .asText()
            );
        }

        // controllerのNodeおよびedge
        for (TypeId controllerType : controllerTypes) {
            sb.append(
                    new Node(controllerType.fullQualifiedName())
                            .label(jigDocumentContext.typeTerm(controllerType).title())
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
