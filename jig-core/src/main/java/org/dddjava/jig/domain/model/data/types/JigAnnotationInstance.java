package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;

/**
 * 宣言アノテーションや型アノテーションとして記述されたアノテーションのインスタンス。
 * @param id アノテーションの型を示すID。FQCN。
 * @param elementValueData
 */
public record JigAnnotationInstance(JigObjectId<JigTypeHeader> id,
                                    Collection<JigAnnotationInstanceElement> elementValueData) {

    public static JigAnnotationInstance from(String name) {
        return new JigAnnotationInstance(new JigObjectId<>(name), List.of());
    }

    public String simpleTypeName() {
        return id.simpleValue();
    }
}
