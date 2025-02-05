package org.dddjava.jig.domain.model.sources.javasources;

import org.dddjava.jig.domain.model.data.classes.method.MethodImplementation;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;
import org.dddjava.jig.domain.model.sources.javasources.comment.PackageComment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * javaファイル由来のソース
 */
public class JavaSourceModel {

    List<ClassComment> classComments;
    public List<MethodImplementation> methodImplementations;
    List<EnumModel> enumModels;
    private List<PackageComment> packageComments;

    public JavaSourceModel(List<ClassComment> classComments, List<MethodImplementation> methodImplementations, List<EnumModel> enumModels, List<PackageComment> packageComments) {
        this.classComments = classComments;
        this.methodImplementations = methodImplementations;
        this.enumModels = enumModels;
        this.packageComments = packageComments;
    }

    public static JavaSourceModel from(List<ClassComment> classComments, List<MethodImplementation> methodImplementations, List<EnumModel> enumModels) {
        return new JavaSourceModel(classComments, methodImplementations, enumModels, List.of());
    }

    public static JavaSourceModel from(PackageComment packageComment) {
        return new JavaSourceModel(List.of(), List.of(), List.of(), List.of(packageComment));
    }

    public static JavaSourceModel empty() {
        return new JavaSourceModel(List.of(), List.of(), List.of(), List.of());
    }

    public EnumModels enumModels() {
        return new EnumModels(enumModels);
    }

    public JavaSourceModel merge(JavaSourceModel other) {
        return new JavaSourceModel(
                Stream.concat(classComments.stream(), other.classComments.stream()).collect(Collectors.toList()),
                Stream.concat(methodImplementations.stream(), other.methodImplementations.stream()).collect(Collectors.toList()),
                Stream.concat(enumModels.stream(), other.enumModels.stream()).collect(Collectors.toList()),
                Stream.concat(packageComments.stream(), other.packageComments.stream()).collect(Collectors.toList())
        );
    }

    public List<ClassComment> classCommentList() {
        return classComments;
    }

    public List<PackageComment> packageComments() {
        return packageComments;
    }

    public Glossary toTerms() {
        var list = Stream.of(
                        classCommentList().stream()
                                .map(classComment -> Term.fromClass(
                                        classComment.typeIdentifier(),
                                        classComment.asTextOrIdentifierSimpleText(),
                                        classComment.documentationComment().bodyText())),
                        methodImplementations.stream()
                                .filter(MethodImplementation::hasComment)
                                .map(methodImplementation -> Term.fromMethod(
                                        methodImplementation.methodIdentifierText(),
                                        methodImplementation.comment().asText(),
                                        methodImplementation.comment().bodyText()
                                )),
                        packageComments().stream()
                                .map(packageComment -> Term.fromPackage(
                                        packageComment.packageIdentifier(),
                                        packageComment.summaryOrSimpleName(),
                                        packageComment.descriptionComment().bodyText()
                                ))
                ).flatMap(term -> term)
                .toList();
        return new Glossary(list);
    }
}
