package jig.domain.model.report.type;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;

public class TypeDetail {

    private final Identifier identifier;
    private final Characteristic characteristic;
    private RelationRepository relationRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public TypeDetail(Identifier identifier, Characteristic characteristic, RelationRepository relationRepository, JapaneseNameRepository japaneseNameRepository) {
        this.identifier = identifier;
        this.characteristic = characteristic;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Identifier name() {
        return identifier;
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(name());
    }

    public Identifiers usage() {
        Relations fieldRelation = relationRepository.findTo(name(), RelationType.FIELD);

        // TODO メソッドでの使用をどのように扱うか
        // Relations methodReturnRelation = relationRepository.findTo(identifier(), RelationType.METHOD_RETURN_TYPE);
        // Relations methodParameterRelation = relationRepository.findTo(identifier(), RelationType.METHOD_PARAMETER);

        return fieldRelation.list().stream().map(Relation::from).collect(Identifiers.collector());
    }

    public boolean is(Characteristic characteristic) {
        return this.characteristic == characteristic;
    }
}
