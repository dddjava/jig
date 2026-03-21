package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.html.view.TreeComposite;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.documents.diagrams.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDiagramOption;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DomainSummaryAdapterTest {

    /**
     * resolveRootComposite() でルートが (default) -> com.xxx... に縮退した場合でも、
     * パッケージ一覧に含まれる上位パッケージ(comなど)の描画で例外にならないこと。
     *
     * 過去の不具合では rootComposite.findComposite("com") で例外になっていた。
     */
    @Test
    void パッケージツリーを縮退しても上位パッケージで落ちない() {
        TreeComposite baseComposite = baseCompositeWithSingleRootChain();
        TreeComposite resolvedRootComposite = baseComposite.resolveRootComposite();

        List<JigPackage> packages = List.of(
                jigPackage("com"),
                jigPackage("com.xxx"),
                jigPackage("com.xxx.xxx"),
                jigPackage("com.xxx.xxx.xxx"),
                jigPackage("com.xxx.xxx.xxx.a"),
                jigPackage("com.xxx.xxx.xxx.b")
        );

        CoreTypesAndRelations coreTypesAndRelations = new CoreTypesAndRelations(new JigTypes(List.of()), new TypeRelationships(List.of()));

        DomainSummaryAdapter adapter = new DomainSummaryAdapter(null, new StubDocumentContext());

        assertDoesNotThrow(() -> invokeBuildJson(adapter, baseComposite, resolvedRootComposite, packages, coreTypesAndRelations));
    }

    private static String invokeBuildJson(DomainSummaryAdapter adapter,
                                          TreeComposite baseComposite,
                                          TreeComposite resolvedRootComposite,
                                          List<JigPackage> packages,
                                          CoreTypesAndRelations coreTypesAndRelations) throws Exception {
        Method method = DomainSummaryAdapter.class.getDeclaredMethod(
                "buildJson",
                TreeComposite.class,
                TreeComposite.class,
                List.class,
                List.class,
                Map.class,
                CoreTypesAndRelations.class
        );
        method.setAccessible(true);
        return (String) method.invoke(adapter, baseComposite, resolvedRootComposite, packages, List.of(), Map.of(), coreTypesAndRelations);
    }

    private static TreeComposite baseCompositeWithSingleRootChain() {
        TreeComposite base = new TreeComposite(jigPackage("(default)"));

        TreeComposite com = new TreeComposite(jigPackage("com"));
        TreeComposite comXxx = new TreeComposite(jigPackage("com.xxx"));
        TreeComposite comXxxXxx = new TreeComposite(jigPackage("com.xxx.xxx"));
        TreeComposite comXxxXxxXxx = new TreeComposite(jigPackage("com.xxx.xxx.xxx"));

        base.addComponent(com);
        com.addComponent(comXxx);
        comXxx.addComponent(comXxxXxx);
        comXxxXxx.addComponent(comXxxXxxXxx);

        comXxxXxxXxx.addComponent(new TreeComposite(jigPackage("com.xxx.xxx.xxx.a")));
        comXxxXxxXxx.addComponent(new TreeComposite(jigPackage("com.xxx.xxx.xxx.b")));

        return base;
    }

    private static JigPackage jigPackage(String packageFqn) {
        PackageId packageId = "(default)".equals(packageFqn) ? PackageId.defaultPackage() : PackageId.valueOf(packageFqn);
        Term term = new Term(new TermId(packageId.asText()), packageId.simpleName(), "", TermKind.パッケージ);
        return new JigPackage(packageId, term);
    }

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
