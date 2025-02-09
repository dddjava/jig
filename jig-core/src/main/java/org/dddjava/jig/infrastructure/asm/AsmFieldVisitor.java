package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.*;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
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

    private final Consumer<AsmFieldVisitor> finisher;
    private final Collection<JigAnnotationReference> declarationAnnotationCollector = new ArrayList<>();

    public AsmFieldVisitor(int api, Consumer<AsmFieldVisitor> finisher) {
        super(api);
        this.finisher = finisher;
    }

    static AsmFieldVisitor from(int api, int access, String name, String descriptor, String signature, TypeIdentifier declaringTypeIdentifier, JigMemberBuilder jigMemberBuilder) {
        logger.debug("field: name={}, descriptor={}, signature={}, declaringTypeIdentifier={}", name, descriptor, signature, declaringTypeIdentifier);

        return new AsmFieldVisitor(api, it -> {
            jigMemberBuilder.addJigFieldHeader(new JigFieldHeader(JigFieldIdentifier.from(declaringTypeIdentifier, name),
                    ((access & Opcodes.ACC_STATIC) == 0) ? JigMemberOwnership.INSTANCE : JigMemberOwnership.CLASS,
                    resolveFieldTypeReference(api, descriptor, signature),
                    new JigFieldAttribute(AsmUtils.resolveMethodVisibility(access), it.declarationAnnotationCollector, jigFieldFlags(access))));
        });
    }

    private static JigTypeReference resolveFieldTypeReference(int api, String descriptor, String signature) {
        if (signature == null) {
            TypeIdentifier fieldTypeIdentifier = AsmClassVisitor.typeDescriptorToIdentifier(descriptor);
            return JigTypeReference.fromId(fieldTypeIdentifier);
        }
        AsmTypeSignatureVisitor typeSignatureVisitor = new AsmTypeSignatureVisitor(api);
        new SignatureReader(signature).accept(typeSignatureVisitor);
        return typeSignatureVisitor.jigTypeReference();
    }

    private static EnumSet<JigFieldFlag> jigFieldFlags(int access) {
        EnumSet<JigFieldFlag> set = EnumSet.noneOf(JigFieldFlag.class);
        if ((access & Opcodes.ACC_FINAL) != 0) set.add(JigFieldFlag.FINAL);
        if ((access & Opcodes.ACC_TRANSIENT) != 0) set.add(JigFieldFlag.TRANSIENT);
        if ((access & Opcodes.ACC_VOLATILE) != 0) set.add(JigFieldFlag.VOLATILE);
        if ((access & Opcodes.ACC_SYNTHETIC) != 0) set.add(JigFieldFlag.SYNTHETIC);
        if ((access & Opcodes.ACC_ENUM) != 0) set.add(JigFieldFlag.ENUM);
        return set;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        logger.debug("visitAnnotation: {}, {}", descriptor, visible);
        TypeIdentifier annotationTypeIdentifier = AsmClassVisitor.typeDescriptorToIdentifier(descriptor);
        return new AsmAnnotationVisitor(this.api, annotationTypeIdentifier, it -> {
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
