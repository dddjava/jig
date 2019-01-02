package org.dddjava.jig.domain.model.implementation.japanese;

import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.implementation.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifier;

import java.util.Optional;

/**
 * 和名発見機
 */
public interface JapaneseNameFinder {

    PackageJapaneseName find(PackageIdentifier packageIdentifier);

    TypeJapaneseName find(TypeIdentifier typeIdentifier);

    MethodJapaneseName find(MethodIdentifier methodIdentifier);

    // TODO 取得したときにはじめてJavadocを読みに行くようにしたい
    class GlossaryServiceAdapter implements JapaneseNameFinder {

        private final GlossaryService glossaryService;

        public GlossaryServiceAdapter(GlossaryService glossaryService) {
            this.glossaryService = glossaryService;
        }

        @Override
        public PackageJapaneseName find(PackageIdentifier packageIdentifier) {
            Optional<JapaneseName> japaneseName = Optional.ofNullable(glossaryService.japaneseNameFrom(packageIdentifier));
            return new PackageJapaneseName(packageIdentifier, japaneseName.orElseGet(() -> new JapaneseName("")));
        }

        @Override
        public TypeJapaneseName find(TypeIdentifier typeIdentifier) {
            return new TypeJapaneseName(typeIdentifier, glossaryService.japaneseNameFrom(typeIdentifier));
        }

        @Override
        public MethodJapaneseName find(MethodIdentifier methodIdentifier) {
            return new MethodJapaneseName(methodIdentifier, glossaryService.japaneseNameFrom(methodIdentifier));
        }
    }
}
