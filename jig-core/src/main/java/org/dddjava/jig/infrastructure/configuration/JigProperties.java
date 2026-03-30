package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
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
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                Object overriddenValue = field.get(overrideProperties);

                if (overriddenValue != null) {
                    if (field.getType().equals(String.class) && ((String) overriddenValue).isEmpty()) {
                        continue;
                    }
                    if (field.getType().equals(List.class) && ((List<?>) overriddenValue).isEmpty()) {
                        continue;
                    }
                    if (field.getType().equals(Path.class) && ((Path) overriddenValue).toString().isEmpty()) {
                        continue;
                    }
                    if (field.getType().equals(Optional.class) && ((Optional<?>) overriddenValue).isEmpty()) {
                        continue;
                    }

                    Object currentValue = field.get(this);
                    if (!overriddenValue.equals(currentValue)) {
                        field.set(this, overriddenValue);
                        logger.info("configure {} from {} to {}", field.getName(), currentValue, overriddenValue);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return "JigProperties{" +
                "businessRulePattern='" + domainPattern + '\'' +
                ", outputDirectory=" + outputDirectory +
                '}';
    }
}
