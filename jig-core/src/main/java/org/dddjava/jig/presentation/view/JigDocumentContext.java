package org.dddjava.jig.presentation.view;

import org.dddjava.jig.presentation.view.report.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

public class JigDocumentContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(JigDocumentContext.class);

    Properties jigProperties;
    ResourceBundle jigDocumentResource;

    JigDocumentContext() {
        init();
    }

    private void init() {
        try {
            jigProperties = new Properties();
            try (InputStream is = JigDocumentContext.class.getClassLoader().getResourceAsStream("jig.properties")) {
                jigProperties.load(is);
            }
            jigDocumentResource = ResourceBundle.getBundle("jig-document");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String version() {
        return jigProperties.getProperty("version");
    }

    public String jigDocumentLabel(JigDocument jigDocument) {
        return label(jigDocument.name());
    }

    public String diagramLabel(JigDocument jigDocument) {
        return jigDocumentLabel(jigDocument) + " [" + version() + "]";
    }

    public static JigDocumentContext getInstance() {
        return new JigDocumentContext();
    }

    public String label(String key) {
        if (jigDocumentResource.containsKey(key)) {
            return jigDocumentResource.getString(key);
        }
        // 取得できない場合はkeyをそのまま返す
        LOGGER.warn("Can't find resource for '{}'", key);
        return key;
    }

    public String reportLabel(ReportItem reportItem) {
        return label(reportItem.name());
    }
}
