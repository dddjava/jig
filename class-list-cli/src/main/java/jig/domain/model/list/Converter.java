package jig.domain.model.list;

import jig.domain.model.usage.ModelMethod;
import jig.domain.model.usage.ModelType;

public interface Converter {

    String convert(ModelType type, ModelMethod method);
}
