package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの実装から読み取れること
 */
public class JigMethodBuilder {
    private static final Logger logger = LoggerFactory.getLogger(JigMethodBuilder.class);

    private final MethodDeclaration methodDeclaration;
    private final Visibility visibility;
    private final MethodDerivation methodDerivation;
    private final List<TypeIdentifier> throwsTypes;
    private final List<TypeIdentifier> signatureContainedTypes;
    private final List<Annotation> annotations;
    private final Instructions instructions;

    private MethodImplementation methodImplementation = null;

    public JigMethodBuilder(MethodDeclaration methodDeclaration, List<TypeIdentifier> signatureContainedTypes, Visibility visibility, MethodDerivation methodDerivation, List<TypeIdentifier> throwsTypes, List<Annotation> annotationList, Instructions methodInstructions) {
        this.methodDeclaration = methodDeclaration;
        this.visibility = visibility;
        this.methodDerivation = methodDerivation;
        this.throwsTypes = throwsTypes;
        this.signatureContainedTypes = signatureContainedTypes;
        this.annotations = annotationList;
        this.instructions = methodInstructions;
    }

    public JigMethod build() {
        if (instructions == null) {
            logger.warn("{}のinstructionsが設定されていません。メソッド実装に伴う情報は出力されません。", methodDeclaration.identifier());
        }
        if (annotations == null) {
            logger.warn("{}のannotationsが設定されていません。メソッド実装に伴うアノテーションの情報は出力されません。", methodDeclaration.identifier());
        }
        return new JigMethod(
                methodDeclaration,
                annotatedMethods(), visibility, methodDerivation, instructions, throwsTypes, signatureContainedTypes,
                methodImplementation != null ? methodImplementation : MethodImplementation.unknown(methodDeclaration.identifier())
        );
    }

    private MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = annotations.stream()
                .map(annotation -> new MethodAnnotation(annotation, methodDeclaration))
                .collect(Collectors.toList());
        return new MethodAnnotations(methodAnnotations);
    }

    public MethodIdentifier methodIdentifier() {
        return methodDeclaration.identifier();
    }

    public void registerMethodImplementation(MethodImplementation methodImplementation) {
        this.methodImplementation = methodImplementation;
    }
}
