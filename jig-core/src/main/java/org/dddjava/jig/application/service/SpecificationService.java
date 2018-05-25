package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedFields;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethods;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationSources;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.relation.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.relation.MethodRelations;
import org.dddjava.jig.domain.model.networks.TypeDependencies;
import org.dddjava.jig.domain.model.values.ValueTypes;
import org.springframework.stereotype.Service;

/**
 * 仕様サービス
 */
@Service
public class SpecificationService {

    final ImplementationFactory implementationFactory;

    public SpecificationService(ImplementationFactory implementationFactory) {
        this.implementationFactory = implementationFactory;
    }

    public ProjectData importSpecification(ImplementationSources implementationSources, ProjectData projectData) {
        if (implementationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        Implementations implementations = implementationFactory.readFrom(implementationSources);

        projectData.setAnnotatedFields(new AnnotatedFields(implementations));
        projectData.setAnnotatedMethods(new AnnotatedMethods(implementations));

        projectData.setFieldDeclarations(FieldDeclarations.ofInstanceField(implementations));
        projectData.setStaticFieldDeclarations(FieldDeclarations.ofStaticField(implementations));
        projectData.setImplementationMethods(new ImplementationMethods(implementations));
        projectData.setMethodRelations(new MethodRelations(implementations));
        projectData.setMethodUsingFields(new MethodUsingFields(implementations));

        projectData.setTypeDependencies(new TypeDependencies(implementations));
        projectData.setCharacterizedTypes(new CharacterizedTypes(implementations));
        projectData.setCharacterizedMethods(new CharacterizedMethods(implementations.instanceMethodSpecifications()));
        projectData.setValueTypes(new ValueTypes(implementations));

        return projectData;
    }
}
