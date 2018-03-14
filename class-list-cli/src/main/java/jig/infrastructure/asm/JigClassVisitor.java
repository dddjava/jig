package jig.infrastructure.asm;

import jig.domain.model.list.kind.Tag;
import jig.domain.model.list.kind.TagRepository;
import jig.domain.model.list.kind.ThingTag;
import jig.domain.model.thing.Name;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JigClassVisitor extends ClassVisitor {

    private static final Logger LOGGER = Logger.getLogger(JigClassVisitor.class.getName());

    private final TagRepository repository;

    private Name className;
    private int classAccess;
    private String classSuperName;
    private final List<String> fieldDescriptors = new ArrayList<>();
    private final List<String> methodDescriptors = new ArrayList<>();

    public JigClassVisitor(TagRepository repository) {
        super(Opcodes.ASM6);
        this.repository = repository;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = new Name(name.replace('/', '.'));
        this.classAccess = access;
        this.classSuperName = superName;

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        switch (descriptor) {
            case "Lorg/springframework/stereotype/Service;":
                repository.register(new ThingTag(className, Tag.SERVICE));
                break;
            case "Lorg/springframework/stereotype/Repository;":
                repository.register(new ThingTag(className, Tag.DATASOURCE));
                break;
            default:
                LOGGER.info(className.value() + "のアノテーションをスキップしました。: " + descriptor);
                break;
        }

        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if ((access & Opcodes.ACC_STATIC) == 0) {
            // インスタンスフィールドだけ相手にする
            fieldDescriptors.add(descriptor);
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if ((access & Opcodes.ACC_STATIC) == 0 && !name.equals("<init>")) {
            // インスタンスメソッドだけ相手にする
            methodDescriptors.add(descriptor);
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {

        if (classSuperName.equals("java/lang/Enum")) {
            if ((classAccess & Opcodes.ACC_FINAL) == 0) {
                // finalでないenumは多態
                repository.register(new ThingTag(className, Tag.ENUM_POLYMORPHISM));
            } else if (!fieldDescriptors.isEmpty()) {
                // フィールドがあるenum
                repository.register(new ThingTag(className, Tag.ENUM_PARAMETERIZED));
            } else if (!methodDescriptors.isEmpty()) {
                repository.register(new ThingTag(className, Tag.ENUM_BEHAVIOUR));
            } else {
                repository.register(new ThingTag(className, Tag.ENUM));
            }

            return;
        }

        if (fieldDescriptors.size() == 1) {
            String descriptor = fieldDescriptors.get(0);

            switch (descriptor) {
                case "Ljava/lang/String;":
                    repository.register(new ThingTag(className, Tag.IDENTIFIER));
                    break;
                case "Ljava/math/BigDecimal;":
                    repository.register(new ThingTag(className, Tag.NUMBER));
                    break;
                case "Ljava/util/List;":
                    repository.register(new ThingTag(className, Tag.FIRST_CLASS_COLLECTION));
                    break;
                case "Ljava/time/LocalDate;":
                    repository.register(new ThingTag(className, Tag.DATE));
                    break;
            }
        } else if (fieldDescriptors.size() == 2) {
            String field1 = fieldDescriptors.get(0);
            String field2 = fieldDescriptors.get(1);
            if (field1.equals(field2) && field1.equals("Ljava/time/LocalDate;")) {
                repository.register(new ThingTag(className, Tag.TERM));
            }
        }

        super.visitEnd();
    }
}
