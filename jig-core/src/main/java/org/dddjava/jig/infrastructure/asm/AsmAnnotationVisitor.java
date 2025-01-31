package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.objectweb.asm.AnnotationVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ( visit | visitEnum | visitAnnotation | visitArray )* visitEnd.
 */
class AsmAnnotationVisitor extends AnnotationVisitor {
    final AnnotationDescription annotationDescription = new AnnotationDescription();
    private final TypeIdentifier annotationType;
    final Consumer<Annotation> finisher;

    public AsmAnnotationVisitor(int api, TypeIdentifier annotationType, Consumer<Annotation> finisher) {
        super(api);
        this.annotationType = annotationType;
        this.finisher = finisher;
    }

    @Override
    public void visit(String name, Object value) {
        annotationDescription.addParam(name, value);
        super.visit(name, value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        annotationDescription.addEnum(name, value);
        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        annotationDescription.addAnnotation(name, descriptor);
        return super.visitAnnotation(name, descriptor);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {

        return new AnnotationVisitor(api) {
            final List<Object> list = new ArrayList<>();

            @Override
            public void visit(String name, Object value) {
                list.add(value);
            }

            @Override
            public void visitEnd() {
                annotationDescription.addArray(name, list);
            }
        };
    }

    @Override
    public void visitEnd() {
        finisher.accept(new Annotation(annotationType, annotationDescription));
    }
}
