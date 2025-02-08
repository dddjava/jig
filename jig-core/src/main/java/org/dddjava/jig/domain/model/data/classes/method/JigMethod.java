package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * メソッド
 */
public class JigMethod {

    MethodDeclaration methodDeclaration;

    MethodAnnotations methodAnnotations;
    JigMemberVisibility jigMemberVisibility;

    MethodDerivation methodDerivation;
    private final Instructions instructions;
    private final List<TypeIdentifier> throwsTypes;
    private final List<TypeIdentifier> signatureContainedTypes;
    private final Term term;

    public JigMethod(MethodDeclaration methodDeclaration, MethodAnnotations methodAnnotations, JigMemberVisibility jigMemberVisibility, MethodDerivation methodDerivation, Instructions instructions, List<TypeIdentifier> throwsTypes, List<TypeIdentifier> signatureContainedTypes, Term term) {
        this.methodDeclaration = methodDeclaration;
        this.methodAnnotations = methodAnnotations;
        this.jigMemberVisibility = jigMemberVisibility;
        this.methodDerivation = methodDerivation;
        this.instructions = instructions;
        this.throwsTypes = throwsTypes;
        this.signatureContainedTypes = signatureContainedTypes;
        this.term = term;
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

    public JigMemberVisibility visibility() {
        return jigMemberVisibility;
    }

    public boolean isPublic() {
        return jigMemberVisibility.isPublic();
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
        var title = term.title();
        return declaration().methodSignature().methodName().equals(title) ? "" : title;
    }

    public String aliasText() {
        if (aliasTextOrBlank().isEmpty()) {
            return declaration().declaringType().asSimpleText() + "\\n" + declaration().methodSignature().methodName();
        }
        return aliasTextOrBlank();
    }

    public Term term() {
        return term;
    }

    /**
     * 出力時に使用する名称
     */
    public String labelTextWithSymbol() {
        return jigMemberVisibility.symbol() + ' ' + labelText();
    }

    public String labelText() {
        return term.title();
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
        return !aliasTextOrBlank().isEmpty();
    }

    /**
     * 注目に値するかの判定
     *
     * publicもしくはドキュメントコメントが記述されているものを「注目に値する」と識別する。
     * privateでもドキュメントコメントが書かれているものは注目する。
     */
    public boolean remarkable() {
        return jigMemberVisibility == JigMemberVisibility.PUBLIC || documented();
    }

    public List<MethodDeclaration> methodInstructions() {
        return instructions.instructMethods().list();
    }

    public String name() {
        return declaration().methodSignature().methodName();
    }

    public boolean hasAnnotation(TypeIdentifier annotation) {
        return methodAnnotations().contains(annotation);
    }

    public Instructions instructions() {
        return instructions;
    }
}
