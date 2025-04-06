package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * メソッドのヘッダ
 */
public record JigMethodHeader(JigMethodIdentifier id,
                              JigMemberOwnership ownership,
                              JigMethodAttribute jigMethodAttribute) {
    public static JigMethodHeader from(JigMethodIdentifier id,
                                       JigMemberOwnership ownership,
                                       JigMemberVisibility jigMemberVisibility,
                                       Collection<JigAnnotationReference> declarationAnnotations,
                                       JigTypeReference returnType,
                                       List<JigTypeReference> argumentList,
                                       Collection<JigTypeReference> throwTypes,
                                       EnumSet<JigMethodFlag> flags) {
        return new JigMethodHeader(id, ownership,
                new JigMethodAttribute(jigMemberVisibility, declarationAnnotations, returnType, argumentList, throwTypes, flags));
    }

    public String name() {
        return id.name();
    }

    public String nameArgumentsReturnSimpleText() {
        return nameAndArgumentSimpleText() + ':' + jigMethodAttribute.returnType().simpleNameWithGenerics();
    }

    public String nameAndArgumentSimpleText() {
        return "%s(%s)".formatted(
                id().name(),
                jigMethodAttribute.argumentList().stream()
                        .map(JigTypeReference::simpleNameWithGenerics)
                        .collect(Collectors.joining(", ")));
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

    public Stream<TypeIdentifier> associatedTypeStream() {
        return jigMethodAttribute.associatedTypeStream();
    }

    /**
     * lambda合成メソッドの判定
     * バイトコード上のフラグはACC_PRIVATE, ACC_STATIC, ACC_SYNTHETICを持つ。
     * TODO LAMBDA_SUPPORTだけで判定させられるはず（いまはLAMBDA_SUPPORTの条件が名前だけなので不足だが）
     */
    public boolean isLambdaSyntheticMethod() {
        return jigMemberVisibility() == JigMemberVisibility.PRIVATE
                && ownership() == JigMemberOwnership.CLASS
                && jigMethodAttribute().flags().contains(JigMethodFlag.SYNTHETIC)
                && jigMethodAttribute().flags().contains(JigMethodFlag.LAMBDA_SUPPORT);
    }

    public List<JigTypeReference> argumentList() {
        return jigMethodAttribute().argumentList();
    }

    public Stream<JigAnnotationReference> declarationAnnotationStream() {
        return jigMethodAttribute().declarationAnnotations().stream();
    }

    public JigTypeReference returnType() {
        return jigMethodAttribute().returnType();
    }

    public boolean isRecordComponentAccessor() {
        return jigMethodAttribute().flags().contains(JigMethodFlag.RECORD_COMPONENT_ACCESSOR);
    }

    public boolean isProgrammerDefined() {
        EnumSet<JigMethodFlag> flags = jigMethodAttribute().flags();
        return flags.stream().noneMatch(JigMethodFlag::compilerGenerated);
    }

    public boolean isStaticOrInstanceInitializer() {
        return jigMethodAttribute().flags().contains(JigMethodFlag.INITIALIZER) ||
                jigMethodAttribute().flags().contains(JigMethodFlag.STATIC_INITIALIZER);
    }
}
