package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.objectweb.asm.Opcodes;
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

    public static MethodDerivation resolveMethodDerivation(MethodDeclaration methodDeclaration, int access, boolean isEnum, boolean isRecordComponent) {
        MethodSignature methodSignature = methodDeclaration.methodSignature();
        String name = methodSignature.methodName();
        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return MethodDerivation.CONSTRUCTOR;
        }

        if ((access & Opcodes.ACC_BRIDGE) != 0 || (access & Opcodes.ACC_SYNTHETIC) != 0) {
            return MethodDerivation.COMPILER_GENERATED;
        }

        if (isRecordComponent) {
            return MethodDerivation.RECORD_COMPONENT;
        }

        if (isEnum && (access & Opcodes.ACC_STATIC) != 0) {
            // enumで生成されるstaticメソッド2つをコンパイラ生成として扱う
            if (methodSignature.isSame(new MethodSignature("values"))) {
                return MethodDerivation.COMPILER_GENERATED;
            } else {
                if (methodSignature.isSame(new MethodSignature("valueOf", TypeIdentifier.from(String.class)))) {
                    return MethodDerivation.COMPILER_GENERATED;
                }
            }
        }

        return MethodDerivation.PROGRAMMER;
    }

    public static JigMethodBuilder builder(int access,
                                           Visibility visibility,
                                           List<TypeIdentifier> signatureContainedTypes,
                                           List<TypeIdentifier> throwsTypes,
                                           MethodDeclaration methodDeclaration,
                                           List<Annotation> annotationList,
                                           Instructions methodInstructions, boolean isEnum, boolean isRecordComponent) {
        MethodDerivation methodDerivation = resolveMethodDerivation(methodDeclaration, access, isEnum, isRecordComponent);
        var jigMethodBuilder = new JigMethodBuilder(methodDeclaration, signatureContainedTypes, visibility, methodDerivation, throwsTypes, annotationList, methodInstructions);
        return jigMethodBuilder;
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
