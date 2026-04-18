package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodParameter;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.data.types.TypeIds;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

/**
 * メソッド
 */
public record JigMethod(JigMethodDeclaration jigMethodDeclaration) {

    public JigMethodId jigMethodId() {
        return header().id();
    }

    public String simpleText() {
        return jigMethodId().simpleText();
    }

    public String fqn() {
        return jigMethodId().fqn();
    }

    public Stream<JigAnnotationReference> declarationAnnotationStream() {
        return header().declarationAnnotationStream();
    }

    public JigMemberVisibility visibility() {
        return header().jigMemberVisibility();
    }

    public boolean isPublic() {
        return visibility().isPublic();
    }

    public UsingFields usingFields() {
        return UsingFields.from(instructions());
    }

    public UsingMethods usingMethods() {
        return UsingMethods.from(instructions());
    }

    public TypeIds usingTypes() {
        return new TypeIds(jigMethodDeclaration.associatedTypes()
                .stream()
                .filter(not(TypeId::isJavaStandardLanguageType))
                .collect(Collectors.toSet()));
    }

    public boolean isObjectMethod() {
        return header().isObjectMethod();
    }

    public String name() {
        return header().name();
    }

    public boolean hasAnnotation(TypeId annotation) {
        return declarationAnnotationStream().anyMatch(it -> it.id().equals(annotation));
    }

    public Instructions instructions() {
        return jigMethodDeclaration.instructions();
    }

    public boolean isAbstract() {
        return header().isAbstract();
    }

    public String simpleMethodDeclarationText() {
        return header().simpleMethodDeclarationText();
    }

    public boolean isCall(JigMethodId jigMethodId) {
        return usingMethods().contains(jigMethodId);
    }

    public JigTypeReference returnType() {
        return header().returnType();
    }

    public String simpleMethodSignatureText() {
        return header().simpleMethodSignatureText();
    }

    public List<JigMethodParameter> parameterList() {
        return header().parameterList();
    }

    public Stream<JigTypeReference> parameterTypeStream() {
        return header().parameterTypeStream();
    }

    public Stream<JigTypeReference> throwTypeStream() {
        return header().throwTypeStream();
    }

    public TypeId declaringType() {
        return header().id().tuple().declaringTypeId();
    }

    public boolean isProgrammerDefined() {
        return header().isProgrammerDefined();
    }

    public boolean isRecordComponent() {
        return header().isRecordComponentAccessor();
    }

    /**
     * メソッド定義のヘッダにアクセスするヘルパーメソッド
     *
     * JigMethodに対する関心のほとんどはヘッダに由来する。
     * すべての箇所でチェーンすると冗長なコードになるので、それを改善する。
     */
    private JigMethodHeader header() {
        return jigMethodDeclaration.header();
    }

    public Stream<MethodCall> lambdaInlinedMethodCallStream() {
        return instructions().lambdaInlinedMethodCallStream();
    }

    public boolean isDeprecated() {
        return header().isDeprecated();
    }
}
