package stub.domain.model.relation;

import stub.domain.model.relation.clz.*;

@ClassAnnotation
public class ClassDefinition extends SuperClass<Integer, Long> implements ImplementA, ImplementB<GenericsParameter> {
}
