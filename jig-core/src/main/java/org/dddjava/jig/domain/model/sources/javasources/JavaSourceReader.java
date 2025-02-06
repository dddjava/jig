package org.dddjava.jig.domain.model.sources.javasources;

import org.dddjava.jig.application.GlossaryRepository;

import java.nio.file.Path;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaSourceReader {

    void loadPackageInfoJavaFile(Path path, GlossaryRepository glossaryRepository);

    JavaSourceModel parseJavaFile(Path path, GlossaryRepository glossaryRepository);
}
