package org.dddjava.jig.domain.model.models.architectures;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;

/**
 * アーキテクチャ
 */
public interface Architecture {

    boolean isBusinessRule(JigType jigType);
}
