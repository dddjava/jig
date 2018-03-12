package jig.domain.model.list;

import jig.domain.model.relation.RelationRepository;
import jig.domain.model.usage.ModelMethod;
import jig.domain.model.usage.ModelType;

public class ConverterCondition {
    private final ModelType type;
    private final ModelMethod method;
    private final RelationRepository registerRelation;

    public ConverterCondition(ModelType type, ModelMethod method, RelationRepository registerRelation) {
        this.type = type;
        this.method = method;
        this.registerRelation = registerRelation;
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
}
