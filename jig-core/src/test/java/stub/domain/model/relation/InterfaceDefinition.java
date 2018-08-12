package stub.domain.model.relation;

import stub.domain.model.relation.clz.ClassAnnotation;
import stub.domain.model.relation.clz.GenericsParameter;

import java.util.List;

@ClassAnnotation
public interface InterfaceDefinition extends Comparable<GenericsParameter> {

    List<String> parameterizedListMethod();
}
