package org.dddjava.jig.domain.model.sources.additional;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

/**
 * 型に対する別名
 */
public record TypeAlias(TypeIdentifier typeIdentifier, String names) {
}
