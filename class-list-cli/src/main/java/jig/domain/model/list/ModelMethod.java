package jig.domain.model.list;

import jig.domain.model.thing.Name;

import java.util.List;

public class ModelMethod {

    private final String methodName;
    private final Name returnTypeName;
    private final List<Name> parameterTypeNames;

    public ModelMethod(String methodName, Name returnTypeName, List<Name> parameterTypeNames) {
        this.methodName = methodName;
        this.returnTypeName = returnTypeName;
        this.parameterTypeNames = parameterTypeNames;
    }

    public String name() {
        return methodName;
    }

    public List<Name> parameters() {
        return parameterTypeNames;
    }

    public Name returnTypeName() {
        return returnTypeName;
    }
}
