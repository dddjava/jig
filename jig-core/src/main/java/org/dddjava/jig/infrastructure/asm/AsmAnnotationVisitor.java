package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.data.types.JigAnnotationInstanceElement;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.objectweb.asm.AnnotationVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ( visit | visitEnum | visitAnnotation | visitArray )* visitEnd.
 */
class AsmAnnotationVisitor extends AnnotationVisitor {
    final AnnotationDescription annotationDescription = new AnnotationDescription();
    final TypeIdentifier annotationType;
    final Consumer<AsmAnnotationVisitor> finisher;

    public AsmAnnotationVisitor(int api, TypeIdentifier annotationType, Consumer<AsmAnnotationVisitor> finisher) {
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
        finisher.accept(this);
    }

    public JigAnnotationReference annotationReference() {
        return new JigAnnotationReference(annotationType,
                annotationDescription.entryStream()
                        .map(entry -> new JigAnnotationInstanceElement(entry.getKey(), entry.getValue()))
                        .toList()
        );
    }
}
