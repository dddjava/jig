package org.dddjava.jig.domain.model.knowledge.validations;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * バリデーション
 */
public record Validation(TypeId typeId, String memberName, TypeId memberType,
                         TypeId annotationType, String annotationDescription) {
}
