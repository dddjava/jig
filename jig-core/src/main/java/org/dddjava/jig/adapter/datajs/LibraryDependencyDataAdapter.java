package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.knowledge.library.LibraryDependencyDiagram;

import java.util.ArrayList;
import java.util.List;

/**
 * ライブラリ依存図（library-dependency-data.js）
 */
public class LibraryDependencyDataAdapter implements DataAdapter {

    private final JigService jigService;

    public LibraryDependencyDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "libraryDependencyData";
    }

    @Override
    public String dataFileName() {
        return "library-dependency-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        LibraryDependencyDiagram diagram = jigService.libraryDependencyDiagram(jigRepository);

        var libraries = diagram.libraries().stream()
                .map(library -> Json.object("id", library.id())
                        .and("displayName", library.displayName())
                        .and("isJavaStandard", library.isJavaStandard())
                        .and("samplePackages", Json.array(new ArrayList<>(library.samplePackages())))
                        .and("usingClasses", Json.array(new ArrayList<>(library.usingClasses()))))
                .toList();

        var edges = diagram.edges().stream()
                .map(edge -> Json.object("from", edge.from()).and("to", edge.to()))
                .toList();

        return Json.object("internalPackages", Json.array(List.copyOf(diagram.internalPackageFqns())))
                .and("libraries", Json.arrayObjects(libraries))
                .and("relations", Json.arrayObjects(edges))
                .build();
    }
}
