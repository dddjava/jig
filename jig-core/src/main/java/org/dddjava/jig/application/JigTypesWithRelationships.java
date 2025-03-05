package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypes;

public record JigTypesWithRelationships(JigTypes jigTypes, TypeRelationships typeRelationships) {
}
