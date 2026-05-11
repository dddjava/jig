package org.dddjava.jig.application;

import org.dddjava.jig.JigResult;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.information.outbound.ExternalAccessorRepositories;
import org.dddjava.jig.domain.model.information.outbound.other.OtherExternalAccessorRepository;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.sources.javasources.TypeSourcePaths;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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

            @Override
            public JigResult.JigSummary summary() {
                return JigResult.JigSummary.empty();
            }

            @Override
            public ExternalAccessorRepositories externalAccessorRepositories() {
                return new ExternalAccessorRepositories(PersistenceAccessorRepository.empty(), OtherExternalAccessorRepository.empty());
            }

            @Override
            public TypeSourcePaths typeSourcePaths() {
                return TypeSourcePaths.empty();
            }

            @Override
            public Optional<Path> repositoryRoot() {
                return Optional.empty();
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

    JigResult.JigSummary summary();

    ExternalAccessorRepositories externalAccessorRepositories();

    TypeSourcePaths typeSourcePaths();

    /**
     * 解析対象プロジェクトのリポジトリルート（.gitの親ディレクトリ）。
     * git管理外の場合はempty。
     */
    Optional<Path> repositoryRoot();
}
