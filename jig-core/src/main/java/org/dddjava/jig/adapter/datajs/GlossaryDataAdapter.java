package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.sources.javasources.TypeSourcePaths;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
        var sourcePaths = sourcePaths(jigRepository.typeSourcePaths(), jigRepository.repositoryRoot());
        return buildGlossaryJson(glossary, domainPackageRoots, sourcePaths);
    }

    public static String buildGlossaryJson(Glossary glossary, List<String> domainPackageRoots) {
        return buildGlossaryJson(glossary, domainPackageRoots, Map.of());
    }

    public static String buildGlossaryJson(Glossary glossary, List<String> domainPackageRoots,
                                           Map<String, String> sourcePaths) {
        var map = new LinkedHashMap<String, String>();
        for (var term : glossary.list()) {
            var builder = Json.object("title", term.title())
                    .and("simpleText", term.simpleText())
                    .and("kind", term.termKind().name())
                    .and("description", term.description())
                    .and("origin", term.origin().name());
            map.put(term.id().asText(), builder.build());
        }
        var sourcePathsBuilder = Json.object();
        sourcePaths.forEach(sourcePathsBuilder::and);
        return Json.object("terms", Json.object(map))
                .and("sourcePaths", sourcePathsBuilder)
                .and("domainPackageRoots", Json.array(domainPackageRoots))
                .build();
    }

    /**
     * 型・パッケージのFQNからリポジトリルート相対のソースパスへのマップを作る。
     * 用語（Javadoc）の有無に関わらず、ソースを解析した全ての型・パッケージを対象とする。
     */
    private static Map<String, String> sourcePaths(TypeSourcePaths typeSourcePaths, Optional<Path> repositoryRoot) {
        if (repositoryRoot.isEmpty()) {
            return Map.of();
        }
        Path root = repositoryRoot.get();
        var map = new TreeMap<String, String>();
        typeSourcePaths.typeMap().forEach((typeId, path) ->
                map.put(typeId.fqn(), root.relativize(path).toString().replace('\\', '/')));
        typeSourcePaths.packageMap().forEach((packageId, path) ->
                map.put(packageId.asText(), root.relativize(path).toString().replace('\\', '/')));
        return map;
    }
}
