package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * 実行時に設定するプロパティ。
 */
public class JigProperties {
    private static final Logger logger = LoggerFactory.getLogger(JigProperties.class);

    /**
     * 主要: ドメイン（主として扱うクラス名）のパターン
     */
    Optional<String> domainPattern;
    /**
     * 主要: ドキュメントの出力先ディレクトリ
     */
    Path outputDirectory;

    /**
     * 出力対象となるJigDocumentのリスト。
     * 全部出ると邪魔／時間がかかる時に指定。
     */
    List<JigDocument> jigDocuments;

    public JigProperties(List<JigDocument> jigDocuments, Optional<String> domainPattern, Path outputDirectory) {
        this.jigDocuments = jigDocuments;
        this.domainPattern = domainPattern;
        this.outputDirectory = outputDirectory;
    }

    static JigProperties defaultInstance() {
        return new JigProperties(JigDocument.canonical(), Optional.empty(), Paths.get(JigProperty.defaultOutputDirectory()));
    }

    public Optional<String> getDomainPattern() {
        return domainPattern;
    }

    public void override(JigProperties overrideProperties) {
        if (overrideProperties.outputDirectory != null
                && !overrideProperties.outputDirectory.toString().isEmpty()
                && !overrideProperties.outputDirectory.equals(this.outputDirectory)) {
            logger.info("configure outputDirectory from {} to {}", this.outputDirectory, overrideProperties.outputDirectory);
            this.outputDirectory = overrideProperties.outputDirectory;
        }
        if (overrideProperties.domainPattern != null
                && overrideProperties.domainPattern.isPresent()
                && !overrideProperties.domainPattern.equals(this.domainPattern)) {
            logger.info("configure domainPattern from {} to {}", this.domainPattern, overrideProperties.domainPattern);
            this.domainPattern = overrideProperties.domainPattern;
        }
        if (overrideProperties.jigDocuments != null
                && !overrideProperties.jigDocuments.isEmpty()
                && !overrideProperties.jigDocuments.equals(this.jigDocuments)) {
            logger.info("configure jigDocuments from {} to {}", this.jigDocuments, overrideProperties.jigDocuments);
            this.jigDocuments = overrideProperties.jigDocuments;
        }
    }

    @Override
    public String toString() {
        return "JigProperties{" +
                "domainPattern='" + domainPattern + '\'' +
                ", outputDirectory=" + outputDirectory +
                '}';
    }
}
