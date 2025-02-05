package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * 別名リポジトリ
 */
public interface GlossaryRepository {

    ClassComment get(TypeIdentifier typeIdentifier);

    boolean exists(PackageIdentifier packageIdentifier);

    PackageComment get(PackageIdentifier packageIdentifier);

    void register(ClassComment classComment);

    void register(PackageComment packageComment);
}
