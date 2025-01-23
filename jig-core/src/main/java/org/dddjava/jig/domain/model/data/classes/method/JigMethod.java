package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * メソッド
 */
public class JigMethod {

    MethodDeclaration methodDeclaration;

    MethodAnnotations methodAnnotations;
    Visibility visibility;

    MethodDerivation methodDerivation;
    MethodImplementation methodImplementation;
    private final Instructions instructions;
    private final List<TypeIdentifier> throwsTypes;
    private final List<TypeIdentifier> signatureContainedTypes;

    public JigMethod(MethodDeclaration methodDeclaration, MethodAnnotations methodAnnotations, Visibility visibility, MethodDerivation methodDerivation, Instructions instructions, List<TypeIdentifier> throwsTypes, List<TypeIdentifier> signatureContainedTypes, MethodImplementation methodImplementation) {
        this.methodDeclaration = methodDeclaration;
        this.methodAnnotations = methodAnnotations;
        this.visibility = visibility;
        this.methodDerivation = methodDerivation;
        this.methodImplementation = methodImplementation;
        this.instructions = instructions;
        this.throwsTypes = throwsTypes;
        this.signatureContainedTypes = signatureContainedTypes;
    }

    public MethodDeclaration declaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return instructions.decisionNumber();
    }

    public MethodAnnotations methodAnnotations() {
        return methodAnnotations;
    }

    public Visibility visibility() {
        return visibility;
    }

    public boolean isPublic() {
        return visibility.isPublic();
    }

    public UsingFields usingFields() {
        return new UsingFields(instructions.fieldReferences());
    }

    public UsingMethods usingMethods() {
        return new UsingMethods(instructions.instructMethods());
    }

    public boolean conditionalNull() {
        return instructions.hasNullDecision();
    }

    public boolean referenceNull() {
        return instructions.hasNullReference();
    }

    public boolean useNull() {
        return referenceNull() || conditionalNull();
    }

    public boolean notUseMember() {
        return instructions.hasMemberInstruction();
    }

    public TypeIdentifiers usingTypes() {
        var list = Stream.of(
                        instructions.usingTypes(),
                        methodDeclaration.relateTypes(),
                        methodAnnotations.list().stream().map(MethodAnnotation::annotationType).toList(),
                        throwsTypes,
                        signatureContainedTypes)
                .flatMap(Collection::stream)
                .toList();
        return new TypeIdentifiers(list);
    }

    public String aliasTextOrBlank() {
        return methodImplementation.comment().summaryText();
    }

    public String aliasText() {
        return methodImplementation.comment()
                .asTextOrDefault(declaration().declaringType().asSimpleText() + "\\n"
                        + declaration().methodSignature().methodName());
    }

    public JigMethodDescription description() {
        return JigMethodDescription.from(methodImplementation.comment());
    }

    /**
     * 出力時に使用する名称
     */
    public String labelTextWithSymbol() {
        return visibility.symbol() + ' ' + labelText();
    }

    public String labelText() {
        return methodImplementation.comment().asTextOrDefault(declaration().methodSignature().methodName());
    }

    public String fqn() {
        return declaration().identifier().asText();
    }

    public String htmlIdText() {
        return declaration().htmlIdText();
    }

    public String labelTextOrLambda() {
        if (declaration().isLambda()) {
            return "lambda";
        }
        return labelText();
    }

    public List<ParameterizedType> argumentTypes() {
        return declaration().methodSignature().arguments();
    }

    public MethodDerivation derivation() {
        return methodDerivation;
    }

    public boolean objectMethod() {
        return declaration().methodSignature().isObjectMethod();
    }

    public boolean documented() {
        return methodImplementation.comment().exists();
    }

    /**
     * 注目に値するかの判定
     *
     * publicもしくはドキュメントコメントが記述されているものを「注目に値する」と識別する。
     * privateでもドキュメントコメントが書かれているものは注目する。
     */
    public boolean remarkable() {
        return visibility == Visibility.PUBLIC || documented();
    }

    public List<MethodDeclaration> methodInstructions() {
        return instructions.instructMethods().list();
    }

    public String name() {
        return declaration().methodSignature().methodName();
    }
}
