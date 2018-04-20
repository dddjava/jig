package jig.infrastructure.asm;

import jig.domain.model.identifier.type.TypeIdentifier;
import org.objectweb.asm.Type;

public class TypeDescriptor {

    String value;

    public TypeDescriptor(String value) {
        this.value = value;
    }

    public TypeIdentifier toTypeIdentifier() {
        Type type = Type.getType(value);
        String className = type.getClassName();
        return new TypeIdentifier(className);
    }
}
