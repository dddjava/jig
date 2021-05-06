package org.dddjava.jig.presentation.view;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.jigdocument.stationery.LinkPrefix;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.ResourceBundle;

public class ResourceBundleJigDocumentContext implements JigDocumentContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleJigDocumentContext.class);

    ResourceBundle jigDocumentResource;
    AliasService aliasService;
    LinkPrefix linkPrefix;

    ResourceBundleJigDocumentContext() {
        init();
    }

    ResourceBundleJigDocumentContext(AliasService aliasService, LinkPrefix linkPrefix) {
        init();
        this.aliasService = aliasService;
        this.linkPrefix = linkPrefix;
    }


    private void init() {
        try {
            jigDocumentResource = Utf8ResourceBundle.documentBundle();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResourceBundleJigDocumentContext getInstance() {
        return new ResourceBundleJigDocumentContext();
    }

    public static JigDocumentContext getInstanceWithAliasFinder(AliasService aliasService, LinkPrefix linkPrefix) {
        return new ResourceBundleJigDocumentContext(aliasService, linkPrefix);
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
    public PackageComment packageComment(PackageIdentifier packageIdentifier) {
        Objects.requireNonNull(aliasService);
        return aliasService.packageAliasOf(packageIdentifier);
    }

    @Override
    public ClassComment classComment(TypeIdentifier typeIdentifier) {
        Objects.requireNonNull(aliasService);
        return aliasService.typeAliasOf(typeIdentifier);
    }

    @Override
    public LinkPrefix linkPrefix() {
        return linkPrefix;
    }
}
