package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.adapter.JigDocumentGenerator;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.core.CoreDomainCondition;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;

// Configurationという名前だけど実態は設定済みの実行コンテキストを管理している（SpringのApplicationContextみたいな感じになっている）
public record Configuration(
        GlossaryRepository glossaryRepository,
        JigEventRepository jigEventRepository,
        JigSettings settings,
        JigDocumentGenerator jigDocumentGenerator,
        JigService jigService
) {

    public static Configuration from(JigSettings settings) {
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();
        JigEventRepository jigEventRepository = new JigEventRepository(settings.locale());

        CoreDomainCondition architecture = new CoreDomainCondition(settings.domainPattern());
        JigService jigService = new JigService(architecture, jigEventRepository);

        JigDocumentGenerator jigDocumentGenerator = new JigDocumentGenerator(settings, jigService);

        return new Configuration(
                glossaryRepository,
                jigEventRepository,
                settings,
                jigDocumentGenerator,
                jigService
        );
    }

    /**
     * 同じ設定で独立した実行コンテキストを生成する。
     *
     * 用語集、イベント、各サービスは実行ごとの状態を持つため、Configurationを再利用しても共有しない。
     */
    public Configuration newExecution() {
        return from(settings);
    }
}
