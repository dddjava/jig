package jig.domain.model.specification;

import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.identifier.TypeIdentifier;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodSpecification {

    final String descriptor;
    public final MethodIdentifier identifier;

    public MethodSpecification(TypeIdentifier classTypeIdentifier, String name, String descriptor) {
        this.descriptor = descriptor;
        this.identifier = new MethodIdentifier(classTypeIdentifier, name, toArgumentSignatureString(descriptor));
    }

    private static Identifiers toArgumentSignatureString(String descriptor) {
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        return Arrays.stream(argumentTypes).map(Type::getClassName).map(TypeIdentifier::new).collect(Identifiers.collector());
    }

    public final List<TypeIdentifier> usingFieldTypeIdentifiers = new ArrayList<>();
    public final List<MethodIdentifier> usingMethodIdentifiers = new ArrayList<>();

    public TypeIdentifier getReturnTypeName() {
        return new TypeIdentifier(Type.getReturnType(descriptor).getClassName());
    }

    public void addFieldInstruction(String owner, String name, String descriptor) {
        // 使っているフィールドの型がわかればOK
        Type type = Type.getType(descriptor);
        usingFieldTypeIdentifiers.add(new TypeIdentifier(type.getClassName()));
    }

    public void addMethodInstruction(String owner, String name, String descriptor) {
        // 使ってるメソッドがわかりたい
        TypeIdentifier ownerTypeIdentifier = new TypeIdentifier(owner);
        MethodIdentifier methodIdentifier = new MethodIdentifier(ownerTypeIdentifier, name, toArgumentSignatureString(descriptor));
        usingMethodIdentifiers.add(methodIdentifier);
    }
}
