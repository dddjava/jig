package org.dddjava.jig.domain.model.jigmodel.usecase;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigdocument.Node;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ユースケースと愉快な仲間たち
 */
public class UseCaseAndFellows {
    UseCase useCase;
    List<TypeIdentifier> controllerTypes;

    public UseCaseAndFellows(ServiceAngle serviceAngle) {
        this.useCase = new UseCase(serviceAngle);
        this.controllerTypes = serviceAngle.userControllerMethods().list().stream()
                .map(methodDeclaration -> methodDeclaration.declaringType())
                .distinct()
                .collect(Collectors.toList());
    }

    public String dotText(AliasFinder aliasFinder) {
        String useCaseIdentifier = useCase.useCaseIdentifier();

        StringBuilder sb = new StringBuilder()
                .append(useCase.dotText(aliasFinder));

        Set<TypeIdentifier> otherTypes = new HashSet<>();

        // 戻り値へのEdge
        // bold, headなし
        Optional<TypeIdentifier> primaryType = useCase.primaryType();
        primaryType.ifPresent(typeIdentifier -> {
                    sb.append(String.format("\"%s\" -> \"%s\"[style=bold];\n", typeIdentifier.fullQualifiedName(), useCaseIdentifier));
            otherTypes.add(typeIdentifier);
                }
        );

        // 引数へのEdge
        // dashed, headあり
        for (TypeIdentifier requireType : useCase.requireTypes()) {
            sb.append(String.format("\"%s\" -> \"%s\"[style=dashed];\n", useCaseIdentifier, requireType.fullQualifiedName()));
            otherTypes.add(requireType);
        }

        // 内部使用クラスへのEdge
        // dotted, headあり
        for (TypeIdentifier usingType : useCase.internalUsingTypes()) {
            sb.append(String.format("\"%s\" -> \"%s\"[style=dashed];\n", useCaseIdentifier, usingType.fullQualifiedName()));
            otherTypes.add(usingType);
        }

        // UseCaseが使用しているクラスのNode
        for (TypeIdentifier otherType : otherTypes) {
            TypeAlias typeAlias = aliasFinder.find(otherType);
            sb.append(
                    new Node(otherType.fullQualifiedName())
                            .label(typeAlias.asTextOrDefault(otherType.asSimpleText()))
                            .tooltip(otherType.asSimpleText())
                            .asText()
            );
        }

        // controllerのNodeおよびedge
        for (TypeIdentifier controllerType : controllerTypes) {
            TypeAlias typeAlias = aliasFinder.find(controllerType);
            sb.append(
                    new Node(controllerType.fullQualifiedName())
                            .label(typeAlias.asTextOrDefault(controllerType.asSimpleText()))
                            .tooltip(controllerType.asSimpleText())
                            .screenNode()
                            .asText()
            );

            // dotted, headあり
            sb.append(String.format("\"%s\" -> \"%s\"[style=dotted];\n", controllerType.fullQualifiedName(), useCaseIdentifier));
        }

        return sb.toString();
    }
}
