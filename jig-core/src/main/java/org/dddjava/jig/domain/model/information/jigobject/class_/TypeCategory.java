package org.dddjava.jig.domain.model.information.jigobject.class_;

public enum TypeCategory {
    Usecase,
    InputAdapter,
    OutputAdapter,
    FrameworkComponent,
    Others;

    public boolean isBoundary() {
        return switch (this) {
            case Usecase, InputAdapter, OutputAdapter, FrameworkComponent -> true;
            default -> false;
        };
    }
}
