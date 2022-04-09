package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModel;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * テキストソースから読み取れること
 */
public class TextSourceModel {

    List<ClassComment> classComments;
    List<MethodComment> methodComments;
    List<EnumModel> enumModels;

    public TextSourceModel(List<ClassComment> classComments, List<MethodComment> methodComments, List<EnumModel> enumModels) {
        this.classComments = classComments;
        this.methodComments = methodComments;
        this.enumModels = enumModels;
    }

    public TextSourceModel(ClassAndMethodComments classAndMethodComments) {
        this(classAndMethodComments.list(), classAndMethodComments.methodList(), List.of());
    }

    public static TextSourceModel empty() {
        return new TextSourceModel(List.of(), List.of(), List.of());
    }

    public ClassAndMethodComments classAndMethodComments() {
        return new ClassAndMethodComments(classComments, methodComments);
    }

    public EnumModels enumModels() {
        return new EnumModels(enumModels);
    }

    public TextSourceModel addClassAndMethodComments(ClassAndMethodComments... others) {
        TextSourceModel result = this;
        for (ClassAndMethodComments other : others) {
            result = result.merge(new TextSourceModel(other));
        }
        return result;
    }

    public TextSourceModel merge(TextSourceModel other) {
        return new TextSourceModel(
                Stream.concat(classComments.stream(), other.classComments.stream()).collect(Collectors.toList()),
                Stream.concat(methodComments.stream(), other.methodComments.stream()).collect(Collectors.toList()),
                Stream.concat(enumModels.stream(), other.enumModels.stream()).collect(Collectors.toList())
        );
    }
}
