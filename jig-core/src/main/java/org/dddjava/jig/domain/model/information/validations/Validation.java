package org.dddjava.jig.domain.model.information.validations;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * バリデーション
 */
public record Validation(TypeIdentifier typeIdentifier, String memberName, TypeIdentifier memberType,
                         TypeIdentifier annotationType, String annotationDescription) {
}
