package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.information.types.JigTypes;

public interface JigTypesRepository {

    JigTypes fetchJigTypes();

    /**
     * JigTypesを通さずにdataにアクセスが必要な時に呼び出すブリッジ
     * 使用箇所が増える場合は見直した方がいいかもしれない。
     */
    JigDataProvider jigDataProvider();
}
