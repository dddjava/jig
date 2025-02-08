package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.FieldType;
import org.dddjava.jig.domain.model.data.classes.field.JigField;
import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.type.JigInstanceMember;
import org.dddjava.jig.domain.model.information.type.JigStaticMember;
import org.dddjava.jig.domain.model.information.type.JigTypeMembers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること
 */
public class JigMemberBuilder {

    final List<StaticFieldDeclaration> staticFieldDeclarations;
    final List<JigMethodBuilder> staticJigMethodBuilders;

    final List<JigMethodBuilder> constructorBuilders;

    private final Collection<JigFieldHeader> fieldHeaders;
    final List<JigField> instanceFields;
    final List<JigMethodBuilder> instanceJigMethodBuilders;

    private final List<RecordComponentDefinition> recordComponentDefinitions;

    public JigMemberBuilder() {
        this.instanceJigMethodBuilders = new ArrayList<>();
        this.staticJigMethodBuilders = new ArrayList<>();
        this.constructorBuilders = new ArrayList<>();
        this.instanceFields = new ArrayList<>();
        this.staticFieldDeclarations = new ArrayList<>();
        this.recordComponentDefinitions = new ArrayList<>();
        this.fieldHeaders = new ArrayList<>();
    }

    public List<JigMethodBuilder> allMethodBuilders() {
        ArrayList<JigMethodBuilder> list = new ArrayList<>();
        list.addAll(instanceJigMethodBuilders);
        list.addAll(staticJigMethodBuilders);
        list.addAll(constructorBuilders);
        return list;
    }

    public JigInstanceMember buildInstanceMember() {
        return new JigInstanceMember(
                new JigMethods(instanceJigMethodBuilders.stream().map(JigMethodBuilder::build).collect(toList())));
    }

    public JigStaticMember buildStaticMember() {
        return new JigStaticMember(
                new JigMethods(constructorBuilders.stream().map(JigMethodBuilder::build).collect(toList())),
                new JigMethods(staticJigMethodBuilders.stream().map(JigMethodBuilder::build).collect(toList()))
        );
    }

    public FieldDeclaration addInstanceField(TypeIdentifier owner, FieldType fieldType, String name) {
        FieldDeclaration fieldDeclaration = new FieldDeclaration(owner, fieldType, name);
        instanceFields.add(new JigField(fieldDeclaration));

        return fieldDeclaration;
    }

    // フィールドと別になっているのが微妙
    public void addFieldAnnotation(FieldAnnotation fieldAnnotation) {
        instanceFields.replaceAll(jigField -> {
            if (jigField.matches(fieldAnnotation.fieldDeclaration())) {
                return jigField.newInstanceWith(fieldAnnotation);
            }
            return jigField;
        });
    }

    public void addStaticField(TypeIdentifier owner, TypeIdentifier fieldTypeIdentifier, String name) {
        // instanceフィールドはFieldTypeを使っているが、staticフィールドのジェネリクスは扱えていなさそう
        staticFieldDeclarations.add(new StaticFieldDeclaration(owner, name, fieldTypeIdentifier));
    }

    public void addInstanceMethod(JigMethodBuilder jigMethodBuilder) {
        instanceJigMethodBuilders.add(jigMethodBuilder);
    }

    public void addConstructor(JigMethodBuilder jigMethodBuilder) {
        constructorBuilders.add(jigMethodBuilder);
    }

    public void addStaticMethod(JigMethodBuilder jigMethodBuilder) {
        staticJigMethodBuilders.add(jigMethodBuilder);
    }

    public void addRecordComponent(String name, TypeIdentifier typeIdentifier) {
        recordComponentDefinitions.add(new RecordComponentDefinition(name, typeIdentifier));
    }

    public boolean isRecordComponent(MethodDeclaration methodDeclaration) {
        return recordComponentDefinitions.stream()
                .anyMatch(recordComponentDefinition ->
                        methodDeclaration.methodSignature().methodName().equals(recordComponentDefinition.name())
                                && methodDeclaration.methodReturn().typeIdentifier().equals(recordComponentDefinition.typeIdentifier())
                );

    }

    public void addJigFieldHeader(JigFieldHeader jigFieldHeader) {
        fieldHeaders.add(jigFieldHeader);
    }

    public JigTypeMembers buildMember() {
        return new JigTypeMembers(
                fieldHeaders,
                List.of(), // メソッドはまだ
                buildStaticMember(),
                buildInstanceMember()
        );
    }
}
