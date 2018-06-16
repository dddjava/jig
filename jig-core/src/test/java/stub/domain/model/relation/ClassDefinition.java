package stub.domain.model.relation;

import stub.domain.model.relation.clz.GenericsParameter;
import stub.domain.model.relation.clz.ImplementA;
import stub.domain.model.relation.clz.ImplementB;
import stub.domain.model.relation.clz.ClassAnnotation;
import stub.domain.model.relation.clz.SuperClass;

@ClassAnnotation
public class ClassDefinition extends SuperClass implements ImplementA, ImplementB<GenericsParameter> {
}
