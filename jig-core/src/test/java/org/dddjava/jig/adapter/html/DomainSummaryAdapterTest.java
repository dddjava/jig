package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDiagramOption;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

class DomainSummaryAdapterTest {

    private static class StubDocumentContext implements JigDocumentContext {
        @Override
        public Term packageTerm(PackageId packageId) {
            return new Term(new TermId(packageId.asText()), packageId.simpleName(), "", TermKind.パッケージ);
        }

        @Override
        public Term typeTerm(org.dddjava.jig.domain.model.data.types.TypeId typeId) {
            return new Term(new TermId(typeId.fqn()), typeId.asSimpleText(), "", TermKind.クラス);
        }

        @Override
        public Path outputDirectory() {
            return Path.of(".");
        }

        @Override
        public List<JigDocument> jigDocuments() {
            return JigDocument.canonical();
        }

        @Override
        public JigDiagramOption diagramOption() {
            return new JigDiagramOption(JigDiagramFormat.SVG, Duration.ofSeconds(1), true);
        }
    }
}
