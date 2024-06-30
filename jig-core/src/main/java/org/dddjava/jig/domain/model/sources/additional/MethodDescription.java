package org.dddjava.jig.domain.model.sources.additional;

import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;

/**
 * メソッドに対する説明
 */
public record MethodDescription(MethodIdentifier methodIdentifier, String descriptionSource) {
}
