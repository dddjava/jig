package jig.domain.model.relation.dependency;

import java.io.OutputStream;

public interface PackageDependencyWriter {

    void write(PackageDependencies packageDependencies, OutputStream outputStream);
}
