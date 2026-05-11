package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.sources.javasources.TypeSourcePaths;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 用語集（glossary-data.js）
 * ほぼ全てのドキュメントで使用
 */
public class GlossaryDataAdapter implements DataAdapter {

    private final JigService jigService;

    public GlossaryDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "glossaryData";
    }

    @Override
    public String dataFileName() {
        return "glossary-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        var glossary = jigService.glossary(jigRepository);
        var domainPackageRoots = jigService.coreDomainJigTypes(jigRepository).domainPackageRoots();
        Function<Term, Optional<String>> sourcePathResolver =
                sourcePathResolver(jigRepository.typeSourcePaths(), jigRepository.repositoryRoot());
        return buildGlossaryJson(glossary, domainPackageRoots, sourcePathResolver);
    }

    public static String buildGlossaryJson(Glossary glossary, List<String> domainPackageRoots) {
        return buildGlossaryJson(glossary, domainPackageRoots, term -> Optional.empty());
    }

    public static String buildGlossaryJson(Glossary glossary, List<String> domainPackageRoots,
                                           Function<Term, Optional<String>> sourcePathResolver) {
        var map = new LinkedHashMap<String, String>();
        for (var term : glossary.list()) {
            var builder = Json.object("title", term.title())
                    .and("simpleText", term.simpleText())
                    .and("kind", term.termKind().name())
                    .and("description", term.description())
                    .and("origin", term.origin().name());
            sourcePathResolver.apply(term).ifPresent(path -> builder.and("sourcePath", path));
            map.put(term.id().asText(), builder.build());
        }
        return Json.object("terms", Json.object(map))
                .and("domainPackageRoots", Json.array(domainPackageRoots))
                .build();
    }

    private static Function<Term, Optional<String>> sourcePathResolver(TypeSourcePaths typeSourcePaths, Optional<Path> repositoryRoot) {
        if (repositoryRoot.isEmpty() || typeSourcePaths.map().isEmpty()) {
            return term -> Optional.empty();
        }
        Path root = repositoryRoot.get();
        return term -> typeIdFor(term).flatMap(typeSourcePaths::find)
                .map(root::relativize)
                .map(Path::toString)
                .map(s -> s.replace('\\', '/'));
    }

    private static Optional<TypeId> typeIdFor(Term term) {
        String value = term.id().asText();
        int hash = value.indexOf('#');
        String typeFqn = (hash < 0) ? value : value.substring(0, hash);
        if (typeFqn.isEmpty()) return Optional.empty();
        return Optional.of(TypeId.valueOf(typeFqn));
    }
}
