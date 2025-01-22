package org.dddjava.jig.domain.model.information.jigobject.class_;

public enum TypeCategory {
    Domain,
    Service,
    Others,
    RequestHandler,
    FrameworkComponent,
    Infrastructure;

    public boolean isApplicationComponent() {
        return switch (this) {
            case Service, RequestHandler, FrameworkComponent, Infrastructure -> true;
            default -> false;
        };
    }
}
