package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 型関連情報（type-relations-data.js）
 * DomainModel と PackageRelation の両ドキュメントで共有。
 */
public class TypeRelationsDataAdapter implements DataAdapter {

    private final JigService jigService;

    public TypeRelationsDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "typeRelationsData";
    }

    @Override
    public String dataFileName() {
        return "type-relations-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        var typeRelationships = jigService.typeRelationships(jigRepository);
        var relations = typeRelationships.list().stream()
                .collect(Collectors.groupingBy(
                        rel -> Map.entry(rel.from(), rel.to()),
                        LinkedHashMap::new,
                        Collectors.mapping(rel -> rel.typeRelationKind().name(), Collectors.toList())
                ))
                .entrySet().stream()
                .map(e -> Json.object("from", e.getKey().getKey().fqn())
                        .and("to", e.getKey().getValue().fqn())
                        .and("kinds", Json.array(e.getValue().stream().distinct().toList())))
                .toList();
        return Json.object("relations", Json.arrayObjects(relations)).build();
    }
}
