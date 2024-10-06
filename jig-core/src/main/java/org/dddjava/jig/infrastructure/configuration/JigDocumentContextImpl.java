package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.AliasService;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.Objects;

public class JigDocumentContextImpl implements JigDocumentContext {

    AliasService aliasService;

    public JigDocumentContextImpl(AliasService aliasService) {
        this.aliasService = aliasService;
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
}
