package org.dddjava.jig.infrastructure.javaparser;

import org.dddjava.jig.domain.model.japanese.MethodJapaneseName;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;

import java.util.List;

public class TypeSourceResult {
    TypeJapaneseName typeJapaneseName;
    List<MethodJapaneseName> methodJapaneseNames;

    public TypeSourceResult(TypeJapaneseName typeJapaneseName, List<MethodJapaneseName> methodJapaneseNames) {
        this.typeJapaneseName = typeJapaneseName;
        this.methodJapaneseNames = methodJapaneseNames;
    }
}
