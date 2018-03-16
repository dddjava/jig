package jig.domain.model.report.method;

import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

public class MethodDetail {

    private Relation methodRelation;
    private RelationRepository relationRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public MethodDetail(Relation methodRelation, RelationRepository relationRepository, JapaneseNameRepository japaneseNameRepository) {
        this.methodRelation = methodRelation;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Name name() {
        return methodRelation.from();
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(name());
    }

    public Name methodName() {
        return methodRelation.to();
    }

    public Name returnTypeName() {
        Relation relation = relationRepository.get(methodName(), RelationType.METHOD_RETURN_TYPE);
        return relation.to();
    }

    public Names instructFields() {
        Relations relations = relationRepository.find(methodName(), RelationType.METHOD_USE_TYPE);
        return relations.list().stream().map(Relation::to).collect(Names.collector());
    }
}
