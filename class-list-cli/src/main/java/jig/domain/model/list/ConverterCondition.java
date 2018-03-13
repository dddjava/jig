package jig.domain.model.list;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseName;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.domain.model.thing.Name;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ConverterCondition {
    private ModelType type;
    private ModelMethod method;
    private RelationRepository registerRelation;

    private Relation methodRelation;
    private RelationRepository relationRepository;
    private JapaneseNameDictionary japaneseNameRepository;

    public ConverterCondition(ModelType type, ModelMethod method, RelationRepository registerRelation, JapaneseNameDictionary japaneseNameRepository) {
        this.type = type;
        this.method = method;
        this.registerRelation = registerRelation;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public ConverterCondition(Relation methodRelation, RelationRepository relationRepository, JapaneseNameDictionary japaneseNameRepository) {
        this.methodRelation = methodRelation;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;

        Name methodName = methodRelation.to();
    }

    public ModelType getType() {
        return type;
    }

    public ModelMethod getMethod() {
        return method;
    }

    public RelationRepository getRegisterRelation() {
        return registerRelation;
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

    public List<Name> parameterTypeNames() {
        Relations relations = relationRepository.find(methodRelation.to(), RelationType.METHOD_PARAMETER);
        return relations.list().stream().map(Relation::to).collect(toList());
    }

    public List<Name> fieldTypeNames() {
        Relations relations = relationRepository.find(methodRelation.from(), RelationType.FIELD);
        return relations.list().stream().map(Relation::to).collect(toList());
    }
}
