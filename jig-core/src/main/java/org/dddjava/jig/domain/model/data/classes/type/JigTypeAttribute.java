package org.dddjava.jig.domain.model.data.classes.type;

import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;

/**
 * 型の属性
 */
public class JigTypeAttribute {
    private final ClassComment classComment;

    public JigTypeAttribute(ClassComment classComment) {
        this.classComment = classComment;
    }

    public ClassComment classcomment() {
        return classComment;
    }

    public JigTypeDescription description() {
        return JigTypeDescription.from(classComment.documentationComment());
    }
}
