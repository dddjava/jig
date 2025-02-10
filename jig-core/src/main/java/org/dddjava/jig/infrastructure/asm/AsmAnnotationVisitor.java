package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigAnnotationInstanceElement;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.objectweb.asm.AnnotationVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ( visit | visitEnum | visitAnnotation | visitArray )* visitEnd.
 */
class AsmAnnotationVisitor extends AnnotationVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmAnnotationVisitor.class);

    private final TypeIdentifier annotationType;
    private final List<JigAnnotationInstanceElement> elementList = new ArrayList<>();
    private final Consumer<AsmAnnotationVisitor> finisher;

    public AsmAnnotationVisitor(int api, TypeIdentifier annotationType, Consumer<AsmAnnotationVisitor> finisher) {
        super(api);
        this.annotationType = annotationType;
        this.finisher = finisher;
    }

    @Override
    public void visit(String name, Object value) {
        logger.debug("visit: {}, {}", name, value);
        if (value instanceof org.objectweb.asm.Type typeValue) {
            TypeIdentifier typeIdentifier = AsmUtils.type2TypeIdentifier(typeValue);
            elementList.add(JigAnnotationInstanceElement.classElement(name, typeIdentifier));
        } else {
            elementList.add(JigAnnotationInstanceElement.element(name, value));
        }
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        logger.debug("visitEnum: {}, {}, {}", name, descriptor, value);
        TypeIdentifier typeIdentifier = AsmUtils.typeDescriptorToIdentifier(descriptor);
        elementList.add(JigAnnotationInstanceElement.enumElement(name, typeIdentifier, value));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        logger.debug("visitAnnotation: {}, {}", name, descriptor);
        TypeIdentifier typeIdentifier = AsmUtils.typeDescriptorToIdentifier(descriptor);
        return new AsmAnnotationVisitor(api, typeIdentifier, it -> {
            elementList.add(JigAnnotationInstanceElement.annotationElement(name, it.annotationType, it.elementList));
        });
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        logger.debug("visitArray: {}", name);
        return new AsmAnnotationVisitor(api, annotationType, it -> {
            elementList.add(JigAnnotationInstanceElement.arrayElement(name,
                    it.elementList.stream()
                            .map(JigAnnotationInstanceElement::value)
                            .toList()));
        });
    }

    @Override
    public void visitEnd() {
        logger.debug("visitEnd");
        finisher.accept(this);
    }

    public JigAnnotationReference annotationReference() {
        return new JigAnnotationReference(annotationType, elementList);
    }
}
