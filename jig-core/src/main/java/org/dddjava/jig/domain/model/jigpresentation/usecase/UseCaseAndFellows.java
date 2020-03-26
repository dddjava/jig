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
        String useCaseIdentifier = "\"" + useCase.useCaseIdentifier() + "\"";
        String useCaseLabel = useCase.useCaseLabel(aliasFinder);

        StringBuilder sb = new StringBuilder()
                .append(String.format("%s[label=\"%s\",style=filled,fillcolor=lightgoldenrod,shape=ellipse];\n", useCaseIdentifier, useCaseLabel));

        // bold, headなし
        TypeIdentifier returnType = useCase.returnType();
        if (!returnType.isJavaLanguageType()) {
            sb.append(String.format("%s -> %s[style=bold,arrowhead=none];\n", returnType.asSimpleText(), useCaseIdentifier));
        }

        // dashed, headあり
        for (TypeIdentifier requireType : useCase.requireTypes()) {
            // returnでだしたら出力しない
            if (requireType.equals(returnType)) continue;

            sb.append(String.format("%s -> %s[style=dashed,arrowhead=open];\n", useCaseIdentifier, requireType.asSimpleText()));
        }

        // dotted, headあり
        for (TypeIdentifier usingType : useCase.internalUsingTypes()) {
            // returnでだしたら出力しない
            if (usingType.equals(returnType)) continue;
            // requireでだしたら出力しない
            if (useCase.requireTypes().contains(usingType)) continue;

            sb.append(String.format("%s -> %s[style=dashed,arrowhead=open];\n", useCaseIdentifier, usingType.asSimpleText()));
        }
        return sb.toString();
    }
}
