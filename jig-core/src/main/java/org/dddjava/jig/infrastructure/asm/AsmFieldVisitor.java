package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.FieldType;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
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
import java.util.List;
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
    private final List<Annotation> annotations;
    private final Collection<JigAnnotationReference> annotationReferences = new ArrayList<>();

    public AsmFieldVisitor(int api, Consumer<AsmFieldVisitor> finisher) {
        super(api);
        this.finisher = finisher;
        this.annotations = new ArrayList<>();
    }

    static AsmFieldVisitor from(int api, int access, String name, String descriptor, String signature, TypeIdentifier declaringTypeIdentifier, JigMemberBuilder jigMemberBuilder) {
        logger.debug("field: name={}, descriptor={}, signature={}, declaringTypeIdentifier={}", name, descriptor, signature, declaringTypeIdentifier);
        FieldType fieldType;
        JigTypeReference jigTypeReference;
        if (signature == null) {
            TypeIdentifier fieldTypeIdentifier = AsmClassVisitor.typeDescriptorToIdentifier(descriptor);
            fieldType = new FieldType(fieldTypeIdentifier);
            jigTypeReference = JigTypeReference.fromId(fieldTypeIdentifier);
        } else {
            AsmTypeSignatureVisitor typeSignatureVisitor = new AsmTypeSignatureVisitor(api);
            new SignatureReader(signature).accept(typeSignatureVisitor);
            ParameterizedType parameterizedType = typeSignatureVisitor.generateParameterizedType();
            fieldType = new FieldType(parameterizedType);
            jigTypeReference = typeSignatureVisitor.jigTypeReference();
        }

        return new AsmFieldVisitor(api, it -> {
            if ((access & Opcodes.ACC_STATIC) == 0) {
                FieldDeclaration fieldDeclaration = jigMemberBuilder.addInstanceField(declaringTypeIdentifier, fieldType, name);
                it.annotations.forEach(annotation -> {
                    jigMemberBuilder.addFieldAnnotation(new FieldAnnotation(annotation, fieldDeclaration));
                });
            } else if (!name.equals("$VALUES")) {
                // staticフィールドのうち、enumにコンパイル時に作成される $VALUES は除く
                jigMemberBuilder.addStaticField(declaringTypeIdentifier, jigTypeReference.id(), name);
            }
            jigMemberBuilder.addJigFieldHeader(new JigFieldHeader(JigFieldIdentifier.from(declaringTypeIdentifier, name),
                    ((access & Opcodes.ACC_STATIC) == 0) ? JigMemberOwnership.INSTANCE : JigMemberOwnership.CLASS,
                    new JigFieldAttribute(resolveMethodVisibility(access), it.annotationReferences, jigFieldFlags(access), jigTypeReference)));
        });
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

    // methodと重複コード
    private static JigMemberVisibility resolveMethodVisibility(int access) {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return JigMemberVisibility.PUBLIC;
        if ((access & Opcodes.ACC_PROTECTED) != 0) return JigMemberVisibility.PROTECTED;
        if ((access & Opcodes.ACC_PRIVATE) != 0) return JigMemberVisibility.PRIVATE;
        return JigMemberVisibility.PACKAGE;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        logger.debug("visitAnnotation: {}, {}", descriptor, visible);
        TypeIdentifier annotationTypeIdentifier = AsmClassVisitor.typeDescriptorToIdentifier(descriptor);
        return new AsmAnnotationVisitor(this.api, annotationTypeIdentifier, it -> {
            annotations.add(new Annotation(it.annotationType, it.annotationDescription));
            annotationReferences.add(it.annotationReference());
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
