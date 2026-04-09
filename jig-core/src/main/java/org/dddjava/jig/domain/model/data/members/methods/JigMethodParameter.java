package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.data.types.JigTypeReference;

/**
 * メソッドの仮引数
 */
public record JigMethodParameter(String name, ParameterNameSource nameSource, JigTypeReference typeReference) {
}
