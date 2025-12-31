package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * メソッドのヘッダ
 */
public record JigMethodHeader(JigMethodId id,
                              JigMemberOwnership ownership,
                              JigMethodAttribute jigMethodAttribute) {
    public static JigMethodHeader from(JigMethodId id,
                                       JigMemberOwnership ownership,
                                       JigMemberVisibility jigMemberVisibility,
                                       Collection<JigAnnotationReference> declarationAnnotations,
                                       JigTypeReference returnType,
                                       List<JigTypeReference> parameterTypeList,
                                       Collection<JigTypeReference> throwTypes,
                                       EnumSet<JigMethodFlag> flags) {
        return new JigMethodHeader(id, ownership,
                new JigMethodAttribute(jigMemberVisibility, declarationAnnotations, returnType, parameterTypeList, throwTypes, flags));
    }

    public String name() {
        return id.name();
    }

    public String nameArgumentsReturnSimpleText() {
        return nameAndArgumentSimpleText() + ':' + jigMethodAttribute.returnType().simpleNameWithGenerics();
    }

    public String nameAndArgumentSimpleText() {
        return "%s(%s)".formatted(
                id.name(),
                jigMethodAttribute.parameterTypeList().stream()
                        .map(JigTypeReference::simpleNameWithGenerics)
                        .collect(joining(", ")));
    }

    public boolean isObjectMethod() {
        // とりあえずIDで比較するけど、attributeのFlagで判別できるようにしてていい気はする
        return id.value().endsWith("#equals(java.lang.Object)")
                || id.value().endsWith("#hashCode()")
                || id.value().endsWith("#toString()");
    }

    public JigMemberVisibility jigMemberVisibility() {
        return jigMethodAttribute.jigMemberVisibility();
    }

    public boolean isAbstract() {
        return jigMethodAttribute.flags().contains(JigMethodFlag.ABSTRACT);
    }

    public Stream<TypeId> associatedTypeStream() {
        return jigMethodAttribute.associatedTypeStream();
    }

    public boolean isLambdaSyntheticMethod() {
        return jigMethodAttribute.flags().contains(JigMethodFlag.LAMBDA_SUPPORT);
    }

    public List<JigTypeReference> argumentList() {
        return jigMethodAttribute.parameterTypeList();
    }

    public Stream<JigAnnotationReference> declarationAnnotationStream() {
        return jigMethodAttribute.declarationAnnotations().stream();
    }

    public JigTypeReference returnType() {
        return jigMethodAttribute.returnType();
    }

    public boolean isRecordComponentAccessor() {
        return jigMethodAttribute.flags().contains(JigMethodFlag.RECORD_COMPONENT_ACCESSOR);
    }

    public boolean isProgrammerDefined() {
        EnumSet<JigMethodFlag> flags = jigMethodAttribute.flags();
        return flags.stream().noneMatch(JigMethodFlag::compilerGenerated);
    }

    public boolean isStaticOrInstanceInitializer() {
        return jigMethodAttribute.flags().contains(JigMethodFlag.INITIALIZER) ||
                jigMethodAttribute.flags().contains(JigMethodFlag.STATIC_INITIALIZER);
    }
}
