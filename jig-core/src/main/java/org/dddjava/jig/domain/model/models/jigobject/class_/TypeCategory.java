package org.dddjava.jig.domain.model.models.jigobject.class_;

public enum TypeCategory {
    Domain,
    Application,
    Others,
    RequestHandler,
    FrameworkComponent,
    Infrastructure;

    public boolean isApplicationComponent() {
        return switch (this) {
            case Application, RequestHandler, FrameworkComponent, Infrastructure -> true;
            default -> false;
        };
    }
}
