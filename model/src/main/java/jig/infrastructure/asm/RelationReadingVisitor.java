package jig.infrastructure.asm;

import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import org.objectweb.asm.*;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RelationReadingVisitor extends ClassVisitor {

    private static final Logger LOGGER = Logger.getLogger(RelationReadingVisitor.class.getName());

    private final RelationRepository relationRepository;

    private Name className;
    private Names interfaceNames;

    public RelationReadingVisitor(RelationRepository relationRepository) {
        super(Opcodes.ASM6);
        this.relationRepository = relationRepository;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = new Name(name.replace('/', '.'));

        this.interfaceNames = Arrays.stream(interfaces)
                .map(n -> n.replace('/', '.'))
                .map(Name::new)
                .peek(n -> relationRepository.register(RelationType.IMPLEMENT.of(className, n)))
                .collect(Names.collector());

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // インスタンスフィールドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0) {
            relationRepository.register(RelationType.FIELD.of(className, toName(descriptor)));
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // インスタンスメソッドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0 && !name.equals("<init>")) {

            // パラメーターの型
            Type[] argumentTypes = Type.getArgumentTypes(descriptor);
            String argumentsString = Arrays.stream(argumentTypes).map(Type::getClassName).collect(Collectors.joining(",", "(", ")"));

            // メソッド
            Name methodName = new Name(className.value() + "." + name + argumentsString);
            relationRepository.register(RelationType.METHOD.of(className, methodName));

            // 戻り値の型
            Name returnTypeName = new Name(Type.getReturnType(descriptor).getClassName());
            relationRepository.register(RelationType.METHOD_RETURN_TYPE.of(methodName, returnTypeName));

            for (Type type : argumentTypes) {
                Name argumentTypeName = new Name(type.getClassName());
                relationRepository.register(RelationType.METHOD_PARAMETER.of(methodName, argumentTypeName));
            }

            for (Name interfaceName : interfaceNames.list()) {
                relationRepository.register(RelationType.IMPLEMENT.of(methodName, interfaceName.concat(methodName)));
            }

            return new MethodVisitor(api) {

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    relationRepository.register(RelationType.METHOD_USE_TYPE.of(methodName, toName(descriptor)));

                    super.visitFieldInsn(opcode, owner, name, descriptor);
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
