package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.implementation.analyzed.japanese.MethodJapaneseName;
import org.dddjava.jig.domain.model.implementation.analyzed.japanese.TypeJapaneseName;

import java.util.List;

public class TypeSourceResult {
    TypeJapaneseName typeJapaneseName;
    List<MethodJapaneseName> methodJapaneseNames;

    public TypeSourceResult(TypeJapaneseName typeJapaneseName, List<MethodJapaneseName> methodJapaneseNames) {
        this.typeJapaneseName = typeJapaneseName;
        this.methodJapaneseNames = methodJapaneseNames;
    }
}
