package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

public class GlossaryAdapter {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public GlossaryAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.Glossary)
    public List<Path> invoke(JigRepository jigRepository, JigDocument jigDocument) {
        var glossary = jigService.glossary(jigRepository);
        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        var domainPackageRoots = jigService.coreDomainJigTypes(jigRepository).domainPackageRoots();

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("glossaryData", buildJson(glossary, domainPackageRoots));

        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(Glossary glossary, List<String> domainPackageRoots) {
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
