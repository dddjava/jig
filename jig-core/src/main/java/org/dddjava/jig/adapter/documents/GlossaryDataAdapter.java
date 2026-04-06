package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.JigDocumentAdapter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.information.JigRepository;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 用語集（glossary-data.js）
 * ほぼ全てのドキュメントで使用
 */
public class GlossaryDataAdapter implements JigDocumentAdapter {

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
        return buildGlossaryJson(glossary, domainPackageRoots);
    }

    public static String buildGlossaryJson(Glossary glossary, List<String> domainPackageRoots) {
        var map = new LinkedHashMap<String, String>();
        for (var term : glossary.list()) {
            var value = Json.object("title", term.title())
                    .and("simpleText", term.simpleText())
                    .and("kind", term.termKind().name())
                    .and("description", term.description())
                    .build();
            map.put(term.id().asText(), value);
        }
        return Json.object("terms", Json.object(map))
                .and("domainPackageRoots", Json.array(domainPackageRoots))
                .build();
    }
}
