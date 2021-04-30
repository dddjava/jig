package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.jigdocument.stationery.LinkPrefix;
import org.dddjava.jig.domain.model.parts.alias.AliasFinder;
import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.ResourceBundle;

public class ResourceBundleJigDocumentContext implements JigDocumentContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleJigDocumentContext.class);

    ResourceBundle jigDocumentResource;
    AliasFinder aliasFinder;
    LinkPrefix linkPrefix;

    ResourceBundleJigDocumentContext() {
        init();
    }

    ResourceBundleJigDocumentContext(AliasFinder aliasFinder, LinkPrefix linkPrefix) {
        init();
        this.aliasFinder = aliasFinder;
        this.linkPrefix = linkPrefix;
    }


    private void init() {
        try {
            jigDocumentResource = Utf8ResourceBundle.documentBundle();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String jigDocumentLabel(JigDocument jigDocument) {
        return label(jigDocument.name());
    }

    public String diagramLabel(JigDocument jigDocument) {
        return jigDocumentLabel(jigDocument);
    }

    public static ResourceBundleJigDocumentContext getInstance() {
        return new ResourceBundleJigDocumentContext();
    }

    public static JigDocumentContext getInstanceWithAliasFinder(AliasFinder aliasFinder, LinkPrefix linkPrefix) {
        return new ResourceBundleJigDocumentContext(aliasFinder, linkPrefix);
    }

    @Override
    public String label(String key) {
        if (jigDocumentResource.containsKey(key)) {
            return jigDocumentResource.getString(key);
        }
        // 取得できない場合はkeyをそのまま返す
        LOGGER.warn("Can't find resource for '{}'", key);
        return key;
    }

    public String reportLabel(ReportItem reportItem) {
        return label(reportItem.key);
    }

    @Override
    public DocumentName documentName(JigDocument jigDocument) {
        return DocumentName.of(jigDocument, diagramLabel(jigDocument));
    }

    @Override
    public AliasFinder aliasFinder() {
        Objects.requireNonNull(aliasFinder);
        return aliasFinder;
    }

    @Override
    public LinkPrefix linkPrefix() {
        return linkPrefix;
    }
}
