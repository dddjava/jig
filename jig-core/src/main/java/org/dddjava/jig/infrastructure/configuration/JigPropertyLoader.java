package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.stationery.LinkPrefix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

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
    static Logger logger = Logger.getLogger(JigProperties.class.getName());

    private JigProperties jigProperties;

    public void load() {
        jigProperties = JigProperties.defaultInstance();

        Path homeConfigDirectoryPath = Paths.get(System.getProperty("user.home")).resolve(".jig");
        loadConfigFromPath(homeConfigDirectoryPath);

        Path currentDirectoryPath = Paths.get(System.getProperty("user.dir"));
        loadConfigFromPath(currentDirectoryPath);

        jigProperties.prepareOutputDirectory();
    }

    private void loadConfigFromPath(Path configDirectoryPath) {
        Path jigPropertiesPath = configDirectoryPath.resolve("jig.properties");
        if (jigPropertiesPath.toFile().exists()) {
            logger.warning(jigPropertiesPath.toAbsolutePath() + "をロードします。");
            try (InputStream is = Files.newInputStream(jigPropertiesPath)) {
                Properties properties = new Properties();
                properties.load(is);
                apply(properties);
            } catch (IOException e) {
                logger.warning("JIG設定ファイルのロードに失敗しました。" + e.toString());
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
    }

    public void apply(JigProperty jigProperty, String value) {
        switch (jigProperty) {
            case OUTPUT_DIRECTORY:
                jigProperties.outputDirectory = Paths.get(value);
                break;
            case OUTPUT_DIAGRAM_FORMAT:
                jigProperties.outputDiagramFormat = JigDiagramFormat.valueOf(value);
                break;
            case PATTERN_DOMAIN:
                jigProperties.businessRulePattern = value;
                break;
            case PATTERN_PRESENTATION:
                jigProperties.presentationPattern = value;
                break;
            case PATTERN_APPLICATION:
                jigProperties.applicationPattern = value;
                break;
            case PATTERN_INFRASTRUCTURE:
                jigProperties.infrastructurePattern = value;
                break;
            case LINK_PREFIX:
                jigProperties.linkPrefix = new LinkPrefix(value);
                break;
        }
    }
}
