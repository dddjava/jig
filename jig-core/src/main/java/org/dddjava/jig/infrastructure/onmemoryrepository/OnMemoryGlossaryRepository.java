package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;
import org.dddjava.jig.domain.model.sources.javasources.comment.PackageComment;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OnMemoryGlossaryRepository implements GlossaryRepository {

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
