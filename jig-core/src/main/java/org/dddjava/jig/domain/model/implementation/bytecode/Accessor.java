package org.dddjava.jig.domain.model.implementation.bytecode;

public enum Accessor {
    PUBLIC,
    NOT_PUBLIC;

    public boolean isPublic() {
        return this == PUBLIC;
    }

    public boolean isNotPublic() {
        return !isPublic();
    }
}
