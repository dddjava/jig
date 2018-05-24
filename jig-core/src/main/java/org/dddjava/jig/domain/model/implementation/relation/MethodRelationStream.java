package org.dddjava.jig.domain.model.implementation.relation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.stream.Stream;

public class MethodRelationStream {

    Stream<MethodRelation> stream;

    public MethodRelationStream(Stream<MethodRelation> stream) {
        this.stream = stream;
    }

    public MethodRelationStream filterTo(MethodDeclaration methodDeclaration) {
        return new MethodRelationStream(stream.filter(methodRelation -> methodRelation.toIs(methodDeclaration)));
    }

    public TypeIdentifiers fromTypeIdentifiers() {
        return stream.map(MethodRelation::from)
                .map(MethodDeclaration::declaringType)
                .collect(TypeIdentifiers.collector());
    }

    public MethodRelationStream filterFromTypeIsIncluded(TypeIdentifiers typeIdentifiers) {
        return new MethodRelationStream(stream.filter(methodRelation -> typeIdentifiers.contains(methodRelation.from().declaringType())));
    }

    public MethodDeclarations fromMethods() {
        return stream.map(MethodRelation::from)
                .collect(MethodDeclarations.collector());
    }

    public MethodRelationStream filterFrom(MethodDeclaration methodDeclaration) {
        return new MethodRelationStream(stream.filter(methodRelation -> methodRelation.fromIs(methodDeclaration)));
    }

    public MethodRelationStream filterToTypeIsIncluded(TypeIdentifiers typeIdentifiers) {
        return new MethodRelationStream(stream.filter(methodRelation -> typeIdentifiers.contains(methodRelation.to().declaringType())));
    }

    public MethodDeclarations toMethods() {
        return stream.map(MethodRelation::to)
                .collect(MethodDeclarations.collector());
    }

    public MethodRelationStream filterAnyFrom(MethodDeclarations methodDeclarations) {
        return new MethodRelationStream(stream.filter(methodRelation -> methodDeclarations.contains(methodRelation.from())));
    }

    public MethodRelationStream filterAnyTo(MethodDeclarations methodDeclarations) {
        return new MethodRelationStream(stream.filter(methodRelation -> methodDeclarations.contains(methodRelation.to())));
    }
}
