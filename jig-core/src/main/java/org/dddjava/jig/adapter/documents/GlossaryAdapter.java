package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.JigDocumentAdapter;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

public class GlossaryAdapter implements JigDocumentAdapter {

    private final JigService jigService;
    private final Path outputDirectory;

    public GlossaryAdapter(JigService jigService, Path outputDirectory) {
        this.jigService = jigService;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public JigDocument supportedDocument() {
        return JigDocument.Glossary;
    }

    @Override
    public List<Path> write(JigDocument jigDocument, JigRepository jigRepository) {
        var glossary = jigService.glossary(jigRepository);
        var domainPackageRoots = jigService.coreDomainJigTypes(jigRepository).domainPackageRoots();

        return List.of(JigDocumentWriter.writeData(outputDirectory, jigDocument, "glossaryData", buildJson(glossary, domainPackageRoots)));
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
