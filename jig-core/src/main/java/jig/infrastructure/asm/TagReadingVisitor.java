package jig.infrastructure.asm;

import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.thing.Name;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TagReadingVisitor extends ClassVisitor {

    private static final Logger LOGGER = Logger.getLogger(TagReadingVisitor.class.getName());

    private final TagRepository tagRepository;

    private Name className;
    private int classAccess;
    private String classSuperName;
    private final List<String> fieldDescriptors = new ArrayList<>();
    private final List<String> methodDescriptors = new ArrayList<>();

    public TagReadingVisitor(TagRepository tagRepository) {
        super(Opcodes.ASM6);
        this.tagRepository = tagRepository;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = new Name(name.replace('/', '.'));
        this.classAccess = access;
        this.classSuperName = superName;

        Tag.registerTag(tagRepository, className);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        Tag.registerTag(tagRepository, className, descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // インスタンスフィールドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0) {
            fieldDescriptors.add(descriptor);
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // インスタンスメソッドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0 && !name.equals("<init>")) {
            methodDescriptors.add(descriptor);
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {

        if (classSuperName.equals("java/lang/Enum")) {
            if ((classAccess & Opcodes.ACC_FINAL) == 0) {
                // finalでないenumは多態
                tagRepository.register(className, Tag.ENUM_POLYMORPHISM);
            } else if (!fieldDescriptors.isEmpty()) {
                // フィールドがあるenum
                tagRepository.register(className, Tag.ENUM_PARAMETERIZED);
            } else if (!methodDescriptors.isEmpty()) {
                tagRepository.register(className, Tag.ENUM_BEHAVIOUR);
            } else {
                tagRepository.register(className, Tag.ENUM);
            }

            super.visitEnd();
            return;
        }

        Tag.registerTag(tagRepository, className, fieldDescriptors);
        super.visitEnd();
    }
}
