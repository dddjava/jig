package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;

/**
 * ユースケースと愉快な仲間たち
 */
public class UseCaseAndFellows {
    UseCase useCase;

    UseCaseAndFellows(ServiceAngle serviceAngle) {
        useCase = new UseCase(serviceAngle.serviceMethod());
    }

    public String dotText() {
        String useCaseName = useCase.useCaseName();

        StringBuilder sb = new StringBuilder()
                .append(useCaseName)
                .append("[style=filled,fillcolor=lightgoldenrod,shape=ellipse];\n");

        for (String relationName : useCase.listRelationTexts()) {
            sb.append(relationName);
            sb.append(" -- ");
            sb.append(useCaseName);
            sb.append(";\n");
        }
        return sb.toString();
    }
}
