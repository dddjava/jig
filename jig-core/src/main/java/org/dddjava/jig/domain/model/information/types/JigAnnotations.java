package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * JIGが提供するアノテーション識別子定数
 */
public class JigAnnotations {
    private JigAnnotations() {}

    public static final TypeId SERVICE = TypeId.valueOf("org.dddjava.jig.annotation.Service");
    public static final TypeId REPOSITORY = TypeId.valueOf("org.dddjava.jig.annotation.Repository");
}
