package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigDocumentGenerator;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.Architecture;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;

import java.util.regex.Pattern;

// Configurationという名前だけど実態は設定されたインスタンスを管理している（SpringのApplicationContextみたいな感じになっている）
public record Configuration(
        GlossaryRepository glossaryRepository,
        JigEventRepository jigEventRepository,
        JigProperties properties,
        JigDocumentGenerator jigDocumentGenerator,
        JigService jigService,
        JigDocumentContext jigDocumentContext
) {

    // 2025.6.1リリース後に削除。MavenPluginなどで使用。　
    @Deprecated(since = "2025.6.1", forRemoval = true)
    public Configuration(JigProperties jigProperties) {
        this(from(jigProperties));
    }

    // Configuration(JigProperties)と併せて廃止する
    private Configuration(Configuration config) {
        this(
                config.glossaryRepository,
                config.jigEventRepository,
                config.properties,
                config.jigDocumentGenerator,
                config.jigService,
                config.jigDocumentContext
        );
    }

    public static Configuration from(JigProperties jigProperties) {
        JigProperties properties = new JigPropertyLoader(jigProperties).load();
        GlossaryRepository glossaryRepository = new OnMemoryGlossaryRepository();
        JigEventRepository jigEventRepository = new JigEventRepository();

        Pattern compilerGeneratedClassPattern = Pattern.compile(".+\\$\\d+");
        Pattern businessRulePattern = Pattern.compile(properties.getDomainPattern());

        Architecture architecture = jigType -> {
            String fqn = jigType.id().fullQualifiedName();
            if (fqn.endsWith(".package-info")) return false;
            return businessRulePattern.matcher(fqn).matches()
                    && !compilerGeneratedClassPattern.matcher(fqn).matches();
        };
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
