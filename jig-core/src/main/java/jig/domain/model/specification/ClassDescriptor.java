package jig.domain.model.specification;

import jig.domain.model.identifier.Identifier;
import org.objectweb.asm.Type;

public class ClassDescriptor {

    String value;

    public ClassDescriptor(String value) {
        this.value = value;
    }

    public Identifier toTypeIdentifier() {
        Type type = Type.getType(value);
        String className = type.getClassName();
        return new Identifier(className);
    }
}
