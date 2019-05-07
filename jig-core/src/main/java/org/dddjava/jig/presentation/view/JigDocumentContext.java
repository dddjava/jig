package org.dddjava.jig.presentation.view;

import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

public class JigDocumentContext {

    Properties jigProperties;

    JigDocumentContext() {
        init();
    }

    private void init() {
        try {
            jigProperties = new Properties();
            try (InputStream is = JigDocumentContext.class.getClassLoader().getResourceAsStream("jig.properties")) {
                jigProperties.load(is);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String version() {
        return jigProperties.getProperty("version");
    }

    public String jigDocumentLabel(JigDocument jigDocument) {
        ResourceBundle jigDocumentResource = ResourceBundle.getBundle("jig-document");
        return jigDocumentResource.getString(jigDocument.name());
    }

    public String diagramLabel(JigDocument jigDocument) {
        return jigDocumentLabel(jigDocument) + " [" + version() + "]";
    }

    public static JigDocumentContext getInstance() {
        return new JigDocumentContext();
    }
}
