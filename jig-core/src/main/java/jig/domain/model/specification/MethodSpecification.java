package jig.domain.model.specification;

import jig.domain.model.thing.Name;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MethodSpecification {

    // TODO 名前の混乱をなんとかする
    public final String methodName;
    public final Name name;
    public final String descriptor;

    public MethodSpecification(Name className, String name, String descriptor) {
        this.methodName = name;
        this.descriptor = descriptor;

        this.name = new Name(className.value() + "." + name + toArgumentSignatureString(descriptor));
    }

    private static String toArgumentSignatureString(String descriptor) {
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        return Arrays.stream(argumentTypes).map(Type::getClassName).collect(Collectors.joining(",", "(", ")"));
    }

    public final List<Name> usingFieldTypeNames = new ArrayList<>();
    public final List<Name> usingMethodNames = new ArrayList<>();

    public Name getReturnTypeName() {
        return new Name(Type.getReturnType(descriptor).getClassName());
    }

    public void addFieldInstruction(String owner, String name, String descriptor) {
        // 使っているフィールドの型がわかればOK
        Type type = Type.getType(descriptor);
        usingFieldTypeNames.add(new Name(type.getClassName()));
    }

    public void addMethodInstruction(String owner, String name, String descriptor) {
        // 使ってるメソッドがわかりたい
        Name ownerTypeName = new Name(owner);
        String methodName = ownerTypeName.value() + "." + name + toArgumentSignatureString(descriptor);
        usingMethodNames.add(new Name(methodName));
    }
}
