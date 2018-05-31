package org.dddjava.jig.domain.model.japanese;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

/**
 * 和名リポジトリ
 */
public interface JapaneseNameRepository {

    JapaneseName get(TypeIdentifier typeIdentifier);

    boolean exists(PackageIdentifier packageIdentifier);

    JapaneseName get(PackageIdentifier packageIdentifier);

    void register(TypeJapaneseName typeJapaneseName);

    void register(PackageJapaneseName packageJapaneseName);

    JapaneseName get(MethodDeclaration methodDeclaration);

    void register(MethodJapaneseName methodJapaneseName);
}
