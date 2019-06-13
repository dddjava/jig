package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

/**
 * 別名発見機
 */
public interface AliasFinder {

    PackageAlias find(PackageIdentifier packageIdentifier);

    TypeAlias find(TypeIdentifier typeIdentifier);

    MethodAlias find(MethodIdentifier methodIdentifier);

    class GlossaryServiceAdapter implements AliasFinder {

        private final GlossaryService glossaryService;

        public GlossaryServiceAdapter(GlossaryService glossaryService) {
            this.glossaryService = glossaryService;
        }

        @Override
        public PackageAlias find(PackageIdentifier packageIdentifier) {
            return glossaryService.packageAliasOf(packageIdentifier);
        }

        @Override
        public TypeAlias find(TypeIdentifier typeIdentifier) {
            return glossaryService.typeAliasOf(typeIdentifier);
        }

        @Override
        public MethodAlias find(MethodIdentifier methodIdentifier) {
            return glossaryService.methodAliasOf(methodIdentifier);
        }
    }
}
