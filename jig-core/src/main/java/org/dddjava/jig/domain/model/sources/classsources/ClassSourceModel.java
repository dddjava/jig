package org.dddjava.jig.domain.model.sources.classsources;

import java.util.Collection;

/**
 * classファイル由来のソース
 */
public record ClassSourceModel(Collection<ClassDeclaration> classDeclarations) {

    public static ClassSourceModel from(Collection<ClassDeclaration> classDeclarations) {
        return new ClassSourceModel(classDeclarations);
    }
}
