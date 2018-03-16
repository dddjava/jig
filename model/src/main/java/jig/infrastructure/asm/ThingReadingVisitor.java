package jig.infrastructure.asm;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;
import jig.domain.model.thing.ThingRepository;
import jig.domain.model.thing.ThingType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ThingReadingVisitor extends ClassVisitor {

    private static final Logger LOGGER = Logger.getLogger(ThingReadingVisitor.class.getName());

    private final ThingRepository thingRepository;

    private Name className;

    public ThingReadingVisitor(ThingRepository thingRepository) {
        super(Opcodes.ASM6);
        this.thingRepository = thingRepository;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = new Name(name.replace('/', '.'));

        thingRepository.register(new Thing(className, ThingType.TYPE));

        super.visit(version, access, name, signature, superName, interfaces);
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
            thingRepository.register(new Thing(methodName, ThingType.METHOD));
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
