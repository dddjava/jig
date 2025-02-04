package org.dddjava.jig.domain.model.data.types;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

public class JigObjectId<T> extends TypeIdentifier{

    public JigObjectId(String value) {
        super(value);
    }

    public static JigObjectId<JigTypeHeader> fromJvmBinaryName(String jvmBinaryName) {
        return new JigObjectId<>(jvmBinaryName.replace('/', '.'));
    }

    public String simpleValue() {
        int lastDotIndex = value().lastIndexOf('.');
        return (lastDotIndex != -1) ? value().substring(lastDotIndex + 1) : value();
    }
}
