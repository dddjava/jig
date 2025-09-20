package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.*;

import java.util.Collection;
import java.util.List;

public record JigTypeHeaderBuilder(TypeId id,
                                   JavaTypeDeclarationKind javaTypeDeclarationKind,
                                   JigBaseTypeDataBundle baseTypeDataBundle,
                                   // JigTypeAttributesからアノテーションを除いたもの
                                   JigTypeVisibility jigTypeVisibility,
                                   Collection<JigTypeModifier> jigTypeModifiers,
                                   List<JigTypeParameter> typeParameters
) {
}
