package jig.domain.model.list;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseName;
import jig.domain.model.tag.JapaneseNameRepository;
import jig.domain.model.thing.Name;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ConverterCondition {

    private Relation methodRelation;
    private RelationRepository relationRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public ConverterCondition(Relation methodRelation, RelationRepository relationRepository, JapaneseNameRepository japaneseNameRepository) {
        this.methodRelation = methodRelation;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Name className() {
        return methodRelation.from();
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(methodRelation.from());
    }

    public Name methodName() {
        return methodRelation.to();
    }

    public Name returnTypeName() {
        Relation relation = relationRepository.get(methodRelation.to(), RelationType.METHOD_RETURN_TYPE);
        return relation.to();
    }

    public List<Name> instructFields() {
        Relations relations = relationRepository.find(methodRelation.to(), RelationType.METHOD_USE_TYPE);
        return relations.list().stream().map(Relation::to).collect(toList());
    }
}
