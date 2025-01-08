package org.dddjava.jig.domain.model.sources.additional;

import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;

/**
 * メソッドに対する別名
 */
public record MethodAlias(MethodIdentifier methodIdentifier, String names) {
}
