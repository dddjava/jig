package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

import java.nio.file.Path;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaSourceReader {

    void loadPackageInfoJavaFile(Path path, GlossaryRepository glossaryRepository);

    JavaSourceModel parseJavaFile(Path path, GlossaryRepository glossaryRepository);
}
