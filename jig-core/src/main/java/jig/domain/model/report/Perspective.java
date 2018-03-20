package jig.domain.model.report;

import jig.domain.model.report.method.MethodPerspective;
import jig.domain.model.report.type.TypePerspective;

public enum Perspective {
    SERVICE(MethodPerspective.SERVICE),
    REPOSITORY(MethodPerspective.REPOSITORY),
    IDENTIFIER(TypePerspective.IDENTIFIER),
    ENUM(TypePerspective.ENUM),
    NUMBER(TypePerspective.NUMBER),
    COLLECTION(TypePerspective.COLLECTION),
    DATE(TypePerspective.DATE),
    TERM(TypePerspective.TERM);

    private TypePerspective typePerspective;
    private MethodPerspective methodPerspective;

    Perspective(TypePerspective typePerspective) {
        this.typePerspective = typePerspective;
    }

    Perspective(MethodPerspective methodPerspective) {
        this.methodPerspective = methodPerspective;
    }

    public boolean isMethod() {
        return methodPerspective != null;
    }

    public MethodPerspective getMethodPerspective() {
        return methodPerspective;
    }

    public TypePerspective getTypePerspective() {
        return typePerspective;
    }
}
