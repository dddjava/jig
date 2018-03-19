package jig.infrastructure.asm;

import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.thing.Name;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RelationReadingVisitor extends ClassVisitor {

    private static final Logger LOGGER = Logger.getLogger(RelationReadingVisitor.class.getName());

    private final RelationRepository relationRepository;

    private Name className;

    public RelationReadingVisitor(RelationRepository relationRepository) {
        super(Opcodes.ASM6);
        this.relationRepository = relationRepository;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = new Name(name);

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // インスタンスメソッドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0 && !name.equals("<init>")) {

            // パラメーターの型
            Type[] argumentTypes = Type.getArgumentTypes(descriptor);

            // メソッド
            String argumentsString = Arrays.stream(argumentTypes).map(Type::getClassName).collect(Collectors.joining(",", "(", ")"));
            Name methodName = new Name(className.value() + "." + name + argumentsString);

            return new MethodVisitor(api) {

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    relationRepository.register(RelationType.METHOD_USE_TYPE.of(methodName, toName(descriptor)));

                    super.visitFieldInsn(opcode, owner, name, descriptor);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    relationRepository.register(RelationType.METHOD_USE_METHOD.of(methodName,
                            new Name(owner + "." + name)));

                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            };
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    private Name toName(String descriptor) {
        Type fieldType = Type.getType(descriptor);
        return new Name(fieldType.getClassName());
    }
}
