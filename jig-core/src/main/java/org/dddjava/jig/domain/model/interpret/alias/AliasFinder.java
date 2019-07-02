package org.dddjava.jig.domain.model.interpret.alias;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 別名発見機
 */
public interface AliasFinder {

    PackageAlias find(PackageIdentifier packageIdentifier);

    TypeAlias find(TypeIdentifier typeIdentifier);

    MethodAlias find(MethodIdentifier methodIdentifier);

    class GlossaryServiceAdapter implements AliasFinder {

        private final AliasService aliasService;

        public GlossaryServiceAdapter(AliasService aliasService) {
            this.aliasService = aliasService;
        }

        @Override
        public PackageAlias find(PackageIdentifier packageIdentifier) {
            return aliasService.packageAliasOf(packageIdentifier);
        }

        @Override
        public TypeAlias find(TypeIdentifier typeIdentifier) {
            return aliasService.typeAliasOf(typeIdentifier);
        }

        @Override
        public MethodAlias find(MethodIdentifier methodIdentifier) {
            return aliasService.methodAliasOf(methodIdentifier);
        }
    }
}
