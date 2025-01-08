package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;

/**
 * 別名リポジトリ
 */
public interface CommentRepository {

    ClassComment get(TypeIdentifier typeIdentifier);

    boolean exists(PackageIdentifier packageIdentifier);

    PackageComment get(PackageIdentifier packageIdentifier);

    void register(ClassComment classComment);

    void register(PackageComment packageComment);
}
