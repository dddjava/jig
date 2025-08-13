package org.dddjava.jig.domain.model.information.core;


import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.function.Predicate;

/**
 * コアドメインの判定条件
 */
public interface CoreDomainCondition extends Predicate<JigType> {

    boolean isCoreDomain(JigType jigType);

    default boolean test(JigType jigType) {
        return isCoreDomain(jigType);
    }
}
