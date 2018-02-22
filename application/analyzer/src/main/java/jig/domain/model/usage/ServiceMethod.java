package jig.domain.model.usage;

public class ServiceMethod {

    String name;
    Class<?> returnType;
    Class<?>[] parameters;

    public ServiceMethod(String name, Class<?> returnType, Class<?>[] parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public String name() {
        return name;
    }

    public Class<?> returnType() {
        return returnType;
    }

    public Class<?>[] parameters() {
        return parameters;
    }
}
