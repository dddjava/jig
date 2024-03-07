package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.stationery.JigPropertyHolder;
import org.dddjava.jig.domain.model.documents.stationery.LinkPrefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

/**
 * JIGの設定を読み込みます。
 *
 * 設定は以下の優先順位で適用します。
 *
 * 1. 実行形式固有の設定
 * 2. 実行ディレクトリの設定ファイル {user.dir}/jig.properties
 * 3. ホームディレクトリの設定ファイル {user.home}/.jig/jig.properties
 * 4. デフォルト値（JigPropertyで定義）
 */
public class JigPropertyLoader {
    static Logger logger = LoggerFactory.getLogger(JigPropertyLoader.class);

    private final JigProperties primaryProperty;
    private JigProperties jigProperties;

    public JigPropertyLoader(JigProperties primaryProperty) {
        this.primaryProperty = primaryProperty;
        this.jigProperties = JigProperties.defaultInstance();
    }

    public JigProperties load() {
        loadEnvironmentProperty();
        applySpecifyProperty();
        return jigProperties;
    }

    private void applySpecifyProperty() {
        jigProperties.override(primaryProperty);
    }

    private void loadEnvironmentProperty() {
        try {
            Path homeConfigDirectoryPath = Paths.get(System.getProperty("user.home")).resolve(".jig");
            loadConfigFromPath(homeConfigDirectoryPath);

            Path currentDirectoryPath = Paths.get(System.getProperty("user.dir"));
            loadConfigFromPath(currentDirectoryPath);
        } catch (Exception e) {
            // 2020.10.2 設定の読み込みを変更
            // 失敗した場合は既存を維持しておく
            logger.error("設定ファイルの読み込みに失敗しました。例外情報を添えて不具合を報告してください。処理は続行します。", e);
            // 初期値に戻す
            jigProperties = JigProperties.defaultInstance();
        }
    }

    private void loadConfigFromPath(Path configDirectoryPath) {
        Path jigPropertiesPath = configDirectoryPath.resolve("jig.properties");
        logger.debug("try to load " + jigPropertiesPath.toAbsolutePath() + " ...");
        if (jigPropertiesPath.toFile().exists()) {
            try (InputStream is = Files.newInputStream(jigPropertiesPath)) {
                Properties properties = new Properties();
                properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                apply(properties);
                logger.info("configuration loaded from {}", jigPropertiesPath.toAbsolutePath());
            } catch (IOException e) {
                logger.warn("fail to load {}", jigPropertiesPath, e);
            }
        }
    }

    void apply(Properties properties) {
        for (JigProperty jigProperty : JigProperty.values()) {
            String key = "jig." + jigProperty.name().toLowerCase().replace("_", ".");
            if (properties.containsKey(key)) {
                apply(jigProperty, properties.getProperty(key));
            }
        }

        JigPropertyHolder.getInstance().load(properties);
    }

    public void apply(JigProperty jigProperty, String value) {
        switch (jigProperty) {
            case OUTPUT_DIRECTORY:
                jigProperties.outputDirectory = Paths.get(value);
                break;
            case OUTPUT_DIAGRAM_FORMAT:
                jigProperties.outputDiagramFormat = JigDiagramFormat.valueOf(value.toUpperCase(Locale.ENGLISH));
                break;
            case PATTERN_DOMAIN:
                jigProperties.domainPattern = value;
                break;
            case LINK_PREFIX:
                jigProperties.linkPrefix = new LinkPrefix(value);
                break;
        }
    }
}
