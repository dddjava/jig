package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;

public interface JigTypesRepository {

    static JigTypesRepository empty() {
        return new JigTypesRepository() {
            @Override
            public JigTypes fetchJigTypes() {
                return new JigTypes(List.of());
            }

            @Override
            public JigDataProvider jigDataProvider() {
                return JigDataProvider.none();
            }
        };
    }

    JigTypes fetchJigTypes();

    /**
     * JigTypesを通さずにdataにアクセスが必要な時に呼び出すブリッジ
     * 使用箇所が増える場合は見直した方がいいかもしれない。
     */
    JigDataProvider jigDataProvider();
}
