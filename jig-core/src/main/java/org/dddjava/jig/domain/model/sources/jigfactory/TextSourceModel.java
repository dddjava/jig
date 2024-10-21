package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModel;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodImplementation;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.term.Term;
import org.dddjava.jig.domain.model.parts.term.Terms;

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
    private List<PackageComment> packageComments;

    public TextSourceModel(List<ClassComment> classComments, List<MethodImplementation> methodImplementations, List<EnumModel> enumModels) {
        this(classComments, methodImplementations, enumModels, List.of());
    }

    public TextSourceModel(List<ClassComment> classComments, List<MethodImplementation> methodImplementations, List<EnumModel> enumModels, List<PackageComment> packageComments) {
        this.classComments = classComments;
        this.methodImplementations = methodImplementations;
        this.enumModels = enumModels;
        this.packageComments = packageComments;
    }

    public static TextSourceModel empty() {
        return new TextSourceModel(List.of(), List.of(), List.of());
    }

    public EnumModels enumModels() {
        return new EnumModels(enumModels);
    }

    public TextSourceModel merge(TextSourceModel other) {
        return new TextSourceModel(
                Stream.concat(classComments.stream(), other.classComments.stream()).collect(Collectors.toList()),
                Stream.concat(methodImplementations.stream(), other.methodImplementations.stream()).collect(Collectors.toList()),
                Stream.concat(enumModels.stream(), other.enumModels.stream()).collect(Collectors.toList()),
                this.packageComments
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

    public void addPackageComment(List<PackageComment> list) {
        this.packageComments = list;
    }

    public List<PackageComment> packageComments() {
        return packageComments;
    }

    public Terms toTerms() {
        var list = Stream.of(
                        classCommentList().stream()
                                .map(classComment -> Term.fromClass(
                                        classComment.typeIdentifier(),
                                        classComment.asTextOrIdentifierSimpleText(),
                                        classComment.documentationComment().bodyText())),
                        methodCommentList().stream()
                                .map(methodComment -> Term.fromMethod(
                                        methodComment.methodIdentifier(),
                                        methodComment.asTextOrDefault(methodComment.methodIdentifier().methodSignature().methodName()),
                                        methodComment.documentationComment().bodyText()
                                )),
                        packageComments().stream()
                                .map(packageComment -> Term.fromPackage(
                                        packageComment.packageIdentifier(),
                                        packageComment.summaryOrSimpleName(),
                                        packageComment.descriptionComment().bodyText()
                                ))
                ).flatMap(term -> term)
                .toList();
        return new Terms(list);
    }
}
