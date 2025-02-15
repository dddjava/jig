package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.unit.ClassDeclaration;

import java.util.Collection;

/**
 * classファイル由来のソース
 */
public record ClassSourceModel(Collection<ClassDeclaration> classDeclarations) {
}
