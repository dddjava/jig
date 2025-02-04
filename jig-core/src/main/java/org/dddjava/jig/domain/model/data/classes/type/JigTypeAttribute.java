package org.dddjava.jig.domain.model.data.classes.type;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.Annotations;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;

/**
 * 型の属性
 */
public class JigTypeAttribute {
    private final ClassComment classComment;

    private final List<Annotation> annotations;

    public JigTypeAttribute(ClassComment classComment, List<Annotation> annotations) {
        this.classComment = classComment;
        this.annotations = annotations;
    }

    public ClassComment classcomment() {
        return classComment;
    }

    public JigTypeDescription description() {
        return JigTypeDescription.from(classComment.documentationComment());
    }

    public Annotations annotationsOf(TypeIdentifier typeIdentifier) {
        return new Annotations(annotations).filterAny(typeIdentifier);
    }
}
