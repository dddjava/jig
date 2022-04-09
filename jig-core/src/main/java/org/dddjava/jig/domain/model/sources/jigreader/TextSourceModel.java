package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModel;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodImplementation;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * テキストソースから読み取れること
 */
public class TextSourceModel {

    List<ClassComment> classComments;
    List<MethodImplementation> methodImplementations;
    List<EnumModel> enumModels;

    public TextSourceModel(List<ClassComment> classComments, List<MethodImplementation> methodImplementations, List<EnumModel> enumModels) {
        this.classComments = classComments;
        this.methodImplementations = methodImplementations;
        this.enumModels = enumModels;
    }

    public TextSourceModel(ClassAndMethodComments classAndMethodComments) {
        this(classAndMethodComments.list(),
                classAndMethodComments.methodList().stream()
                        .map(methodComment -> new MethodImplementation(methodComment.methodIdentifier(), methodComment))
                        .collect(Collectors.toList()),
                List.of());
    }

    public static TextSourceModel empty() {
        return new TextSourceModel(List.of(), List.of(), List.of());
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
                Stream.concat(methodImplementations.stream(), other.methodImplementations.stream()).collect(Collectors.toList()),
                Stream.concat(enumModels.stream(), other.enumModels.stream()).collect(Collectors.toList())
        );
    }

    public List<ClassComment> classCommentList() {
        return classComments;
    }

    public List<MethodComment> methodCommentList() {
        return methodImplementations.stream()
                .flatMap(methodImplementation -> methodImplementation.comment().stream())
                .collect(Collectors.toList());
    }

    public Optional<MethodImplementation> methodImplementation(MethodIdentifier methodIdentifier) {
        return methodImplementations.stream()
                .filter(methodImplementation -> methodImplementation.matches(methodIdentifier))
                .findAny();
    }
}
