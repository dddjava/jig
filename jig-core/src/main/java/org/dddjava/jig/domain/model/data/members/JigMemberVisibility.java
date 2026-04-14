package org.dddjava.jig.domain.model.data.members;

/**
 * メンバの可視性
 */
public enum JigMemberVisibility {
    PUBLIC,
    PROTECTED,
    PACKAGE,
    PRIVATE;

    public boolean isPublic() {
        return this == PUBLIC;
    }
}
