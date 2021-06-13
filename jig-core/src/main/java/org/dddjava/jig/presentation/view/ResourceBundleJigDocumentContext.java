package org.dddjava.jig.presentation.view;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.stationery.LinkPrefix;
import org.dddjava.jig.domain.model.documents.stationery.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.ResourceBundle;

public class ResourceBundleJigDocumentContext implements JigDocumentContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleJigDocumentContext.class);

    ResourceBundle jigDocumentResource = Utf8ResourceBundle.getBundle();
    AliasService aliasService;
    LinkPrefix linkPrefix;
    PackageIdentifierFormatter packageIdentifierFormatter;

    ResourceBundleJigDocumentContext(AliasService aliasService, LinkPrefix linkPrefix, PackageIdentifierFormatter packageIdentifierFormatter) {
        this.aliasService = aliasService;
        this.linkPrefix = linkPrefix;
        this.packageIdentifierFormatter = packageIdentifierFormatter;
    }

    public static JigDocumentContext getInstanceWithAliasFinder(AliasService aliasService, LinkPrefix linkPrefix, PackageIdentifierFormatter packageIdentifierFormatter) {
        return new ResourceBundleJigDocumentContext(aliasService, linkPrefix, packageIdentifierFormatter);
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
    public PackageIdentifierFormatter packageIdentifierFormatter() {
        return packageIdentifierFormatter;
    }

    @Override
    public LinkPrefix linkPrefix() {
        return linkPrefix;
    }
}
