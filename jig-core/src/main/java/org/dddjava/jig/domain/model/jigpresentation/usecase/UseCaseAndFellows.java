package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;

/**
 * ユースケースと愉快な仲間たち
 */
public class UseCaseAndFellows {
    UseCase useCase;

    UseCaseAndFellows(ServiceAngle serviceAngle) {
        useCase = new UseCase(serviceAngle.serviceMethod());
    }

    public String dotText(AliasFinder aliasFinder) {
        String useCaseName = "\"" + useCase.useCaseIdentifier() + "\"";
        String useCaseLabel = "\"" + useCase.useCaseLabel(aliasFinder) + "\"";

        StringBuilder sb = new StringBuilder()
                .append(useCaseName).append("[label=").append(useCaseLabel).append(",style=filled,fillcolor=lightgoldenrod,shape=ellipse];\n");

        // bold, headなし
        TypeIdentifier returnType = useCase.returnType();
        if (!returnType.isJavaLanguageType()) {
            sb.append(returnType.asSimpleText());
            sb.append(" -> ");
            sb.append(useCaseName);
            sb.append("[style=bold,arrowhead=none]");
            sb.append(";\n");
        }

        // dashed, headあり
        for (TypeIdentifier requireType : useCase.requireTypes()) {
            // returnでだしたら出力しない
            if (requireType.equals(returnType)) continue;

            sb.append(useCaseName);
            sb.append(" -> ");
            sb.append(requireType.asSimpleText());
            sb.append("[style=dashed,arrowhead=normal]");
            sb.append(";\n");
        }

        // dotted, headあり
        for (TypeIdentifier typeIdentifier : useCase.internalUsingTypes()) {
            // returnでだしたら出力しない
            if (typeIdentifier.equals(returnType)) continue;
            // requireでだしたら出力しない
            if (useCase.requireTypes().contains(typeIdentifier)) continue;

            sb.append(useCaseName);
            sb.append(" -> ");
            sb.append(typeIdentifier.asSimpleText());
            sb.append("[style=dotted,arrowhead=normal]");
            sb.append(";\n");
        }
        return sb.toString();
    }
}
