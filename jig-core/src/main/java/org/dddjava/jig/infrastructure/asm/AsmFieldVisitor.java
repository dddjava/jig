package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.JigFieldAttribute;
import org.dddjava.jig.domain.model.data.members.JigFieldFlag;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * FieldVisitor
 *
 * {@code ( visitAnnotation | visitTypeAnnotation | visitAttribute )* visitEnd}
 * シンプルなフィールドは visitEnd 以外呼ばれない。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.5">...</a>
 */
class AsmFieldVisitor extends FieldVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmFieldVisitor.class);

    private final ArrayList<JigAnnotationReference> declarationAnnotationCollector = new ArrayList<>();
    private final Consumer<AsmFieldVisitor> finisher;

    public AsmFieldVisitor(int api, Consumer<AsmFieldVisitor> finisher) {
        super(api);
        this.finisher = finisher;
    }

    static AsmFieldVisitor from(int api, int access, String name, String descriptor, String signature,
                                TypeIdentifier declaringTypeIdentifier, JigMemberBuilder jigMemberBuilder) {
        logger.debug("field: name={}, descriptor={}, signature={}, declaringTypeIdentifier={}", name, descriptor, signature, declaringTypeIdentifier);

        EnumSet<JigFieldFlag> flags = EnumSet.noneOf(JigFieldFlag.class);
        if ((access & Opcodes.ACC_FINAL) != 0) flags.add(JigFieldFlag.FINAL);
        if ((access & Opcodes.ACC_TRANSIENT) != 0) flags.add(JigFieldFlag.TRANSIENT);
        if ((access & Opcodes.ACC_VOLATILE) != 0) flags.add(JigFieldFlag.VOLATILE);
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) flags.add(JigFieldFlag.SYNTHETIC);
        if ((access & Opcodes.ACC_ENUM) != 0) flags.add(JigFieldFlag.ENUM);

        return new AsmFieldVisitor(api, it -> {
            jigMemberBuilder.addJigFieldHeader(new JigFieldHeader(JigFieldIdentifier.from(declaringTypeIdentifier, name),
                    AsmUtils.jigMemberOwnership(access),
                    resolveFieldTypeReference(api, descriptor, signature),
                    new JigFieldAttribute(AsmUtils.resolveMethodVisibility(access), it.declarationAnnotationCollector, flags)));
        });
    }

    private static JigTypeReference resolveFieldTypeReference(int api, String descriptor, String signature) {
        if (signature == null) {
            TypeIdentifier fieldTypeIdentifier = AsmUtils.typeDescriptorToIdentifier(descriptor);
            return JigTypeReference.fromId(fieldTypeIdentifier);
        }
        AsmTypeSignatureVisitor typeSignatureVisitor = new AsmTypeSignatureVisitor(api);
        new SignatureReader(signature).accept(typeSignatureVisitor);
        return typeSignatureVisitor.jigTypeReference();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        logger.debug("visitAnnotation: {}, {}", descriptor, visible);
        return new AsmAnnotationVisitor(this.api, AsmUtils.typeDescriptorToIdentifier(descriptor), it -> {
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
