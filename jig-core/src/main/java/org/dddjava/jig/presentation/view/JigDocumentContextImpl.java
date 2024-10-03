package org.dddjava.jig.presentation.view;

import org.dddjava.jig.application.AliasService;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.stationery.LinkPrefix;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.Objects;

public class JigDocumentContextImpl implements JigDocumentContext {

    AliasService aliasService;
    LinkPrefix linkPrefix;

    public JigDocumentContextImpl(AliasService aliasService, LinkPrefix linkPrefix) {
        this.aliasService = aliasService;
        this.linkPrefix = linkPrefix;
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
