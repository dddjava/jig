package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.JigDocumentAdapter;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inbound.Entrypoint;
import org.dddjava.jig.domain.model.information.inbound.InputAdapters;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 入力インタフェース
 */
public class InboundInterfaceAdapter implements JigDocumentAdapter {

    private final JigService jigService;
    private final Path outputDirectory;

    public InboundInterfaceAdapter(JigService jigService, Path outputDirectory) {
        this.jigService = jigService;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public JigDocument supportedDocument() {
        return JigDocument.InboundInterface;
    }

    @Override
    public List<Path> write(JigDocument jigDocument, JigRepository jigRepository) {
        var contextJigTypes = jigService.jigTypes(jigRepository);
        var inputAdapters = jigService.inputAdapters(jigRepository);

        var json = buildJson(inputAdapters, contextJigTypes);

        return List.of(JigDocumentWriter.writeData(outputDirectory, jigDocument, "inboundData", json));
    }

    public static String buildJson(InputAdapters inputAdapters, JigTypes jigTypes) {
        List<JsonObjectBuilder> controllerList = new ArrayList<>();

        MethodRelations springComponentMethodRelations = inputAdapters.methodRelations().filterApplicationComponent(jigTypes).inlineLambda();

        inputAdapters.groups().forEach(inputAdapter -> {
            var jigType = inputAdapter.jigType();

            List<JsonObjectBuilder> edges = new ArrayList<>();
            List<JsonObjectBuilder> entrypointList = new ArrayList<>();

            inputAdapter.entrypoints().forEach(entrypoint -> {
                var entrypointMethodId = entrypoint.jigMethod().jigMethodId();

                MethodRelations declaredMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypointMethodId, jigTypes::isService);
                declaredMethodRelations.list().forEach(relation -> {
                    edges.add(Json.object("from", relation.from().fqn())
                            .and("to", relation.to().fqn()));
                });

                entrypointList.add(JsonSupport.buildMethodJson(entrypoint.jigMethod())
                        .and("path", entrypoint.pathText()));
            });

            var classPath = inputAdapter.entrypoints().stream()
                    .findFirst()
                    .map(Entrypoint::classPathText)
                    .orElse("");

            controllerList.add(Json.object("fqn", jigType.fqn())
                    .and("classPath", classPath)
                    .and("relations", Json.arrayObjects(edges))
                    .and("entrypoints", Json.arrayObjects(entrypointList)));
        });

        return Json.object("controllers", Json.arrayObjects(controllerList)).build();
    }
}
