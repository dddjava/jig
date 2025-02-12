package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * メソッドの識別子
 */
public record MethodIdentifier(TypeIdentifier declaringType, MethodSignature methodSignature) {

}
