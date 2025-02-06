package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.FieldType;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

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

    static AsmFieldVisitor from(final int api, int access, String name, String descriptor, String signature, TypeIdentifier typeIdentifier, JigMemberBuilder jigMemberBuilder) {
        FieldType fieldType;
        TypeIdentifier fieldTypeIdentifier = AsmClassVisitor.typeDescriptorToIdentifier(descriptor);
        if (signature == null) {
            fieldType = new FieldType(fieldTypeIdentifier);
        } else {
            ArrayList<TypeIdentifier> typeParameters = new ArrayList<>();
            new SignatureReader(signature).accept(
                    new SignatureVisitor(api) {
                        @Override
                        public SignatureVisitor visitTypeArgument(char wildcard) {
                            if (wildcard == '=') {
                                return new SignatureVisitor(this.api) {
                                    @Override
                                    public void visitClassType(String name1) {
                                        typeParameters.add(TypeIdentifier.valueOf(name1));
                                    }
                                };
                            }
                            return super.visitTypeArgument(wildcard);
                        }
                    }
            );
            TypeIdentifiers typeIdentifiers = new TypeIdentifiers(typeParameters);
            fieldType = new FieldType(fieldTypeIdentifier, typeIdentifiers);
        }

        return new AsmFieldVisitor(api, it -> {
            if ((access & Opcodes.ACC_STATIC) == 0) {
                FieldDeclaration fieldDeclaration = jigMemberBuilder.addInstanceField(typeIdentifier, fieldType, name);
                it.annotations.forEach(annotation -> {
                    jigMemberBuilder.addFieldAnnotation(new FieldAnnotation(annotation, fieldDeclaration));
                });
            } else if (!name.equals("$VALUES")) {
                // staticフィールドのうち、enumにコンパイル時に作成される $VALUES は除く
                jigMemberBuilder.addStaticField(typeIdentifier, fieldTypeIdentifier, name);
            }
        });
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
