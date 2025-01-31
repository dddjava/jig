package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ( visitAnnotation | visitTypeAnnotation | visitAttribute )* visitEnd
 */
class AsmFieldVisitor extends FieldVisitor {

    private final Consumer<AsmFieldVisitor> finisher;
    final List<Annotation> annotations;

    public AsmFieldVisitor(int api, Consumer<AsmFieldVisitor> finisher) {
        super(api);
        this.finisher = finisher;
        this.annotations = new ArrayList<>();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        TypeIdentifier annotationTypeIdentifier = AsmClassVisitor.typeDescriptorToIdentifier(descriptor);
        return new AsmAnnotationVisitor(this.api, annotationTypeIdentifier, annotation -> annotations.add(annotation));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        // いまはなにもしていない
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        // いまはなにもしていない
        super.visitAttribute(attribute);
    }

    @Override
    public void visitEnd() {
        finisher.accept(this);
        super.visitEnd();
    }
}
