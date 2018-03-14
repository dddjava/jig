package jig.infrastructure.asm;

import jig.domain.model.list.kind.Tag;
import jig.domain.model.list.kind.TagRepository;
import jig.domain.model.list.kind.ThingTag;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;
import jig.domain.model.thing.ThingRepository;
import jig.domain.model.thing.ThingType;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JigClassVisitor extends ClassVisitor {

    private static final Logger LOGGER = Logger.getLogger(JigClassVisitor.class.getName());

    private final TagRepository tagRepository;
    private final ThingRepository thingRepository;
    private final RelationRepository relationRepository;

    private Name className;
    private int classAccess;
    private String classSuperName;
    private final List<String> fieldDescriptors = new ArrayList<>();
    private final List<String> methodDescriptors = new ArrayList<>();

    public JigClassVisitor(TagRepository tagRepository, ThingRepository thingRepository, RelationRepository relationRepository) {
        super(Opcodes.ASM6);
        this.tagRepository = tagRepository;
        this.thingRepository = thingRepository;
        this.relationRepository = relationRepository;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = new Name(name.replace('/', '.'));
        this.classAccess = access;
        this.classSuperName = superName;

        thingRepository.register(new Thing(className, ThingType.TYPE));

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        switch (descriptor) {
            case "Lorg/springframework/stereotype/Service;":
                tagRepository.register(new ThingTag(className, Tag.SERVICE));
                break;
            case "Lorg/springframework/stereotype/Repository;":
                tagRepository.register(new ThingTag(className, Tag.DATASOURCE));
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

            Type fieldType = Type.getType(descriptor);
            Name fieldTypeName = new Name(fieldType.getClassName());
            thingRepository.register(new Thing(fieldTypeName, ThingType.TYPE));
            relationRepository.register(RelationType.FIELD.create(className, fieldTypeName));
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        // インスタンスメソッドだけ相手にする
        if ((access & Opcodes.ACC_STATIC) == 0 && !name.equals("<init>")) {
            methodDescriptors.add(descriptor);

            // メソッド
            Name methodName = new Name(className.value() + "." + name);
            thingRepository.register(new Thing(methodName, ThingType.METHOD));
            relationRepository.register(RelationType.METHOD.create(className, methodName));

            // 戻り値の型
            Name returnTypeName = new Name(Type.getReturnType(descriptor).getClassName());
            thingRepository.register(new Thing(returnTypeName, ThingType.TYPE));
            relationRepository.register(RelationType.METHOD_RETURN_TYPE.create(methodName, returnTypeName));

            // パラメーターの型
            Type[] argumentTypes = Type.getArgumentTypes(descriptor);
            for (Type type : argumentTypes) {
                Name argumentTypeName = new Name(type.getClassName());
                thingRepository.register(new Thing(argumentTypeName, ThingType.TYPE));
                relationRepository.register(RelationType.METHOD_PARAMETER.create(methodName, argumentTypeName));
            }

            return new MethodVisitor(api) {

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    Type fieldType = Type.getType(descriptor);
                    Name fieldTypeName = new Name(fieldType.getClassName());
                    thingRepository.register(new Thing(fieldTypeName, ThingType.TYPE));
                    relationRepository.register(RelationType.METHOD_USE_TYPE.create(methodName, fieldTypeName));

                    super.visitFieldInsn(opcode, owner, name, descriptor);
                }
            };
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {

        if (classSuperName.equals("java/lang/Enum")) {
            if ((classAccess & Opcodes.ACC_FINAL) == 0) {
                // finalでないenumは多態
                tagRepository.register(new ThingTag(className, Tag.ENUM_POLYMORPHISM));
            } else if (!fieldDescriptors.isEmpty()) {
                // フィールドがあるenum
                tagRepository.register(new ThingTag(className, Tag.ENUM_PARAMETERIZED));
            } else if (!methodDescriptors.isEmpty()) {
                tagRepository.register(new ThingTag(className, Tag.ENUM_BEHAVIOUR));
            } else {
                tagRepository.register(new ThingTag(className, Tag.ENUM));
            }

            return;
        }

        if (fieldDescriptors.size() == 1) {
            String descriptor = fieldDescriptors.get(0);

            switch (descriptor) {
                case "Ljava/lang/String;":
                    tagRepository.register(new ThingTag(className, Tag.IDENTIFIER));
                    break;
                case "Ljava/math/BigDecimal;":
                    tagRepository.register(new ThingTag(className, Tag.NUMBER));
                    break;
                case "Ljava/util/List;":
                    tagRepository.register(new ThingTag(className, Tag.FIRST_CLASS_COLLECTION));
                    break;
                case "Ljava/time/LocalDate;":
                    tagRepository.register(new ThingTag(className, Tag.DATE));
                    break;
            }
        } else if (fieldDescriptors.size() == 2) {
            String field1 = fieldDescriptors.get(0);
            String field2 = fieldDescriptors.get(1);
            if (field1.equals(field2) && field1.equals("Ljava/time/LocalDate;")) {
                tagRepository.register(new ThingTag(className, Tag.TERM));
            }
        }

        super.visitEnd();
    }
}
