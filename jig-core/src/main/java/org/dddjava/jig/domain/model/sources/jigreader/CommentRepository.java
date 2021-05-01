package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;

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
