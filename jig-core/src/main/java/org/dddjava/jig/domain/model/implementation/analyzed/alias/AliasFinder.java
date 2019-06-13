package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

import java.util.Optional;

/**
 * 別名発見機
 */
public interface AliasFinder {

    PackageAlias find(PackageIdentifier packageIdentifier);

    TypeAlias find(TypeIdentifier typeIdentifier);

    MethodAlias find(MethodIdentifier methodIdentifier);

    // TODO 取得したときにはじめてJavadocを読みに行くようにしたい
    class GlossaryServiceAdapter implements AliasFinder {

        private final GlossaryService glossaryService;

        public GlossaryServiceAdapter(GlossaryService glossaryService) {
            this.glossaryService = glossaryService;
        }

        @Override
        public PackageAlias find(PackageIdentifier packageIdentifier) {
            Optional<Alias> japaneseName = Optional.ofNullable(glossaryService.japaneseNameFrom(packageIdentifier));
            return new PackageAlias(packageIdentifier, japaneseName.orElseGet(() -> Alias.empty()));
        }

        @Override
        public TypeAlias find(TypeIdentifier typeIdentifier) {
            return new TypeAlias(typeIdentifier, glossaryService.japaneseNameFrom(typeIdentifier));
        }

        @Override
        public MethodAlias find(MethodIdentifier methodIdentifier) {
            return glossaryService.methodAliasOf(methodIdentifier);
        }
    }
}
