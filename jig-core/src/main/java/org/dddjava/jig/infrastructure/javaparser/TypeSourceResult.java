package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;

import java.util.List;

public class TypeSourceResult {
    ClassComment classComment;
    List<MethodComment> methodComments;

    public TypeSourceResult(ClassComment classComment, List<MethodComment> methodComments) {
        this.classComment = classComment;
        this.methodComments = methodComments;
    }
}
