package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.type.JigStaticMember;
import org.dddjava.jig.domain.model.information.type.JigTypeMembers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class JigMemberBuilder {

    final List<JigMethodBuilder> staticJigMethodBuilders = new ArrayList<>();
    final List<JigMethodBuilder> constructorBuilders = new ArrayList<>();
    final List<JigMethodBuilder> instanceJigMethodBuilders = new ArrayList<>();

    private final Collection<JigFieldHeader> fieldHeaders = new ArrayList<>();
    private final Collection<JigMethodHeader> methodHeaders = new ArrayList<>();

    private final List<RecordComponentDefinition> recordComponentDefinitions = new ArrayList<>();

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

    public void addJigMethodHeader(JigMethodHeader jigMethodHeader) {
        methodHeaders.add(jigMethodHeader);
    }

    public JigTypeMembers buildJigTypeMembers() {
        return new JigTypeMembers(
                fieldHeaders,
                methodHeaders,
                // 以下は互換のため。メソッドの実装をおえたら不要になる想定
                new JigStaticMember(
                        new JigMethods(constructorBuilders.stream().map(JigMethodBuilder::build).collect(toList())),
                        new JigMethods(staticJigMethodBuilders.stream().map(JigMethodBuilder::build).collect(toList()))
                ),
                new JigMethods(instanceJigMethodBuilders.stream().map(JigMethodBuilder::build).collect(toList()))
        );
    }

    public void applyAllMethodBuilders(Consumer<JigMethodBuilder> consumer) {
        instanceJigMethodBuilders.forEach(consumer);
        staticJigMethodBuilders.forEach(consumer);
        constructorBuilders.forEach(consumer);
    }
}
