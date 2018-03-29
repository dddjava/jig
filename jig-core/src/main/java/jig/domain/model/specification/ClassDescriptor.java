package jig.domain.model.specification;

import jig.domain.model.identifier.type.TypeIdentifier;
import org.objectweb.asm.Type;

public class ClassDescriptor {

    String value;

    public ClassDescriptor(String value) {
        this.value = value;
    }

    public TypeIdentifier toTypeIdentifier() {
        Type type = Type.getType(value);
        String className = type.getClassName();
        return new TypeIdentifier(className);
    }
}
