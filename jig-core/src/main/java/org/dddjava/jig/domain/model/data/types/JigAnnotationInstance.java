package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;

/**
 * 宣言アノテーションや型アノテーションとして記述されたアノテーションのインスタンス。
 *
 * @param id               アノテーションの型を示すID
 * @param elementValueData
 */
public record JigAnnotationInstance(TypeIdentifier id,
                                    Collection<JigAnnotationInstanceElement> elementValueData) {

    public static JigAnnotationInstance from(TypeIdentifier typeIdentifier) {
        return new JigAnnotationInstance(typeIdentifier, List.of());
    }

    public String simpleTypeName() {
        return id.simpleValue();
    }
}
