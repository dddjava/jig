package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.application.CommentRepository;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OnMemoryCommentRepository implements CommentRepository {

    final Map<TypeIdentifier, ClassComment> map = new HashMap<>();
    final Map<PackageIdentifier, PackageComment> packageMap = new HashMap<>();

    @Override
    public ClassComment get(TypeIdentifier typeIdentifier) {
        return map.getOrDefault(typeIdentifier, ClassComment.empty(typeIdentifier));
    }

    @Override
    public boolean exists(PackageIdentifier packageIdentifier) {
        return packageMap.containsKey(packageIdentifier);
    }

    @Override
    public PackageComment get(PackageIdentifier packageIdentifier) {
        return packageMap.getOrDefault(packageIdentifier, PackageComment.empty(packageIdentifier));
    }

    @Override
    public void register(ClassComment classComment) {
        map.put(classComment.typeIdentifier(), classComment);
    }

    @Override
    public void register(PackageComment packageComment) {
        packageMap.put(packageComment.packageIdentifier(), packageComment);
    }
}
