package org.dddjava.jig.domain.model.knowledge.validations;

import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * バリデーション一覧
 */
public class Validations {
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("((javax|jakarta).validation|org.hibernate.validator).+");

    List<Validation> list;

    public Validations(List<Validation> list) {
        this.list = list;
    }

    public static Validations from(JigTypes jigTypes) {
        List<Validation> list = jigTypes.stream()
                .flatMap(Validations::validationAnnotatedMembers)
                .collect(Collectors.toList());
        return new Validations(list);
    }

    static Stream<Validation> validationAnnotatedMembers(JigType jigType) {
        Stream<Validation> methodStream = jigType.jigTypeMembers().jigMethodDeclarations().stream()
                .flatMap(jigMethodDeclaration -> jigMethodDeclaration.header().jigMethodAttribute().declarationAnnotations().stream()
                        // TODO 正規表現の絞り込みをやめる
                        .filter(jigAnnotationReference -> ANNOTATION_PATTERN.matcher(jigAnnotationReference.id().fullQualifiedName()).matches())
                        .map(jigAnnotationReference -> {
                            return new Validation(
                                    jigType.identifier(),
                                    jigMethodDeclaration.name(),
                                    jigMethodDeclaration.header().jigMethodAttribute().returnType().id(),
                                    jigAnnotationReference.id(),
                                    jigAnnotationReference.asText()
                            );
                        }));
        Stream<Validation> fieldStream = jigType.jigTypeMembers().jigFieldHeaders().stream()
                .flatMap(jigFieldHeader -> jigFieldHeader.jigFieldAttribute().declarationAnnotations().stream()
                        // TODO 正規表現の絞り込みをやめる
                        .filter(jigAnnotationReference -> ANNOTATION_PATTERN.matcher(jigAnnotationReference.id().fullQualifiedName()).matches())
                        .map(jigAnnotationReference -> {
                            return new Validation(
                                    jigType.identifier(),
                                    jigFieldHeader.name(),
                                    jigFieldHeader.jigTypeReference().id(),
                                    jigAnnotationReference.id(),
                                    jigAnnotationReference.asText()
                            );
                        }));
        return Stream.concat(fieldStream, methodStream);
    }

    public List<Validation> list() {
        return list.stream()
                .sorted(Comparator.comparing(validation -> validation.typeIdentifier()))
                .collect(Collectors.toList());
    }
}
