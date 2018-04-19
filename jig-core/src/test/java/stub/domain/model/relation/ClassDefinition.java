package stub.domain.model.relation;

import stub.domain.model.relation.test.GenericArgument;
import stub.domain.model.relation.test.ImplementA;
import stub.domain.model.relation.test.ImplementB;
import stub.domain.model.relation.test.SuperClass;

public class ClassDefinition extends SuperClass implements ImplementA, ImplementB<GenericArgument> {
}
