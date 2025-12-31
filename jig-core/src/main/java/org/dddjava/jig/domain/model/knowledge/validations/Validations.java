package org.dddjava.jig.domain.model.knowledge.validations;

import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * バリデーション一覧
 */
public record Validations(List<Validation> values) {
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("((javax|jakarta).validation|org.hibernate.validator).+");

    public static Validations from(JigTypes jigTypes) {
        List<Validation> list = jigTypes.stream()
                .flatMap(Validations::validationAnnotatedMembers)
                .toList();
        return new Validations(list);
    }

    static Stream<Validation> validationAnnotatedMembers(JigType jigType) {
        Stream<Validation> methodStream = jigType.jigTypeMembers().jigMethodDeclarations().stream()
                .flatMap(jigMethodDeclaration -> jigMethodDeclaration.header().declarationAnnotationStream()
                        // TODO 正規表現の絞り込みをやめる
                        .filter(jigAnnotationReference -> ANNOTATION_PATTERN.matcher(jigAnnotationReference.id().fqn()).matches())
                        .map(jigAnnotationReference -> {
                            return new Validation(
                                    jigType.id(),
                                    jigMethodDeclaration.header().simpleMethodSignatureText(),
                                    jigMethodDeclaration.header().returnType().id(),
                                    jigAnnotationReference.id(),
                                    jigAnnotationReference.asText()
                            );
                        }));
        Stream<Validation> fieldStream = jigType.jigTypeMembers().allJigFieldStream()
                .flatMap(jigField -> jigField.jigFieldHeader().declarationAnnotationStream()
                        // TODO 正規表現の絞り込みをやめる
                        .filter(jigAnnotationReference -> ANNOTATION_PATTERN.matcher(jigAnnotationReference.id().fqn()).matches())
                        .map(jigAnnotationReference -> {
                            return new Validation(
                                    jigType.id(),
                                    jigField.nameText(),
                                    jigField.jigTypeReference().id(),
                                    jigAnnotationReference.id(),
                                    jigAnnotationReference.asText()
                            );
                        }));
        return Stream.concat(fieldStream, methodStream);
    }

    public List<Validation> list() {
        return values.stream()
                // できればメンバのタイプも加えてフィールド→メソッドの順にしたい
                .sorted(Comparator.comparing(Validation::typeId)
                        .thenComparing(Validation::memberName))
                .toList();
    }
}
