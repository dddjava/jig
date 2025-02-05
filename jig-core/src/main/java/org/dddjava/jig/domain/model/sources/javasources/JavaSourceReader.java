package org.dddjava.jig.domain.model.sources.javasources;

import org.dddjava.jig.domain.model.data.term.Term;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaSourceReader {

    Optional<Term> parsePackageInfoJavaFile(Path path);

    JavaSourceModel parseJavaFile(Path path, Consumer<Term> termCollector);
}
