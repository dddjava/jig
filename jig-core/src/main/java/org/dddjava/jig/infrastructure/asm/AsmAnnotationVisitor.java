package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.types.JigAnnotationElementValuePair;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.objectweb.asm.AnnotationVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AnnotationVisitorの実装
 *
 * アノテーションに設定されている要素を読み取る
 *
 * ```
 * ( visit | visitEnum | visitAnnotation | visitArray )* visitEnd
 * ```
 */
class AsmAnnotationVisitor extends AnnotationVisitor {
    private static final Logger logger = LoggerFactory.getLogger(AsmAnnotationVisitor.class);

    private final TypeId annotationType;
    private final List<JigAnnotationElementValuePair> elementList = new ArrayList<>();
    private final Consumer<AsmAnnotationVisitor> finisher;

    private AsmAnnotationVisitor(int api, TypeId annotationType, Consumer<AsmAnnotationVisitor> finisher) {
        super(api);
        this.annotationType = annotationType;
        this.finisher = finisher;
    }

    public static AsmAnnotationVisitor from(int api, String descriptor, Consumer<AsmAnnotationVisitor> finisher) {
        TypeId typeId = AsmUtils.typeDescriptorToTypeId(descriptor);
        return new AsmAnnotationVisitor(api, typeId, finisher);
    }

    @Override
    public void visit(String name, Object value) {
        logger.debug("visit: {}, {}", name, value);
        if (value instanceof org.objectweb.asm.Type typeValue) {
            TypeId typeId = AsmUtils.type2TypeId(typeValue);
            elementList.add(JigAnnotationElementValuePair.classElement(name, typeId));
        } else {
            elementList.add(JigAnnotationElementValuePair.element(name, value));
        }
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        logger.debug("visitEnum: {}, {}, {}", name, descriptor, value);
        TypeId typeId = AsmUtils.typeDescriptorToTypeId(descriptor);
        elementList.add(JigAnnotationElementValuePair.enumElement(name, typeId, value));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        logger.debug("visitAnnotation: {}, {}", name, descriptor);
        return from(api, descriptor, it -> {
            elementList.add(JigAnnotationElementValuePair.annotationElement(name, it.annotationType, it.elementList));
        });
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        logger.debug("visitArray: {}", name);
        return new AsmAnnotationVisitor(api, annotationType, it -> {
            elementList.add(JigAnnotationElementValuePair.arrayElement(name,
                    // このメソッドで生成されるVisitorのvisitに渡されるelementのnameはnullとなる。
                    // AsmAnnotationVisitorではなくarray用のを作った方がいいかもしれないが、
                    // nameがnullになる以外に個別処理があるわけでもなく、name自体はarrayのものを採用すればよいので、
                    // nameを無視してvalueのみ参照する。
                    it.elementList.stream()
                            .map(JigAnnotationElementValuePair::value)
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
