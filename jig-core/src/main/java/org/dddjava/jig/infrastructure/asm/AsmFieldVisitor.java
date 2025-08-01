package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldFlag;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * フィールドのバイトコードから必要な情報を抽出するMethodVisitorの実装
 *
 * {@code ( visitAnnotation | visitTypeAnnotation | visitAttribute )* visitEnd}
 * シンプルなフィールドは visitEnd 以外呼ばれない。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.5">4.5. Fields</a>
 */
class AsmFieldVisitor extends FieldVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmFieldVisitor.class);

    private final ArrayList<JigAnnotationReference> declarationAnnotationCollector = new ArrayList<>();
    private final Consumer<AsmFieldVisitor> finisher;

    private AsmFieldVisitor(int api, Consumer<AsmFieldVisitor> finisher) {
        super(api);
        this.finisher = finisher;
    }

    static AsmFieldVisitor from(AsmClassVisitor contextClass, int access, String name, String descriptor, String signature) {
        logger.debug("field: name={}, descriptor={}, signature={}", name, descriptor, signature);

        EnumSet<JigFieldFlag> flags = EnumSet.noneOf(JigFieldFlag.class);
        if ((access & Opcodes.ACC_FINAL) != 0) flags.add(JigFieldFlag.FINAL);
        if ((access & Opcodes.ACC_TRANSIENT) != 0) flags.add(JigFieldFlag.TRANSIENT);
        if ((access & Opcodes.ACC_VOLATILE) != 0) flags.add(JigFieldFlag.VOLATILE);
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) flags.add(JigFieldFlag.SYNTHETIC);
        if ((access & Opcodes.ACC_ENUM) != 0) flags.add(JigFieldFlag.ENUM);

        return new AsmFieldVisitor(contextClass.api(), it -> {
            contextClass.addJigFieldHeader(JigFieldHeader.from(JigFieldId.from(contextClass.jigTypeHeader().id(), name),
                    AsmUtils.jigMemberOwnership(access),
                    resolveFieldTypeReference(contextClass.api(), descriptor, signature),
                    AsmUtils.resolveMethodVisibility(access),
                    it.declarationAnnotationCollector,
                    flags
            ));
        });
    }

    private static JigTypeReference resolveFieldTypeReference(int api, String descriptor, String signature) {
        if (signature == null) {
            TypeId fieldTypeId = AsmUtils.typeDescriptorToTypeId(descriptor);
            return JigTypeReference.fromId(fieldTypeId);
        }
        AsmTypeSignatureVisitor typeSignatureVisitor = new AsmTypeSignatureVisitor(api);
        new SignatureReader(signature).accept(typeSignatureVisitor);
        return typeSignatureVisitor.jigTypeReference();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        logger.debug("visitAnnotation: {}, {}", descriptor, visible);
        return AsmAnnotationVisitor.from(this.api, descriptor, it -> {
            declarationAnnotationCollector.add(it.annotationReference());
        });
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        logger.debug("visitTypeAnnotation: {}, {}, {}, {}", typeRef, typePath, descriptor, visible);
        // いまはなにもしていない
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        logger.debug("visitAttribute: {}", attribute);
        // いまはなにもしていない
        super.visitAttribute(attribute);
    }

    @Override
    public void visitEnd() {
        logger.debug("visitEnd");
        finisher.accept(this);
        super.visitEnd();
    }
}
