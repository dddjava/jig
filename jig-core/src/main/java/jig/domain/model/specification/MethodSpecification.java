package jig.domain.model.specification;

import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodSpecification {

    final String descriptor;
    public final MethodIdentifier identifier;

    public MethodSpecification(TypeIdentifier classTypeIdentifier, String name, String descriptor) {
        this.descriptor = descriptor;
        this.identifier = new MethodIdentifier(classTypeIdentifier, methodSignature(name, descriptor));
    }

    private static MethodSignature methodSignature(String name, String descriptor) {
        List<TypeIdentifier> arguments = Arrays.stream(Type.getArgumentTypes(descriptor))
                .map(Type::getClassName)
                .map(TypeIdentifier::new)
                .collect(Collectors.toList());
        return new MethodSignature(name, arguments);
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
        MethodIdentifier methodIdentifier = new MethodIdentifier(ownerTypeIdentifier, methodSignature(name, descriptor));
        usingMethodIdentifiers.add(methodIdentifier);
    }
}
