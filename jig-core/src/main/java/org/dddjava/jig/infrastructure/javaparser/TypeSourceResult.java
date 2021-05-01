package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.parts.class_.method.MethodComment;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;

import java.util.List;

public class TypeSourceResult {
    ClassComment classComment;
    List<MethodComment> methodComments;

    public TypeSourceResult(ClassComment classComment, List<MethodComment> methodComments) {
        this.classComment = classComment;
        this.methodComments = methodComments;
    }
}
