package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.application.JigDataProvider;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;

public interface JigRepository {

    static JigRepository empty() {
        return new JigRepository() {
            @Override
            public JigTypes fetchJigTypes() {
                return new JigTypes(List.of());
            }

            @Override
            public JigDataProvider jigDataProvider() {
                return JigDataProvider.none();
            }

            @Override
            public Glossary fetchGlossary() {
                return new Glossary(List.of());
            }
        };
    }

    JigTypes fetchJigTypes();

    /**
     * JigTypesを通さずにdataにアクセスが必要な時に呼び出すブリッジ
     * 使用箇所が増える場合は見直した方がいいかもしれない。
     */
    JigDataProvider jigDataProvider();

    Glossary fetchGlossary();
}
