package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ユースケースと愉快な仲間たち
 */
public class UseCaseAndFellows {
    UseCase useCase;
    List<TypeIdentifier> controllerTypes;

    UseCaseAndFellows(ServiceAngle serviceAngle) {
        this.useCase = new UseCase(serviceAngle.serviceMethod());
        this.controllerTypes = serviceAngle.userControllerMethods().list().stream()
                .map(methodDeclaration -> methodDeclaration.declaringType())
                .distinct()
                .collect(Collectors.toList());
    }

    public String dotText(AliasFinder aliasFinder) {
        String useCaseIdentifier = useCase.useCaseIdentifier();

        StringBuilder sb = new StringBuilder()
                .append(String.format("\"%s\"[label=\"%s\",style=filled,fillcolor=lightgoldenrod,shape=ellipse];\n", useCaseIdentifier, useCase.useCaseLabel(aliasFinder)));

        Set<TypeIdentifier> otherTypes = new HashSet<>();

        // bold, headなし
        TypeIdentifier returnType = useCase.returnType();
        if (!returnType.isJavaLanguageType()) {
            sb.append(String.format("\"%s\" -> \"%s\"[style=bold,arrowhead=none];\n", returnType.fullQualifiedName(), useCaseIdentifier));

            otherTypes.add(returnType);
        }

        // dashed, headあり
        for (TypeIdentifier requireType : useCase.requireTypes()) {
            // returnでだしたら出力しない
            if (requireType.equals(returnType)) continue;

            sb.append(String.format("\"%s\" -> \"%s\"[style=dashed,arrowhead=open];\n", useCaseIdentifier, requireType.fullQualifiedName()));
            otherTypes.add(requireType);
        }

        // dotted, headあり
        for (TypeIdentifier usingType : useCase.internalUsingTypes()) {
            // returnでだしたら出力しない
            if (usingType.equals(returnType)) continue;
            // requireでだしたら出力しない
            if (useCase.requireTypes().contains(usingType)) continue;

            sb.append(String.format("\"%s\" -> \"%s\"[style=dashed,arrowhead=open];\n", useCaseIdentifier, usingType.fullQualifiedName()));
            otherTypes.add(usingType);
        }

        for (TypeIdentifier otherType : otherTypes) {
            TypeAlias typeAlias = aliasFinder.find(otherType);
            sb.append(String.format("\"%s\"[label=\"%s\"];\n", otherType.fullQualifiedName(), typeAlias.asTextOrDefault(otherType.asSimpleText())));
        }

        for (TypeIdentifier controllerType : controllerTypes) {
            // controllerの表示
            TypeAlias typeAlias = aliasFinder.find(controllerType);
            sb.append(String.format("\"%s\"[label=\"%s\",style=filled,fillcolor=lightgray,shape=Msquare];\n",
                    controllerType.fullQualifiedName(), typeAlias.asTextOrDefault(controllerType.asSimpleText())));

            // dotted, headあり
            sb.append(String.format("\"%s\" -> \"%s\"[style=dotted,arrowhead=open];\n", controllerType.fullQualifiedName(), useCaseIdentifier));
        }

        return sb.toString();
    }
}
