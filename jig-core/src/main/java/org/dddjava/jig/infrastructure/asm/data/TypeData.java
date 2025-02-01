package org.dddjava.jig.infrastructure.asm.data;

import org.dddjava.jig.domain.model.data.classes.type.TypeVisibility;

import java.util.Collection;
import java.util.Optional;

class JigId<T> {
}

/**
 *
 * JSL`NormalClassDeclaration` の `ClassBody` 以外で得られる情報
 *
 * @param id 完全修飾クラス名
 * @param typeKind
 * @param attributeData
 * @param superType 親クラスの完全修飾クラス名。classの場合は未指定でもObjectが入るが、interfaceなどではempty。
 * @param interfaceTypes 実装インタフェースの完全修飾クラス名
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html">jls/Chapter 8. Classes</a>
 */
public record TypeData(JigId<TypeData> id,
                       TypeKind typeKind,
                       AttributeData attributeData,
                       Optional<JigId<TypeData>> superType,
                       Collection<JigId<TypeData>> interfaceTypes) {

    enum TypeKind {
        CLASS,
        ABSTRACT_CLASS,
        INTERFACE,
        ANNOTATION,
        ENUM,
        RECORD
    }

    record AttributeData(TypeVisibility typeVisibility, Collection<AnnotationData> declarationAnnotations) {
    }

    record AnnotationData(JigId<TypeData> id, Collection<ElementValueData> elementValueData) {
    }

    record ElementValueData(String name, Object value) {
    }
}
