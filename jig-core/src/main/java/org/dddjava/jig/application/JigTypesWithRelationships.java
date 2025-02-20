package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.relations.TypeRelationships;

public record JigTypesWithRelationships(JigTypes jigTypes, TypeRelationships typeRelationships) {
}
