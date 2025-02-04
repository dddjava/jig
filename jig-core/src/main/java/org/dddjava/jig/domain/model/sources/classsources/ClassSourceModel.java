package org.dddjava.jig.domain.model.sources.classsources;

import java.util.Collection;

/**
 * classファイル由来のソース
 */
public record ClassSourceModel(Collection<ClassDeclaration> classDeclarations) {
}
