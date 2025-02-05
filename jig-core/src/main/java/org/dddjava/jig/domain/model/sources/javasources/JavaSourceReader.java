package org.dddjava.jig.domain.model.sources.javasources;

import org.dddjava.jig.domain.model.data.term.Term;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Javaのテキストソースを読み取る
 */
public interface JavaSourceReader {

    JavaSourceModel javaSourceModel(JavaSources javaSources);

    Optional<Term> parsePackageInfoJavaFile(Path path);
}
