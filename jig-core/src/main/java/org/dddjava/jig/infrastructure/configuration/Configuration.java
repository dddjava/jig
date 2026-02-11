package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.adapter.JigDocumentGenerator;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.core.CoreDomainCondition;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;
import org.dddjava.jig.infrastructure.configuration.JigProperty;

// Configurationという名前だけど実態は設定されたインスタンスを管理している（SpringのApplicationContextみたいな感じになっている）
public record Configuration(
        GlossaryRepository glossaryRepository,
        JigEventRepository jigEventRepository,
        JigProperties properties,
        JigDocumentGenerator jigDocumentGenerator,
        JigService jigService,
        JigDocumentContext jigDocumentContext
) {

    public static Configuration from(JigProperties jigProperties) {
        JigProperties properties = new JigPropertyLoader(jigProperties).load();
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();
        JigEventRepository jigEventRepository = new JigEventRepository();

        CoreDomainCondition architecture;
        if (properties.getDomainPattern().equals(JigProperty.PATTERN_DOMAIN.defaultValue())) {
            architecture = CoreDomainCondition.defaultCondition();
        } else {
            architecture = new CoreDomainCondition(properties.getDomainPattern());
        }
        JigService jigService = new JigService(architecture, jigEventRepository);

        JigDocumentContext jigDocumentContext = new JigDocumentContextImpl(glossaryRepository, properties);
        JigDocumentGenerator jigDocumentGenerator = new JigDocumentGenerator(jigDocumentContext, jigService);

        return new Configuration(
                glossaryRepository,
                jigEventRepository,
                properties,
                jigDocumentGenerator,
                jigService,
                jigDocumentContext
        );
    }
}
