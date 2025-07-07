package org.dddjava.jig.domain.model.data.members.fields;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Stream;

/**
 * フィールドのヘッダ
 *
 * フィールドには本体がないためヘッダだけで完全な定義になるが、
 * JigTypeHeaderやJigMethodHeaderに合わせてこの名前にしておく。
 */
public record JigFieldHeader(JigFieldId id,
                             JigMemberOwnership ownership,
                             JigTypeReference jigTypeReference,
                             JigFieldAttribute jigFieldAttribute) {
    public static JigFieldHeader from(JigFieldId id, JigMemberOwnership jigMemberOwnership, JigTypeReference jigTypeReference,
                                      JigMemberVisibility jigMemberVisibility, Collection<JigAnnotationReference> declarationAnnotations, EnumSet<JigFieldFlag> flags) {
        return new JigFieldHeader(id, jigMemberOwnership, jigTypeReference,
                new JigFieldAttribute(jigMemberVisibility, declarationAnnotations, flags));
    }

    public String simpleText() {
        return jigTypeReference.simpleName() + ' ' + id.name();
    }

    public Stream<TypeIdentifier> allTypeIdentifierStream() {
        return Stream.concat(jigTypeReference.allTypeIentifierStream(), jigFieldAttribute().allTypeIdentifierStream());
    }

    public String simpleNameWithGenerics() {
        return jigTypeReference.simpleNameWithGenerics() + ' ' + id.name();
    }

    public String name() {
        return id.name();
    }

    public boolean isDeprecated() {
        return jigFieldAttribute.isDeprecated();
    }

    public boolean isEnumConstant() {
        return jigFieldAttribute.flags().contains(JigFieldFlag.ENUM);
    }

    public Stream<JigAnnotationReference> declarationAnnotationStream() {
        return jigFieldAttribute.declarationAnnotations().stream();
    }
}
