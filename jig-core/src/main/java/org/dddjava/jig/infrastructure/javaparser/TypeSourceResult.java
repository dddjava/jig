package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModel;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;

import java.util.List;

public class TypeSourceResult {
    final ClassComment classComment;
    final List<MethodComment> methodComments;
    final EnumModel enumModel;

    public TypeSourceResult(ClassComment classComment, List<MethodComment> methodComments, EnumModel enumModel) {
        this.classComment = classComment;
        this.methodComments = methodComments;
        this.enumModel = enumModel;
    }

    void collectClassComment(List<ClassComment> collector) {
        if (classComment != null) collector.add(classComment);
    }

    void collectMethodComments(List<MethodComment> collector) {
        collector.addAll(methodComments);
    }

    void collectEnum(List<EnumModel> collector) {
        if (enumModel != null) collector.add(enumModel);
    }
}
