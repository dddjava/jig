package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypes;

/**
 * コアドメインのJigTypesと内部関連のペアコンテナ。
 */
public record CoreTypesAndRelations(JigTypes coreJigTypes, TypeRelationships internalTypeRelationships) {
}
