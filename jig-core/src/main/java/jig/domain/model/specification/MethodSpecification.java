package jig.domain.model.specification;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.MethodIdentifier;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodSpecification {

    // TODO 名前の混乱をなんとかする
    public final String methodName;
    public final MethodIdentifier identifier;
    public final String descriptor;

    public MethodSpecification(Identifier classIdentifier, String name, String descriptor) {
        this.methodName = name;
        this.descriptor = descriptor;

        this.identifier = new MethodIdentifier(classIdentifier, name, toArgumentSignatureString(descriptor));
    }

    private static String toArgumentSignatureString(String descriptor) {
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        return Arrays.stream(argumentTypes).map(Type::getClassName).collect(Collectors.joining(",", "(", ")"));
    }

    public final List<Identifier> usingFieldTypeIdentifiers = new ArrayList<>();
    public final List<MethodIdentifier> usingMethodIdentifiers = new ArrayList<>();

    public Identifier getReturnTypeName() {
        return new Identifier(Type.getReturnType(descriptor).getClassName());
    }

    public void addFieldInstruction(String owner, String name, String descriptor) {
        // 使っているフィールドの型がわかればOK
        Type type = Type.getType(descriptor);
        usingFieldTypeIdentifiers.add(new Identifier(type.getClassName()));
    }

    public void addMethodInstruction(String owner, String name, String descriptor) {
        // 使ってるメソッドがわかりたい
        Identifier ownerTypeIdentifier = new Identifier(owner);
        MethodIdentifier methodIdentifier = new MethodIdentifier(ownerTypeIdentifier, name, toArgumentSignatureString(descriptor));
        usingMethodIdentifiers.add(methodIdentifier);
    }
}
