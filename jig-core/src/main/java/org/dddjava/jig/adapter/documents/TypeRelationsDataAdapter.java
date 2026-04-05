package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.JigDocumentAdapter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.JigRepository;

/**
 * 型関連情報（type-relations-data.js）
 * DomainModel と PackageRelation の両ドキュメントで共有。
 */
public class TypeRelationsDataAdapter implements JigDocumentAdapter {

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
        return Json.object("relations", Json.arrayObjects(typeRelationships.list().stream()
                .map(relation -> Json.object("from", relation.from().fqn())
                        .and("to", relation.to().fqn()))
                .toList())).build();
    }
}
