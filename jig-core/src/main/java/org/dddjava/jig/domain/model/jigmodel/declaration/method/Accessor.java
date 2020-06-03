package org.dddjava.jig.domain.model.jigmodel.declaration.method;

/**
 * メソッドの可視性
 */
public enum Accessor {
    PUBLIC,
    NOT_PUBLIC;

    public boolean isPublic() {
        return this == PUBLIC;
    }
}
