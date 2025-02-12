package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.JigMethodDeclaration;
import org.dddjava.jig.domain.model.data.members.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigMethods;
import org.dddjava.jig.domain.model.information.types.JigStaticMember;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;

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
    private final Collection<JigMethodDeclaration> methodDeclarations = new ArrayList<>();

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

    public boolean isRecordComponent(JigMethodHeader jigMethodHeader) {
        return recordComponentDefinitions.stream()
                .anyMatch(recordComponentDefinition ->
                        jigMethodHeader.name().equals(recordComponentDefinition.name())
                                && jigMethodHeader.jigMethodAttribute().returnType().id().equals(recordComponentDefinition.typeIdentifier())
                );

    }

    public void addJigFieldHeader(JigFieldHeader jigFieldHeader) {
        fieldHeaders.add(jigFieldHeader);
    }

    public void addJigMethodDeclaration(JigMethodDeclaration jigMethodDeclaration) {
        methodDeclarations.add(jigMethodDeclaration);
    }

    public JigTypeMembers buildJigTypeMembers() {
        return new JigTypeMembers(
                fieldHeaders,
                methodDeclarations,
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
