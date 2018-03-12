package jig.domain.model.list;

import jig.domain.model.relation.RelationRepository;
import jig.domain.model.tag.JapaneseName;
import jig.domain.model.tag.JapaneseNameDictionary;

public class ConverterCondition {
    private final ModelType type;
    private final ModelMethod method;
    private final RelationRepository registerRelation;
    private final JapaneseNameDictionary japaneseNameRepository;

    public ConverterCondition(ModelType type, ModelMethod method, RelationRepository registerRelation, JapaneseNameDictionary japaneseNameRepository) {
        this.type = type;
        this.method = method;
        this.registerRelation = registerRelation;
        this.japaneseNameRepository = japaneseNameRepository;
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

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(type.name());
    }
}
